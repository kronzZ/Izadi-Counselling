import Foundation

/// Square Point of Sale configuration for Izadi Counselling (iOS).
///
/// Developer Dashboard checklist (one-time):
/// 1. https://developer.squareup.com/apps → open your app (or create one)
/// 2. Credentials → copy Application ID into `applicationId` below if different
/// 3. Point of Sale API → iOS section:
///    - Bundle ID: `com.practice.app`
///    - URL Scheme: `izadicounselling`
/// 4. Save
enum SquareConfig {
    /// Same Application ID used previously for Android testing.
    static let applicationId = "sq0idp-urZsWABGSBP75wz3nbpMvw"

    /// Must match Info.plist URL Types + Square Developer Dashboard.
    static let callbackURLScheme = "izadicounselling"

    static var callbackURL: URL {
        URL(string: "\(callbackURLScheme)://square-callback")!
    }

    static var isConfigured: Bool {
        !applicationId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    static let currencyCode = "AUD"

    static let pointOfSaleAppStoreURL = URL(string: "https://apps.apple.com/app/square-point-of-sale/id335393788")!
}
