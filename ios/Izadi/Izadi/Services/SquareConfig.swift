import Foundation

/// Paste your Square Application ID from:
/// https://developer.squareup.com/apps → Credentials
///
/// Also register this iOS bundle ID in Point of Sale API settings:
/// - Bundle ID: com.practice.app
enum SquareConfig {
    static let applicationId = "sq0idp-f1wy8hZaRNEuvqGnb-yFnw"
    static var isConfigured: Bool { !applicationId.isEmpty }
}
