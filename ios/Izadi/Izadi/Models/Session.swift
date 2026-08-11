import Foundation

enum SessionStatus: String, Codable, CaseIterable {
    case scheduled = "Scheduled"
    case completed = "Completed"
    case cancelled = "Cancelled"

    var label: String { rawValue }
}

enum PaymentStatus: String, Codable, CaseIterable {
    case pending = "Pending"
    case paid = "Paid"

    var label: String { rawValue }
}

enum PaymentMethod: String, Codable, CaseIterable {
    case cash = "Cash"
    case tapped = "Tapped"

    var label: String { rawValue }
}

struct Session: Identifiable, Equatable, Hashable {
    var id: String
    var clientId: String
    var clientName: String
    var date: Date
    var timeMinutes: Int
    var durationMinutes: Int
    var notes: String
    var status: SessionStatus
    var paymentStatus: PaymentStatus
    var paymentAmountCents: Int?
    var paymentMethod: PaymentMethod?

    init(
        id: String = UUID().uuidString,
        clientId: String,
        clientName: String,
        date: Date,
        timeMinutes: Int,
        durationMinutes: Int = 50,
        notes: String = "",
        status: SessionStatus = .scheduled,
        paymentStatus: PaymentStatus = .pending,
        paymentAmountCents: Int? = nil,
        paymentMethod: PaymentMethod? = nil
    ) {
        self.id = id
        self.clientId = clientId
        self.clientName = clientName
        self.date = Calendar.current.startOfDay(for: date)
        self.timeMinutes = timeMinutes
        self.durationMinutes = durationMinutes
        self.notes = notes
        self.status = status
        self.paymentStatus = paymentStatus
        self.paymentAmountCents = paymentAmountCents
        self.paymentMethod = paymentMethod
    }

    var startsAt: Date {
        Calendar.current.date(byAdding: .minute, value: timeMinutes, to: date) ?? date
    }

    var endsAt: Date {
        startsAt.addingTimeInterval(TimeInterval(durationMinutes * 60))
    }

    var formattedPaymentAmount: String? {
        paymentAmountCents.map(MoneyFormatting.formatAud(cents:))
    }

    var formattedPaidSummary: String? {
        guard let amount = formattedPaymentAmount else { return nil }
        guard let method = paymentMethod?.label else { return amount }
        return "\(method) - \(amount)"
    }

    var friendlyDate: String {
        let dayName = startsAt.formatted(.dateTime.weekday(.wide))
        let day = Calendar.current.component(.day, from: startsAt)
        let month = startsAt.formatted(.dateTime.month(.wide))
        let year = Calendar.current.component(.year, from: startsAt)
        return "\(dayName), \(day)\(ordinalSuffix(day)) \(month) \(year)"
    }

    var friendlyTime: String {
        startsAt.formatted(.dateTime.hour().minute())
    }

    var shortDate: String {
        let dayName = startsAt.formatted(.dateTime.weekday(.abbreviated))
        let day = Calendar.current.component(.day, from: startsAt)
        let month = startsAt.formatted(.dateTime.month(.abbreviated))
        return "\(dayName) \(day) \(month)"
    }

    var dateString: String {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }

    var timeString: String {
        String(format: "%02d:%02d", timeMinutes / 60, timeMinutes % 60)
    }
}

enum SessionQueries {
    static let estimatedSessionFeeDollars = 130
    static let durationOptions = [30, 45, 50, 60, 90]

    static func upcoming(_ sessions: [Session], limit: Int = 5) -> [Session] {
        sessions
            .filter { $0.status == .scheduled }
            .sorted { $0.startsAt < $1.startsAt }
            .prefix(limit)
            .map { $0 }
    }

    static func isAwaitingWrapUp(_ session: Session, now: Date = Date()) -> Bool {
        session.status == .scheduled && session.endsAt < now
    }

    static func isPast(_ session: Session) -> Bool {
        session.status == .completed || session.status == .cancelled
    }

    static func manageUpcoming(_ sessions: [Session]) -> [Session] {
        sessions.filter { !isPast($0) }.sorted { $0.startsAt < $1.startsAt }
    }

    static func managePast(_ sessions: [Session]) -> [Session] {
        sessions.filter { isPast($0) }.sorted { $0.startsAt > $1.startsAt }
    }

    static func upcomingPayments(_ sessions: [Session]) -> [Session] {
        sessions
            .filter { $0.status != .cancelled && $0.paymentStatus == .pending }
            .sorted { $0.startsAt < $1.startsAt }
    }

    static func paidPayments(_ sessions: [Session]) -> [Session] {
        sessions
            .filter { $0.status != .cancelled && $0.paymentStatus == .paid }
            .sorted { $0.startsAt > $1.startsAt }
    }

    static func estimatedUpcomingRevenueDollars(count: Int) -> Int {
        count * estimatedSessionFeeDollars
    }

    static func collectedPaymentsCents(_ paid: [Session]) -> Int {
        paid.reduce(0) { $0 + ($1.paymentAmountCents ?? 0) }
    }
}

enum MoneyFormatting {
    static func formatAud(cents: Int) -> String {
        let absolute = abs(cents)
        let dollars = absolute / 100
        let remainder = absolute % 100
        let sign = cents < 0 ? "-" : ""
        return String(format: "%@$%d.%02d", sign, dollars, remainder)
    }

    static func parseDollarsToCents(_ raw: String) -> Int? {
        var cleaned = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        if cleaned.hasPrefix("$") { cleaned.removeFirst() }
        cleaned = cleaned.replacingOccurrences(of: ",", with: "")
        cleaned = cleaned.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !cleaned.isEmpty else { return nil }
        let pattern = #"^(\d+)(?:\.(\d{0,2}))?$"#
        guard let regex = try? NSRegularExpression(pattern: pattern),
              let match = regex.firstMatch(in: cleaned, range: NSRange(cleaned.startIndex..., in: cleaned)),
              let dollarsRange = Range(match.range(at: 1), in: cleaned),
              let dollars = Int(cleaned[dollarsRange])
        else { return nil }

        let fraction: String
        if match.range(at: 2).location != NSNotFound,
           let fractionRange = Range(match.range(at: 2), in: cleaned) {
            fraction = String(cleaned[fractionRange])
        } else {
            fraction = ""
        }

        let cents: Int
        switch fraction.count {
        case 0: cents = 0
        case 1: cents = (Int(fraction) ?? 0) * 10
        default: cents = Int(fraction) ?? 0
        }
        return dollars * 100 + cents
    }
}

func ordinalSuffix(_ day: Int) -> String {
    switch day {
    case 11, 12, 13: return "th"
    default:
        switch day % 10 {
        case 1: return "st"
        case 2: return "nd"
        case 3: return "rd"
        default: return "th"
        }
    }
}

enum TimeHelpers {
    static func nextFiveMinuteSlot(from date: Date = Date()) -> Int {
        let calendar = Calendar.current
        let hour = calendar.component(.hour, from: date)
        let minute = calendar.component(.minute, from: date)
        let total = hour * 60 + minute
        let rounded = ((total + 4) / 5) * 5
        return min(rounded, (23 * 60) + 55)
    }

    static func parseTimeString(_ value: String) -> Int? {
        let parts = value.split(separator: ":")
        guard parts.count >= 2,
              let hour = Int(parts[0]),
              let minute = Int(parts[1].prefix(2))
        else { return nil }
        return hour * 60 + minute
    }
}
