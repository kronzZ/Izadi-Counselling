import SwiftUI

enum IzadiColor {
    static let ink = Color(red: 0x2E / 255, green: 0x3C / 255, blue: 0x3A / 255)
    static let inkSoft = Color(red: 0x6E / 255, green: 0x7F / 255, blue: 0x7B / 255)
    static let mist = Color(red: 0xF3 / 255, green: 0xF6 / 255, blue: 0xF5 / 255)
    static let sage = Color(red: 0x6B / 255, green: 0x95 / 255, blue: 0x8C / 255)
    static let sageSoft = Color(red: 0x87 / 255, green: 0xAF / 255, blue: 0xA6 / 255)
    static let foam = Color(red: 0xFA / 255, green: 0xFC / 255, blue: 0xFB / 255)
    static let butter = Color(red: 0xF3 / 255, green: 0xE6 / 255, blue: 0xC4 / 255)
    static let bloom = Color(red: 0xE8 / 255, green: 0xF0 / 255, blue: 0xED / 255)
    static let rose = Color(red: 0xE8 / 255, green: 0xB4 / 255, blue: 0xB0 / 255)
    static let roseDeep = Color(red: 0x9A / 255, green: 0x5B / 255, blue: 0x56 / 255)
    static let softSky = Color(red: 0xEA / 255, green: 0xF2 / 255, blue: 0xF0 / 255)
    static let softCloud = Color(red: 0xF7 / 255, green: 0xF9 / 255, blue: 0xF8 / 255)
}

struct SoftScreenBackground<Content: View>: View {
    var fullMint: Bool = false
    @ViewBuilder var content: () -> Content

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Group {
                if fullMint {
                    IzadiColor.softSky
                } else {
                    LinearGradient(
                        colors: [IzadiColor.softSky, IzadiColor.mist, IzadiColor.softCloud],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                }
            }
            .ignoresSafeArea()

            RadialGradient(
                colors: [IzadiColor.sageSoft.opacity(0.20), .clear],
                center: .topTrailing,
                startRadius: 20,
                endRadius: 320
            )
            .frame(height: 300)
            .ignoresSafeArea()

            content()
        }
    }
}

extension Font {
    static func izadi(_ style: IzadiFontStyle) -> Font {
        switch style {
        case .display:
            return .custom("Outfit-Light", size: 46, relativeTo: .largeTitle)
        case .title:
            return .custom("Outfit-Light", size: 28, relativeTo: .title)
        case .titleMedium:
            return .custom("Outfit-Light", size: 20, relativeTo: .title2)
        case .body:
            return .custom("Outfit-Light", size: 17, relativeTo: .body)
        case .bodyMedium:
            return .custom("Outfit-Light", size: 15, relativeTo: .callout)
        case .label:
            return .custom("Outfit-Light", size: 13, relativeTo: .caption)
        case .boldBody:
            return .custom("Outfit-Bold", size: 17, relativeTo: .body)
        }
    }
}

enum IzadiFontStyle {
    case display, title, titleMedium, body, bodyMedium, label, boldBody
}
