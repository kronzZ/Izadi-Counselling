package com.practice.app.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class PracticeSettings(
    val name: String = "Izadi Counselling",
    val welcomeSmsTemplate: String = defaultWelcomeSmsTemplate(),
    val currency: String = "AUD",
    val estimatedFeeDollars: Int = EstimatedSessionFeeDollars,
    val defaultFeeCents: Int = 15_000,
)

class FirestoreRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun practiceRef(uid: String) = db.collection("practices").document(uid)
    private fun clientsRef(uid: String) = practiceRef(uid).collection("clients")
    private fun sessionsRef(uid: String) = practiceRef(uid).collection("sessions")

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun ensurePracticeDocument(uid: String, settings: PracticeSettings = PracticeSettings()) {
        val snap = practiceRef(uid).get().await()
        if (!snap.exists()) {
            practiceRef(uid).set(settings.toMap()).await()
        }
    }

    fun observeClients(uid: String): Flow<List<Client>> = callbackFlow {
        val registration: ListenerRegistration = clientsRef(uid)
            .orderBy("createdAtEpochMs", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val clients = snapshot?.documents?.mapNotNull { it.toClientOrNull() }.orEmpty()
                trySend(clients)
            }
        awaitClose { registration.remove() }
    }

    fun observeSessions(uid: String): Flow<List<Session>> = callbackFlow {
        val registration: ListenerRegistration = sessionsRef(uid)
            .orderBy("startsAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull { it.toSessionOrNull() }.orEmpty()
                trySend(sessions)
            }
        awaitClose { registration.remove() }
    }

    fun observeSettings(uid: String): Flow<PracticeSettings> = callbackFlow {
        val registration = practiceRef(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.data.toPracticeSettings())
        }
        awaitClose { registration.remove() }
    }

    suspend fun upsertClient(uid: String, client: Client) {
        clientsRef(uid).document(client.id).set(client.toMap(), SetOptions.merge()).await()
    }

    suspend fun deleteClient(uid: String, clientId: String) {
        val batch = db.batch()
        batch.delete(clientsRef(uid).document(clientId))
        val sessionDocs = sessionsRef(uid).whereEqualTo("clientId", clientId).get().await()
        sessionDocs.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun upsertSession(uid: String, session: Session) {
        sessionsRef(uid).document(session.id).set(session.toMap(), SetOptions.merge()).await()
    }

    suspend fun updateWelcomeSmsTemplate(uid: String, template: String) {
        val saved = template.trim().ifEmpty { defaultWelcomeSmsTemplate() }
        practiceRef(uid).set(
            mapOf(
                "welcomeSmsTemplate" to saved,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
            SetOptions.merge(),
        ).await()
    }

    /** One-time import of on-device JSON into Firestore (merge by document id). */
    suspend fun importLocalData(uid: String, clients: List<Client>, sessions: List<Session>, template: String) {
        ensurePracticeDocument(
            uid,
            PracticeSettings(welcomeSmsTemplate = template.ifBlank { defaultWelcomeSmsTemplate() }),
        )
        val batchSize = 400
        clients.chunked(batchSize).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { client ->
                batch.set(clientsRef(uid).document(client.id), client.toMap(), SetOptions.merge())
            }
            batch.commit().await()
        }
        sessions.chunked(batchSize).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { session ->
                batch.set(sessionsRef(uid).document(session.id), session.toMap(), SetOptions.merge())
            }
            batch.commit().await()
        }
        updateWelcomeSmsTemplate(uid, template)
    }
}

private fun Client.toMap(): Map<String, Any?> = mapOf(
    "firstName" to firstName,
    "surname" to surname,
    "dateOfBirth" to dateOfBirth,
    "mobile" to mobile,
    "emergencyContactName" to emergencyContactName,
    "emergencyContactNumber" to emergencyContactNumber,
    "relationship" to relationship?.name,
    "isActive" to isActive,
    "createdAtEpochMs" to createdAtEpochMs,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toClientOrNull(): Client? {
    val data = data ?: return null
    val firstName = data["firstName"] as? String ?: return null
    val surname = data["surname"] as? String ?: return null
    val relationshipName = data["relationship"] as? String
    return Client(
        id = id,
        firstName = firstName,
        surname = surname,
        dateOfBirth = data["dateOfBirth"] as? String ?: "",
        mobile = data["mobile"] as? String ?: "",
        emergencyContactName = data["emergencyContactName"] as? String ?: "",
        emergencyContactNumber = data["emergencyContactNumber"] as? String ?: "",
        relationship = relationshipName?.let { name ->
            EmergencyRelationship.entries.find { it.name == name }
        },
        isActive = data["isActive"] as? Boolean ?: true,
        createdAtEpochMs = (data["createdAtEpochMs"] as? Number)?.toLong() ?: 0L,
    )
}

private fun Session.toMap(): Map<String, Any?> {
    val zone = ZoneId.systemDefault()
    val starts = startsAt.atZone(zone).toInstant()
    val ends = endsAt.atZone(zone).toInstant()
    return mapOf(
        "clientId" to clientId,
        "clientName" to clientName,
        "date" to date.toString(),
        "time" to time.toString().take(5),
        "startsAt" to Timestamp(starts.epochSecond, starts.nano),
        "endsAt" to Timestamp(ends.epochSecond, ends.nano),
        "durationMinutes" to durationMinutes,
        "notes" to notes,
        "status" to status.name,
        "paymentStatus" to paymentStatus.name,
        "paymentAmountCents" to paymentAmountCents,
        "paymentMethod" to paymentMethod?.name,
        "updatedAt" to FieldValue.serverTimestamp(),
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toSessionOrNull(): Session? {
    val data = data ?: return null
    val clientId = data["clientId"] as? String ?: return null
    val clientName = data["clientName"] as? String ?: return null
    val date = runCatching { LocalDate.parse(data["date"] as? String ?: return null) }.getOrNull()
        ?: return null
    val time = runCatching { LocalTime.parse(data["time"] as? String ?: return null) }.getOrNull()
        ?: return null
    val duration = (data["durationMinutes"] as? Number)?.toInt() ?: return null
    val statusRaw = data["status"] as? String ?: SessionStatus.Scheduled.name
    val status = when (statusRaw) {
        "Postponed" -> SessionStatus.Scheduled
        else -> SessionStatus.entries.find { it.name == statusRaw } ?: SessionStatus.Scheduled
    }
    val paymentRaw = data["paymentStatus"] as? String
    val paymentStatus = when (paymentRaw) {
        "NotPaid" -> PaymentStatus.Paid
        else -> paymentRaw?.let { name -> PaymentStatus.entries.find { it.name == name } }
            ?: if (status == SessionStatus.Completed) PaymentStatus.Paid else PaymentStatus.Pending
    }
    val method = (data["paymentMethod"] as? String)?.let { name ->
        PaymentMethod.entries.find { it.name == name }
    }
    return Session(
        id = id,
        clientId = clientId,
        clientName = clientName,
        date = date,
        time = time,
        durationMinutes = duration,
        notes = data["notes"] as? String ?: "",
        status = status,
        paymentStatus = paymentStatus,
        paymentAmountCents = (data["paymentAmountCents"] as? Number)?.toInt(),
        paymentMethod = method,
    )
}

private fun PracticeSettings.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "welcomeSmsTemplate" to welcomeSmsTemplate,
    "currency" to currency,
    "estimatedFeeDollars" to estimatedFeeDollars,
    "defaultFeeCents" to defaultFeeCents,
    "updatedAt" to FieldValue.serverTimestamp(),
)

private fun Map<String, Any>?.toPracticeSettings(): PracticeSettings {
    if (this == null) return PracticeSettings()
    return PracticeSettings(
        name = this["name"] as? String ?: "Izadi Counselling",
        welcomeSmsTemplate = this["welcomeSmsTemplate"] as? String ?: defaultWelcomeSmsTemplate(),
        currency = this["currency"] as? String ?: "AUD",
        estimatedFeeDollars = (this["estimatedFeeDollars"] as? Number)?.toInt()
            ?: EstimatedSessionFeeDollars,
        defaultFeeCents = (this["defaultFeeCents"] as? Number)?.toInt() ?: 15_000,
    )
}
