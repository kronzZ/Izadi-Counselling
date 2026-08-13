import Foundation
import FirebaseAuth
import FirebaseFirestore

enum FirestorePaths {
    static func practice(_ uid: String) -> DocumentReference {
        Firestore.firestore().collection("practices").document(uid)
    }

    static func clients(_ uid: String) -> CollectionReference {
        practice(uid).collection("clients")
    }

    static func sessions(_ uid: String) -> CollectionReference {
        practice(uid).collection("sessions")
    }
}

struct PracticeSettings: Equatable {
    var name: String
    var welcomeSmsTemplate: String
    var courtesySmsTemplate: String
    var currency: String
    var estimatedFeeDollars: Int
    var defaultFeeCents: Int

    static let `default` = PracticeSettings(
        name: "Izadi Counselling",
        welcomeSmsTemplate: WelcomeSms.defaultTemplate,
        courtesySmsTemplate: CourtesySms.defaultTemplate,
        currency: "AUD",
        estimatedFeeDollars: 130,
        defaultFeeCents: 15_000
    )
}

enum FirestoreMapping {
    static func client(from data: [String: Any], id: String) -> Client? {
        guard
            let firstName = data["firstName"] as? String,
            let surname = data["surname"] as? String
        else { return nil }

        let relationshipRaw = data["relationship"] as? String
        let relationshipCustomRaw = (data["relationshipCustom"] as? String ?? "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        let (relationship, relationshipCustom) = EmergencyRelationship.parseStored(
            relationship: relationshipRaw,
            relationshipCustom: relationshipCustomRaw
        )

        return Client(
            id: id,
            firstName: firstName,
            surname: surname,
            dateOfBirth: data["dateOfBirth"] as? String ?? "",
            mobile: data["mobile"] as? String ?? "",
            emergencyContactName: data["emergencyContactName"] as? String ?? "",
            emergencyContactNumber: data["emergencyContactNumber"] as? String ?? "",
            relationship: relationship,
            relationshipCustom: relationshipCustom,
            isActive: data["isActive"] as? Bool ?? true,
            createdAtEpochMs: (data["createdAtEpochMs"] as? NSNumber)?.int64Value
                ?? Int64(Date().timeIntervalSince1970 * 1000)
        )
    }

    static func clientData(_ client: Client) -> [String: Any] {
        [
            "firstName": client.firstName,
            "surname": client.surname,
            "dateOfBirth": client.dateOfBirth,
            "mobile": client.mobile,
            "emergencyContactName": client.emergencyContactName,
            "emergencyContactNumber": client.emergencyContactNumber,
            "relationship": client.relationship?.rawValue as Any,
            "relationshipCustom": client.relationship == .other
                ? client.relationshipCustom.trimmingCharacters(in: .whitespacesAndNewlines)
                : "",
            "isActive": client.isActive,
            "createdAtEpochMs": client.createdAtEpochMs,
        ]
    }

    static func session(from data: [String: Any], id: String) -> Session? {
        guard
            let clientId = data["clientId"] as? String,
            let clientName = data["clientName"] as? String,
            let dateString = data["date"] as? String,
            let timeString = data["time"] as? String,
            let duration = data["durationMinutes"] as? Int
        else { return nil }

        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "yyyy-MM-dd"
        guard let date = formatter.date(from: dateString),
              let timeMinutes = TimeHelpers.parseTimeString(timeString)
        else { return nil }

        let statusRaw = data["status"] as? String ?? SessionStatus.scheduled.rawValue
        let status: SessionStatus = {
            if statusRaw == "Postponed" { return .scheduled }
            return SessionStatus(rawValue: statusRaw) ?? .scheduled
        }()

        let paymentRaw = data["paymentStatus"] as? String
        let paymentStatus: PaymentStatus = {
            if paymentRaw == "NotPaid" { return .paid }
            if let paymentRaw, let parsed = PaymentStatus(rawValue: paymentRaw) {
                return parsed
            }
            return status == .completed ? .paid : .pending
        }()

        let methodRaw = data["paymentMethod"] as? String
        let method = methodRaw.flatMap(PaymentMethod.init(rawValue:))
        let amount = data["paymentAmountCents"] as? Int

        return Session(
            id: id,
            clientId: clientId,
            clientName: clientName,
            date: date,
            timeMinutes: timeMinutes,
            durationMinutes: duration,
            notes: data["notes"] as? String ?? "",
            status: status,
            paymentStatus: paymentStatus,
            paymentAmountCents: amount,
            paymentMethod: method,
            courtesySmsCompleted: data["courtesySmsCompleted"] as? Bool ?? false
        )
    }

    static func sessionData(_ session: Session) -> [String: Any] {
        [
            "clientId": session.clientId,
            "clientName": session.clientName,
            "date": session.dateString,
            "time": session.timeString,
            "startsAt": Timestamp(date: session.startsAt),
            "endsAt": Timestamp(date: session.endsAt),
            "durationMinutes": session.durationMinutes,
            "notes": session.notes,
            "status": session.status.rawValue,
            "paymentStatus": session.paymentStatus.rawValue,
            "paymentAmountCents": session.paymentAmountCents as Any,
            "paymentMethod": session.paymentMethod?.rawValue as Any,
            "courtesySmsCompleted": session.courtesySmsCompleted,
            "updatedAt": FieldValue.serverTimestamp(),
        ]
    }

    static func settings(from data: [String: Any]?) -> PracticeSettings {
        guard let data else { return .default }
        return PracticeSettings(
            name: data["name"] as? String ?? PracticeSettings.default.name,
            welcomeSmsTemplate: data["welcomeSmsTemplate"] as? String ?? WelcomeSms.defaultTemplate,
            courtesySmsTemplate: data["courtesySmsTemplate"] as? String ?? CourtesySms.defaultTemplate,
            currency: data["currency"] as? String ?? "AUD",
            estimatedFeeDollars: data["estimatedFeeDollars"] as? Int ?? 130,
            defaultFeeCents: data["defaultFeeCents"] as? Int ?? 15_000
        )
    }

    static func settingsData(_ settings: PracticeSettings) -> [String: Any] {
        [
            "name": settings.name,
            "welcomeSmsTemplate": settings.welcomeSmsTemplate,
            "courtesySmsTemplate": settings.courtesySmsTemplate,
            "currency": settings.currency,
            "estimatedFeeDollars": settings.estimatedFeeDollars,
            "defaultFeeCents": settings.defaultFeeCents,
            "updatedAt": FieldValue.serverTimestamp(),
        ]
    }
}

final class FirestoreRepository {
    private var clientListener: ListenerRegistration?
    private var sessionListener: ListenerRegistration?
    private var settingsListener: ListenerRegistration?

    func ensurePracticeDocument(uid: String, settings: PracticeSettings = .default) async throws {
        let ref = FirestorePaths.practice(uid)
        let snapshot = try await ref.getDocument()
        if !snapshot.exists {
            try await ref.setData(FirestoreMapping.settingsData(settings))
        }
    }

    func listen(
        uid: String,
        onClients: @escaping ([Client]) -> Void,
        onSessions: @escaping ([Session]) -> Void,
        onSettings: @escaping (PracticeSettings) -> Void,
        onError: @escaping (Error) -> Void
    ) {
        stopListening()

        settingsListener = FirestorePaths.practice(uid).addSnapshotListener { snapshot, error in
            if let error {
                onError(error)
                return
            }
            onSettings(FirestoreMapping.settings(from: snapshot?.data()))
        }

        clientListener = FirestorePaths.clients(uid)
            .order(by: "createdAtEpochMs", descending: true)
            .addSnapshotListener { snapshot, error in
                if let error {
                    onError(error)
                    return
                }
                let clients = snapshot?.documents.compactMap {
                    FirestoreMapping.client(from: $0.data(), id: $0.documentID)
                } ?? []
                onClients(clients)
            }

        sessionListener = FirestorePaths.sessions(uid)
            .order(by: "startsAt", descending: false)
            .addSnapshotListener { snapshot, error in
                if let error {
                    onError(error)
                    return
                }
                let sessions = snapshot?.documents.compactMap {
                    FirestoreMapping.session(from: $0.data(), id: $0.documentID)
                } ?? []
                onSessions(sessions)
            }
    }

    func stopListening() {
        clientListener?.remove()
        sessionListener?.remove()
        settingsListener?.remove()
        clientListener = nil
        sessionListener = nil
        settingsListener = nil
    }

    func upsertClient(uid: String, client: Client) async throws {
        try await FirestorePaths.clients(uid)
            .document(client.id)
            .setData(FirestoreMapping.clientData(client), merge: true)
    }

    func deleteClient(uid: String, clientId: String) async throws {
        let db = Firestore.firestore()
        let batch = db.batch()
        batch.deleteDocument(FirestorePaths.clients(uid).document(clientId))

        let sessionDocs = try await FirestorePaths.sessions(uid)
            .whereField("clientId", isEqualTo: clientId)
            .getDocuments()
        for doc in sessionDocs.documents {
            batch.deleteDocument(doc.reference)
        }
        try await batch.commit()
    }

    func upsertSession(uid: String, session: Session) async throws {
        try await FirestorePaths.sessions(uid)
            .document(session.id)
            .setData(FirestoreMapping.sessionData(session), merge: true)
    }

    func deleteSession(uid: String, sessionId: String) async throws {
        try await FirestorePaths.sessions(uid).document(sessionId).delete()
    }

    func updateWelcomeSmsTemplate(uid: String, template: String) async throws {
        let saved = template.trimmingCharacters(in: .whitespacesAndNewlines)
        let value = saved.isEmpty ? WelcomeSms.defaultTemplate : saved
        try await FirestorePaths.practice(uid).setData(
            [
                "welcomeSmsTemplate": value,
                "updatedAt": FieldValue.serverTimestamp(),
            ],
            merge: true
        )
    }

    func updateCourtesySmsTemplate(uid: String, template: String) async throws {
        let saved = template.trimmingCharacters(in: .whitespacesAndNewlines)
        let value = saved.isEmpty ? CourtesySms.defaultTemplate : saved
        try await FirestorePaths.practice(uid).setData(
            [
                "courtesySmsTemplate": value,
                "updatedAt": FieldValue.serverTimestamp(),
            ],
            merge: true
        )
    }
}
