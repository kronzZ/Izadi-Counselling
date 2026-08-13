import Foundation
import Combine
import UserNotifications

/// Schedules local courtesy-SMS reminders from session rules and opens the action sheet on tap.
@MainActor
final class CourtesyReminderService: NSObject, ObservableObject {
    static let shared = CourtesyReminderService()

    static let notificationPrefix = "courtesy."
    static let sessionIdKey = "sessionId"

    /// When set (e.g. from a notification tap), Home should present the courtesy SMS sheet.
    @Published var actionSessionId: String?

    private var configured = false

    func configure() {
        guard !configured else { return }
        configured = true
        UNUserNotificationCenter.current().delegate = self
    }

    func requestAuthorizationIfNeeded() async {
        let center = UNUserNotificationCenter.current()
        let settings = await center.notificationSettings()
        switch settings.authorizationStatus {
        case .notDetermined:
            _ = try? await center.requestAuthorization(options: [.alert, .sound, .badge])
        default:
            break
        }
    }

    func clearActionSession() {
        actionSessionId = nil
    }

    func sync(sessions: [Session]) async {
        let center = UNUserNotificationCenter.current()
        let pending = await center.pendingNotificationRequests()
        let ours = pending
            .map(\.identifier)
            .filter { $0.hasPrefix(Self.notificationPrefix) }
        if !ours.isEmpty {
            center.removePendingNotificationRequests(withIdentifiers: ours)
        }

        var requests: [UNNotificationRequest] = []
        for session in sessions {
            for fireDate in CourtesySmsSchedule.fireTimes(for: session) {
                let identifier = notificationIdentifier(sessionId: session.id, fireDate: fireDate)
                let content = UNMutableNotificationContent()
                content.title = "Izadi Counselling"
                content.body = CourtesySms.notificationBody(clientName: session.clientName)
                content.sound = .default
                content.userInfo = [Self.sessionIdKey: session.id]

                let components = Calendar.current.dateComponents(
                    [.year, .month, .day, .hour, .minute, .second],
                    from: fireDate
                )
                let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
                requests.append(UNNotificationRequest(identifier: identifier, content: content, trigger: trigger))
            }
        }

        // Leave headroom under iOS's ~64 pending-notification limit.
        for request in requests.sorted(by: { lhs, rhs in
            let left = (lhs.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() ?? .distantFuture
            let right = (rhs.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() ?? .distantFuture
            return left < right
        }.prefix(60)) {
            try? await center.add(request)
        }
    }

    func cancelAll() async {
        let center = UNUserNotificationCenter.current()
        let pending = await center.pendingNotificationRequests()
        let pendingOurs = pending
            .map(\.identifier)
            .filter { $0.hasPrefix(Self.notificationPrefix) }
        if !pendingOurs.isEmpty {
            center.removePendingNotificationRequests(withIdentifiers: pendingOurs)
        }

        let delivered = await center.deliveredNotifications()
        let deliveredOurs = delivered
            .map(\.request.identifier)
            .filter { $0.hasPrefix(Self.notificationPrefix) }
        if !deliveredOurs.isEmpty {
            center.removeDeliveredNotifications(withIdentifiers: deliveredOurs)
        }
    }

    private func notificationIdentifier(sessionId: String, fireDate: Date) -> String {
        let stamp = Int(fireDate.timeIntervalSince1970)
        return "\(Self.notificationPrefix)\(sessionId).\(stamp)"
    }

    private func handleNotificationResponse(_ response: UNNotificationResponse) {
        let info = response.notification.request.content.userInfo
        guard let sessionId = info[Self.sessionIdKey] as? String else { return }
        actionSessionId = sessionId
    }
}

extension CourtesyReminderService: UNUserNotificationCenterDelegate {
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }

    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse
    ) async {
        await MainActor.run {
            handleNotificationResponse(response)
        }
    }
}
