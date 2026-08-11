import Foundation
import FirebaseAuth
import FirebaseCore
import Combine

@MainActor
final class AuthService: ObservableObject {
    @Published private(set) var user: User?
    @Published private(set) var isConfiguring = true
    @Published var errorMessage: String?

    private var handle: AuthStateDidChangeListenerHandle?

    init() {
        // If GoogleService-Info.plist is still placeholder values, Firebase Auth
        // will fail at runtime with vague errors. Detect that early so we
        // don't waste time debugging "internal error" messages.
        if let options = FirebaseApp.app()?.options {
            let apiKey = options.apiKey ?? ""
            let projectId = options.projectID ?? ""
            if apiKey.contains("REPLACE_WITH_FIREBASE_IOS_API_KEY")
                || projectId.contains("placeholder") {
                errorMessage = "Firebase is not configured for this build. Replace ios/Izadi/Izadi/GoogleService-Info.plist with the real file from Firebase for bundle ID com.turtletech.izadicounselling."
            }
        }

        handle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                self?.user = user
                self?.isConfiguring = false
            }
        }
    }

    deinit {
        if let handle {
            Auth.auth().removeStateDidChangeListener(handle)
        }
    }

    var uid: String? { user?.uid }
    var isSignedIn: Bool { user != nil }

    func signIn(email: String, password: String) async {
        errorMessage = nil
        do {
            _ = try await Auth.auth().signIn(
                withEmail: email.trimmingCharacters(in: .whitespacesAndNewlines),
                password: password
            )
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func signUp(email: String, password: String) async {
        errorMessage = nil
        do {
            _ = try await Auth.auth().createUser(
                withEmail: email.trimmingCharacters(in: .whitespacesAndNewlines),
                password: password
            )
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func signOut() {
        errorMessage = nil
        do {
            try Auth.auth().signOut()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func markConfigured() {
        isConfiguring = false
    }
}
