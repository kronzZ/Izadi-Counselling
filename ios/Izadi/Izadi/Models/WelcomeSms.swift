import Foundation

enum WelcomeSms {
    static let firstNameToken = "{firstName}"

    static let defaultTemplate = """
    Hi \(firstNameToken),

    Welcome to Izadi Counselling — thank you for getting in touch. I’m looking forward to supporting you.

    Sessions are typically 50 minutes.
    Standard fee: $150 per session.
    Payment can be arranged at the end of each session.

    If you have any questions before we meet, feel free to reply to this message.

    Warm regards,
    Izadi Counselling
    """

    static func render(template: String, firstName: String) -> String {
        let name = firstName.trimmingCharacters(in: .whitespacesAndNewlines)
        let resolved = name.isEmpty ? "there" : name
        return template.replacingOccurrences(of: firstNameToken, with: resolved)
    }

    static func isValidMobile(_ mobile: String) -> Bool {
        mobile.filter(\.isNumber).count >= 8
    }
}
