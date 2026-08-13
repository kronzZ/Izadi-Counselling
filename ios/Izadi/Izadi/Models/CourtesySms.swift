import Foundation

enum CourtesySms {
    static let firstNameToken = "{firstName}"
    static let whenToken = "{when}"

    static let defaultTemplate = """
    Hey \(firstNameToken)!, Confirming our appointment for \(whenToken). Looking forward to seeing you!
    """

    /// Relative day/time phrase for the session, based on when the SMS is actioned.
    static func whenPhrase(sessionStartsAt: Date, now: Date = Date()) -> String {
        let time = sessionStartsAt.formatted(.dateTime.hour().minute())
        let calendar = Calendar.current

        if calendar.isDateInToday(sessionStartsAt) {
            return "today at \(time)"
        }
        if calendar.isDateInTomorrow(sessionStartsAt) {
            return "tomorrow at \(time)"
        }

        let dayName = sessionStartsAt.formatted(.dateTime.weekday(.wide))
        return "\(dayName) at \(time)"
    }

    static func render(
        template: String,
        firstName: String,
        sessionStartsAt: Date,
        now: Date = Date()
    ) -> String {
        template
            .replacingOccurrences(of: firstNameToken, with: firstName)
            .replacingOccurrences(
                of: whenToken,
                with: whenPhrase(sessionStartsAt: sessionStartsAt, now: now)
            )
    }

    static func notificationBody(clientName: String) -> String {
        "Courtesy SMS due for \(clientName)"
    }

    static func isValidMobile(_ mobile: String) -> Bool {
        mobile.filter(\.isNumber).count >= 8
    }
}

enum CourtesySmsSchedule {
    /// Hour slots within each reminder day (last ping 5pm).
    static let hourSlots = [9, 11, 13, 15, 17]

    /// First reminder day is 2 calendar days before the session.
    static func reminderWindowStart(for sessionStartsAt: Date, calendar: Calendar = .current) -> Date? {
        let sessionDay = calendar.startOfDay(for: sessionStartsAt)
        guard let tMinus2 = calendar.date(byAdding: .day, value: -2, to: sessionDay) else { return nil }
        var components = calendar.dateComponents([.year, .month, .day], from: tMinus2)
        components.hour = hourSlots.first ?? 9
        components.minute = 0
        return calendar.date(from: components)
    }

    /// Inclusive cutoff: the exact "3 hours before session" moment.
    static func notificationCutoff(for sessionStartsAt: Date) -> Date {
        sessionStartsAt.addingTimeInterval(-3 * 60 * 60)
    }

    /// Future fire times for a session, applying late-booking and cutoff rules.
    static func fireTimes(
        for session: Session,
        now: Date = Date(),
        calendar: Calendar = .current
    ) -> [Date] {
        guard session.status == .scheduled, !session.courtesySmsCompleted else { return [] }

        let sessionStart = session.startsAt
        let cutoff = notificationCutoff(for: sessionStart)
        guard now <= cutoff else { return [] }

        guard let windowStartDay = calendar.date(
            byAdding: .day,
            value: -2,
            to: calendar.startOfDay(for: sessionStart)
        ) else { return [] }

        let sessionDay = calendar.startOfDay(for: sessionStart)
        var times: [Date] = []
        var day = windowStartDay

        while day <= sessionDay {
            for hour in hourSlots {
                var components = calendar.dateComponents([.year, .month, .day], from: day)
                components.hour = hour
                components.minute = 0
                guard let slot = calendar.date(from: components) else { continue }
                if slot > now && slot <= cutoff {
                    times.append(slot)
                }
            }
            guard let next = calendar.date(byAdding: .day, value: 1, to: day) else { break }
            day = next
        }

        return times
    }
}
