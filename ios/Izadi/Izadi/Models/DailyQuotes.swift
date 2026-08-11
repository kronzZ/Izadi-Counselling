import Foundation

enum DailyQuotes {
    private static let lines = [
        "A quiet hour can hold more than a busy week.",
        "You don’t have to have the answer — only the next honest step.",
        "Presence is a kind of care.",
        "Small truths, spoken gently, still move things.",
        "Listening is a form of courage.",
        "What feels heavy today may become clearer in stillness.",
        "There is room here for whatever arrives.",
        "Progress isn’t always loud.",
        "Being seen can be the beginning of change.",
        "Tenderness is not the opposite of strength.",
    ]

    static func today(on date: Date = Date()) -> String {
        let epochDay = Int(date.timeIntervalSince1970 / 86_400)
        return lines[abs(epochDay) % lines.count]
    }
}
