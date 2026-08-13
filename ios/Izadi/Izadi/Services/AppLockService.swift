import Foundation
import LocalAuthentication
import SwiftUI
import Combine

/// Gate for Face ID / Touch ID / device passcode when the practice is signed in.
/// Locks on cold start and whenever the app leaves to the background.
@MainActor
final class AppLockService: ObservableObject {
    @Published private(set) var isUnlocked = false
    @Published private(set) var isAuthenticating = false
    /// Hides practice UI in the app switcher / Control Center without forcing Face ID yet.
    @Published private(set) var isPrivacyCovered = false
    @Published var errorMessage: String?

    /// When true, the next return from background skips biometrics (Square POS handoff).
    private var skipLockOnce = false

    var needsLockScreen: Bool {
        !isUnlocked || isPrivacyCovered
    }

    var biometryLabel: String {
        let context = LAContext()
        _ = context.canEvaluatePolicy(.deviceOwnerAuthentication, error: nil)
        switch context.biometryType {
        case .faceID: return "Face ID"
        case .touchID: return "Touch ID"
        case .opticID: return "Optic ID"
        default: return "Passcode"
        }
    }

    func lock() {
        isUnlocked = false
        errorMessage = nil
    }

    /// No biometric gate on the email login screen.
    func clearForSignedOut() {
        isUnlocked = true
        isPrivacyCovered = false
        skipLockOnce = false
        errorMessage = nil
    }

    /// Call before opening Square Point of Sale so returning from payment isn’t blocked.
    func allowNextResumeWithoutAuth() {
        skipLockOnce = true
    }

    func handleScenePhase(_ phase: ScenePhase, isSignedIn: Bool) {
        guard isSignedIn else {
            clearForSignedOut()
            return
        }

        switch phase {
        case .inactive:
            if !skipLockOnce {
                isPrivacyCovered = true
            }
        case .background:
            isPrivacyCovered = true
            if !skipLockOnce {
                lock()
            }
        case .active:
            isPrivacyCovered = false
            if skipLockOnce {
                skipLockOnce = false
                isUnlocked = true
                return
            }
            if !isUnlocked {
                Task { await authenticate() }
            }
        @unknown default:
            break
        }
    }

    func authenticate() async {
        guard !isAuthenticating else { return }

        isAuthenticating = true
        errorMessage = nil
        defer { isAuthenticating = false }

        #if targetEnvironment(simulator)
        // Simulator often has no biometrics/passcode — unlock so UI can be tested.
        isUnlocked = true
        return
        #else
        let context = LAContext()
        context.localizedCancelTitle = "Cancel"
        var authError: NSError?

        guard context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &authError) else {
            errorMessage = authError?.localizedDescription
                ?? "Biometrics or device passcode are not available on this device."
            isUnlocked = false
            return
        }

        do {
            let ok = try await context.evaluatePolicy(
                .deviceOwnerAuthentication,
                localizedReason: "Unlock Izadi Counselling"
            )
            isUnlocked = ok
            if ok { errorMessage = nil }
        } catch let error as LAError
            where error.code == .userCancel
                || error.code == .appCancel
                || error.code == .systemCancel {
            isUnlocked = false
            errorMessage = nil
        } catch {
            isUnlocked = false
            errorMessage = error.localizedDescription
        }
        #endif
    }
}
