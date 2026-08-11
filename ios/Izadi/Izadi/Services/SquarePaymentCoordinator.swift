import Foundation
import UIKit
import SquarePointOfSaleSDK

/// Launches Square Point of Sale for tap-to-pay and parses the return URL.
@MainActor
final class SquarePaymentCoordinator: ObservableObject {
    static let shared = SquarePaymentCoordinator()

    struct PendingCharge: Equatable {
        let sessionId: String
        let amountCents: Int
        let clientName: String
    }

    @Published private(set) var pending: PendingCharge?
    @Published var lastError: String?
    /// Set when Square returns a successful payment for `pending.sessionId`.
    @Published var completedSessionId: String?

    private var didConfigureApplicationId = false

    private init() {}

    func configureIfNeeded() {
        guard !didConfigureApplicationId, SquareConfig.isConfigured else { return }
        SCCAPIRequest.setApplicationID(SquareConfig.applicationId)
        didConfigureApplicationId = true
    }

    var isSquarePointOfSaleInstalled: Bool {
        UIApplication.shared.canOpenURL(URL(string: "square-commerce-v1://")!)
    }

    func openAppStoreListing() {
        UIApplication.shared.open(SquareConfig.pointOfSaleAppStoreURL)
    }

    /// Opens Square POS. Does **not** mark the session paid — wait for `handleOpenURL`.
    func startTapPayment(sessionId: String, amountCents: Int, clientName: String) throws {
        configureIfNeeded()
        guard SquareConfig.isConfigured else {
            throw SquarePaymentError.notConfigured
        }
        guard isSquarePointOfSaleInstalled else {
            throw SquarePaymentError.squareNotInstalled
        }
        guard amountCents > 0 else {
            throw SquarePaymentError.invalidAmount
        }

        lastError = nil
        completedSessionId = nil
        pending = PendingCharge(
            sessionId: sessionId,
            amountCents: amountCents,
            clientName: clientName
        )

        let money = try SCCMoney(amountCents: amountCents, currencyCode: SquareConfig.currencyCode)
        // userInfoString comes back on the callback so we can match the session.
        let request = try SCCAPIRequest(
            callbackURL: SquareConfig.callbackURL,
            amount: money,
            userInfoString: sessionId,
            locationID: nil,
            notes: clientName,
            customerID: nil,
            supportedTenderTypes: .all,
            clearsDefaultFees: false,
            returnsAutomaticallyAfterPayment: true,
            disablesKeyedInCardEntry: false,
            skipsReceipt: false
        )
        try SCCAPIConnection.perform(request)
    }

    /// Call from AppDelegate / scene URL handler. Returns true if this was a Square callback.
    @discardableResult
    func handleOpenURL(_ url: URL) -> Bool {
        guard SCCAPIResponse.isSquareResponse(url) else { return false }

        do {
            let response = try SCCAPIResponse(responseURL: url)
            let sessionId = response.userInfoString ?? pending?.sessionId

            if let error = response.error {
                lastError = error.localizedDescription
                pending = nil
                return true
            }

            guard response.isSuccessResponse else {
                lastError = "Square payment was not completed."
                pending = nil
                return true
            }

            guard let sessionId, !sessionId.isEmpty else {
                lastError = "Payment succeeded in Square, but the session could not be matched."
                pending = nil
                return true
            }

            completedSessionId = sessionId
            pending = nil
            lastError = nil
        } catch {
            lastError = error.localizedDescription
            pending = nil
        }
        return true
    }

    func clearCompletion() {
        completedSessionId = nil
    }

    func clearError() {
        lastError = nil
    }
}

enum SquarePaymentError: LocalizedError {
    case notConfigured
    case squareNotInstalled
    case invalidAmount

    var errorDescription: String? {
        switch self {
        case .notConfigured:
            return "Add your Square Application ID in SquareConfig.swift."
        case .squareNotInstalled:
            return "Install Square Point of Sale to take tap payments."
        case .invalidAmount:
            return "Enter a valid amount before opening Square."
        }
    }
}
