import Foundation
import Combine

@MainActor
final class PracticeStore: ObservableObject {
    @Published private(set) var clients: [Client] = []
    @Published private(set) var sessions: [Session] = []
    @Published private(set) var welcomeSmsTemplate: String = WelcomeSms.defaultTemplate
    @Published private(set) var isLoading = false
    @Published var highlightClientId: String?
    @Published var errorMessage: String?

    private let repository = FirestoreRepository()
    private var uid: String?

    func start(uid: String) {
        guard self.uid != uid else { return }
        self.uid = uid
        isLoading = true
        errorMessage = nil

        Task {
            do {
                try await repository.ensurePracticeDocument(uid: uid)
            } catch {
                errorMessage = error.localizedDescription
            }
        }

        repository.listen(
            uid: uid,
            onClients: { [weak self] clients in
                Task { @MainActor in
                    self?.clients = clients
                    self?.isLoading = false
                }
            },
            onSessions: { [weak self] sessions in
                Task { @MainActor in
                    self?.sessions = sessions
                    self?.isLoading = false
                }
            },
            onSettings: { [weak self] settings in
                Task { @MainActor in
                    self?.welcomeSmsTemplate = settings.welcomeSmsTemplate
                }
            },
            onError: { [weak self] error in
                Task { @MainActor in
                    self?.errorMessage = error.localizedDescription
                    self?.isLoading = false
                }
            }
        )
    }

    func stop() {
        repository.stopListening()
        uid = nil
        clients = []
        sessions = []
        welcomeSmsTemplate = WelcomeSms.defaultTemplate
        highlightClientId = nil
    }

    func addClient(_ client: Client) {
        guard let uid else { return }
        highlightClientId = client.id
        clients.insert(client, at: 0)
        Task {
            do {
                try await repository.upsertClient(uid: uid, client: client)
                errorMessage = nil
            } catch {
                clients.removeAll { $0.id == client.id }
                highlightClientId = nil
                errorMessage = "Couldn’t save client: \(error.localizedDescription)"
            }
        }
    }

    func updateClient(_ client: Client) {
        guard let uid else { return }
        if let index = clients.firstIndex(where: { $0.id == client.id }) {
            clients[index] = client
        }
        Task {
            do {
                try await repository.upsertClient(uid: uid, client: client)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func toggleClientActive(clientId: String) {
        guard let index = clients.firstIndex(where: { $0.id == clientId }) else { return }
        var updated = clients[index]
        updated.isActive.toggle()
        updateClient(updated)
    }

    func deleteClient(clientId: String) {
        guard let uid else { return }
        clients.removeAll { $0.id == clientId }
        sessions.removeAll { $0.clientId == clientId }
        Task {
            do {
                try await repository.deleteClient(uid: uid, clientId: clientId)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func addSession(_ session: Session) {
        guard let uid else { return }
        sessions.append(session)
        Task {
            do {
                try await repository.upsertSession(uid: uid, session: session)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func updateSession(_ session: Session) {
        guard let uid else { return }
        if let index = sessions.firstIndex(where: { $0.id == session.id }) {
            sessions[index] = session
        }
        Task {
            do {
                try await repository.upsertSession(uid: uid, session: session)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func deleteSession(sessionId: String) {
        guard let uid else { return }
        sessions.removeAll { $0.id == sessionId }
        Task {
            do {
                try await repository.deleteSession(uid: uid, sessionId: sessionId)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func updateWelcomeSmsTemplate(_ template: String) {
        guard let uid else { return }
        let saved = template.trimmingCharacters(in: .whitespacesAndNewlines)
        welcomeSmsTemplate = saved.isEmpty ? WelcomeSms.defaultTemplate : saved
        Task {
            do {
                try await repository.updateWelcomeSmsTemplate(uid: uid, template: welcomeSmsTemplate)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func clearHighlightClientId() {
        highlightClientId = nil
    }
}
