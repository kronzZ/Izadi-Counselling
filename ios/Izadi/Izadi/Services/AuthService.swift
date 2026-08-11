import Foundation
import FirebaseAuth
import Combine

@MainActor
final class AuthService: ObservableObject {
    @Published private(set) var user: User?
    @Published private(set) var isConfiguring = true
    @Published var errorMessage: String?

    private var handle: AuthStateDidChangeListenerHandle?

    init() {
        handle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                self?.user = user
                if user != nil {
                    self?.isConfiguring = false
                }
            }
        }

        Task {
            await ensureSignedIn()
        }
    }

    deinit {
        if let handle {
            Auth.auth().removeStateDidChangeListener(handle)
        }
    }

    var uid: String? { user?.uid }
    var isSignedIn: Bool { user != nil }

    /// Single-user app: sign in anonymously with no email/password UI.
    func ensureSignedIn() async {
        errorMessage = nil
        if Auth.auth().currentUser != nil {
            isConfiguring = false
            return
        }
        do {
            _ = try await Auth.auth().signInAnonymously()
            isConfiguring = false
        } catch {
            errorMessage = error.localizedDescription
            isConfiguring = false
        }
    }

    func markConfigured() {
        isConfiguring = false
    }
}
