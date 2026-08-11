import SwiftUI
import UIKit

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
    /// PostScript names inside the bundled TTFs are OutfitThin-*, not Outfit-*.
    static func izadi(_ style: IzadiFontStyle) -> Font {
        let size: CGFloat
        let postScript: String
        let weight: Font.Weight

        switch style {
        case .display:
            size = 46; postScript = "OutfitThin-Light"; weight = .light
        case .title:
            size = 28; postScript = "OutfitThin-Light"; weight = .light
        case .titleMedium:
            size = 20; postScript = "OutfitThin-Light"; weight = .light
        case .body:
            size = 17; postScript = "OutfitThin-Light"; weight = .light
        case .bodyMedium:
            size = 15; postScript = "OutfitThin-Light"; weight = .light
        case .label:
            size = 13; postScript = "OutfitThin-Light"; weight = .light
        case .boldBody:
            size = 17; postScript = "OutfitThin-Bold"; weight = .bold
        }

        if UIFont(name: postScript, size: size) != nil {
            return .custom(postScript, size: size, relativeTo: style.textStyle)
        }
        // Fallback so the UI never goes blank if the font fails to load.
        return .system(size: size, weight: weight, design: .default)
    }
}

enum IzadiFontStyle {
    case display, title, titleMedium, body, bodyMedium, label, boldBody

    var textStyle: Font.TextStyle {
        switch self {
        case .display: return .largeTitle
        case .title: return .title
        case .titleMedium: return .title2
        case .body: return .body
        case .bodyMedium: return .callout
        case .label: return .caption
        case .boldBody: return .body
        }
    }
}
