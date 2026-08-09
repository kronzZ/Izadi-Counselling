package com.practice.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import com.practice.app.data.Client
import com.practice.app.data.ClientSeedVersion
import com.practice.app.data.LocalStore
import com.practice.app.data.PlaceholderClients
import com.practice.app.data.Session
import com.practice.app.data.SessionSeedVersion
import com.practice.app.data.buildPlaceholderSessions

class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalStore(application)

    val clients: SnapshotStateList<Client> = mutableStateListOf()
    val sessions: SnapshotStateList<Session> = mutableStateListOf()

    /** Briefly highlight this client card on the Active list after create. */
    var highlightClientId by mutableStateOf<String?>(null)
        private set

    var welcomeSmsTemplate by mutableStateOf(store.loadWelcomeSmsTemplate())
        private set

    init {
        val existingClients = store.loadClients()
        val existingSessions = store.loadSessions()
        clients.addAll(existingClients)
        sessions.addAll(existingSessions)

        val seedVersion = store.loadClientSeedVersion()
        if (!store.hasClientsFile() || seedVersion < ClientSeedVersion) {
            val existingIds = clients.map { it.id }.toSet()
            val missingSeedClients = PlaceholderClients.filter { it.id !in existingIds }
            if (missingSeedClients.isNotEmpty()) {
                clients.addAll(missingSeedClients)
            }
            // Persist merged list + bump seed version. Never clears sessions.
            store.saveClients(clients.toList(), seedVersion = ClientSeedVersion)
        }

        val sessionSeedVersion = store.loadSessionSeedVersion()
        if (sessionSeedVersion < SessionSeedVersion) {
            val seedSessions = buildPlaceholderSessions()
            val seedIds = seedSessions.map { it.id }.toSet()
            // Refresh demo seed rows only; leave any user-created sessions alone.
            sessions.removeAll { it.id in seedIds || it.id.startsWith("seed-session-") }
            sessions.addAll(seedSessions)
            store.saveSessions(sessions.toList(), seedVersion = SessionSeedVersion)
        }
    }

    fun updateClient(updated: Client) {
        val index = clients.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            clients[index] = updated
            persistClients()
        }
    }

    fun toggleClientActive(clientId: String) {
        val index = clients.indexOfFirst { it.id == clientId }
        if (index >= 0) {
            val current = clients[index]
            clients[index] = current.copy(isActive = !current.isActive)
            persistClients()
        }
    }

    fun addClient(client: Client) {
        clients.add(0, client)
        highlightClientId = client.id
        persistClients()
    }

    fun clearHighlightClientId() {
        highlightClientId = null
    }

    fun updateWelcomeSmsTemplate(template: String) {
        val saved = template.trim().ifEmpty { store.loadWelcomeSmsTemplate() }
        welcomeSmsTemplate = saved
        store.saveWelcomeSmsTemplate(saved)
    }

    fun deleteClient(clientId: String) {
        clients.removeAll { it.id == clientId }
        sessions.removeAll { it.clientId == clientId }
        persistClients()
        persistSessions()
    }

    fun addSession(session: Session) {
        sessions.add(session)
        persistSessions()
    }

    fun updateSession(updated: Session) {
        val index = sessions.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            sessions[index] = updated
            persistSessions()
        }
    }

    private fun persistClients() {
        store.saveClients(clients.toList())
    }

    private fun persistSessions() {
        store.saveSessions(sessions.toList())
    }
}
