package com.practice.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

@Serializable
private data class StoredClient(
    val id: String,
    val firstName: String,
    val surname: String,
    val dateOfBirth: String,
    val mobile: String,
    val emergencyContactName: String = "",
    val emergencyContactNumber: String = "",
    val relationship: String? = null,
    val isActive: Boolean,
    val createdAtEpochMs: Long = 0L,
)

@Serializable
private data class StoredSession(
    val id: String,
    val clientId: String,
    val clientName: String,
    val date: String,
    val time: String,
    val durationMinutes: Int,
    val notes: String = "",
    val status: String = SessionStatus.Scheduled.name,
    val paymentStatus: String? = null,
    val paymentAmountCents: Int? = null,
    val paymentMethod: String? = null,
)

@Serializable
private data class StoredClientsFile(
    val seedVersion: Int = 0,
    val clients: List<StoredClient>,
)

@Serializable
private data class StoredSessionsFile(
    val seedVersion: Int = 0,
    val sessions: List<StoredSession>,
)

class LocalStore(context: Context) {
    private val dir = context.filesDir
    private val clientsFile = File(dir, "clients.json")
    private val sessionsFile = File(dir, "sessions.json")
    private val welcomeSmsTemplateFile = File(dir, "welcome_sms_template.txt")

    fun hasClientsFile(): Boolean = clientsFile.exists()

    fun loadClientSeedVersion(): Int {
        if (!clientsFile.exists()) return 0
        return runCatching {
            json.decodeFromString<StoredClientsFile>(clientsFile.readText()).seedVersion
        }.getOrElse { 0 }
    }

    fun loadClients(): List<Client> {
        if (!clientsFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<StoredClientsFile>(clientsFile.readText())
                .clients
                .map { it.toClient() }
        }.getOrElse { emptyList() }
    }

    fun saveClients(clients: List<Client>, seedVersion: Int = ClientSeedVersion) {
        val payload = StoredClientsFile(
            seedVersion = seedVersion,
            clients = clients.map { it.toStored() },
        )
        clientsFile.writeText(json.encodeToString(payload))
    }

    fun loadSessionSeedVersion(): Int {
        if (!sessionsFile.exists()) return 0
        return runCatching {
            json.decodeFromString<StoredSessionsFile>(sessionsFile.readText()).seedVersion
        }.getOrElse { 0 }
    }

    fun loadSessions(): List<Session> {
        if (!sessionsFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<StoredSessionsFile>(sessionsFile.readText())
                .sessions
                .mapNotNull { it.toSessionOrNull() }
        }.getOrElse { emptyList() }
    }

    fun saveSessions(
        sessions: List<Session>,
        seedVersion: Int = loadSessionSeedVersion(),
    ) {
        val payload = StoredSessionsFile(
            seedVersion = seedVersion,
            sessions = sessions.map { it.toStored() },
        )
        sessionsFile.writeText(json.encodeToString(payload))
    }

    fun loadWelcomeSmsTemplate(): String {
        if (!welcomeSmsTemplateFile.exists()) return defaultWelcomeSmsTemplate()
        return runCatching {
            welcomeSmsTemplateFile.readText().trim().ifEmpty { defaultWelcomeSmsTemplate() }
        }.getOrElse { defaultWelcomeSmsTemplate() }
    }

    fun saveWelcomeSmsTemplate(template: String) {
        welcomeSmsTemplateFile.writeText(template.trim().ifEmpty { defaultWelcomeSmsTemplate() })
    }
}

private fun Client.toStored() = StoredClient(
    id = id,
    firstName = firstName,
    surname = surname,
    dateOfBirth = dateOfBirth,
    mobile = mobile,
    emergencyContactName = emergencyContactName,
    emergencyContactNumber = emergencyContactNumber,
    relationship = relationship?.name,
    isActive = isActive,
    createdAtEpochMs = createdAtEpochMs,
)

private fun StoredClient.toClient() = Client(
    id = id,
    firstName = firstName,
    surname = surname,
    dateOfBirth = dateOfBirth,
    mobile = mobile,
    emergencyContactName = emergencyContactName,
    emergencyContactNumber = emergencyContactNumber,
    relationship = relationship?.let { name ->
        EmergencyRelationship.entries.find { it.name == name }
    },
    isActive = isActive,
    createdAtEpochMs = createdAtEpochMs,
)

private fun Session.toStored() = StoredSession(
    id = id,
    clientId = clientId,
    clientName = clientName,
    date = date.toString(),
    time = time.toString(),
    durationMinutes = durationMinutes,
    notes = notes,
    status = status.name,
    paymentStatus = paymentStatus.name,
    paymentAmountCents = paymentAmountCents,
    paymentMethod = paymentMethod?.name,
)

private fun StoredSession.toSessionOrNull(): Session? {
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
    val parsedTime = runCatching { LocalTime.parse(time) }.getOrNull() ?: return null
    val parsedStatus = when (status) {
        "Postponed" -> SessionStatus.Scheduled // legacy status removed; treat as editable scheduled
        else -> SessionStatus.entries.find { it.name == status } ?: SessionStatus.Scheduled
    }
    val parsedPayment = when (paymentStatus) {
        "NotPaid" -> PaymentStatus.Paid // legacy "Outstanding" — sessions are paid at completion
        else -> paymentStatus?.let { name ->
            PaymentStatus.entries.find { it.name == name }
        } ?: when (parsedStatus) {
            SessionStatus.Completed -> PaymentStatus.Paid
            else -> PaymentStatus.Pending
        }
    }
    val parsedMethod = paymentMethod?.let { name ->
        PaymentMethod.entries.find { it.name == name }
    }
    return Session(
        id = id,
        clientId = clientId,
        clientName = clientName,
        date = parsedDate,
        time = parsedTime,
        durationMinutes = durationMinutes,
        notes = notes,
        status = parsedStatus,
        paymentStatus = parsedPayment,
        paymentAmountCents = paymentAmountCents,
        paymentMethod = parsedMethod,
    )
}
