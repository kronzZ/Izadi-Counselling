package com.practice.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.practice.app.data.Client
import com.practice.app.data.FirestoreRepository
import com.practice.app.data.LocalStore
import com.practice.app.data.Session
import com.practice.app.data.defaultWelcomeSmsTemplate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    private val localStore = LocalStore(application)
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    val clients: SnapshotStateList<Client> = mutableStateListOf()
    val sessions: SnapshotStateList<Session> = mutableStateListOf()

    var highlightClientId by mutableStateOf<String?>(null)
        private set

    var welcomeSmsTemplate by mutableStateOf(defaultWelcomeSmsTemplate())
        private set

    var currentUser by mutableStateOf(auth.currentUser)
        private set

    var isCloudReady by mutableStateOf(false)
        private set

    var authError by mutableStateOf<String?>(null)

    var cloudError by mutableStateOf<String?>(null)

    /** True when on-device JSON exists and has not been imported for this uid. */
    var pendingLocalImport by mutableStateOf(false)
        private set

    var isImporting by mutableStateOf(false)
        private set

    private var listenersJob: Job? = null
    private val prefs = application.getSharedPreferences("izadi_cloud", 0)

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        currentUser = firebaseAuth.currentUser
        val user = firebaseAuth.currentUser
        if (user != null) {
            startCloud(user)
        } else {
            stopCloud()
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        currentUser?.let { startCloud(it) }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        listenersJob?.cancel()
        super.onCleared()
    }

    fun signIn(email: String, password: String) {
        authError = null
        viewModelScope.launch {
            runCatching {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
            }.onFailure {
                authError = it.message ?: "Sign in failed"
            }
        }
    }

    fun signUp(email: String, password: String) {
        authError = null
        viewModelScope.launch {
            runCatching {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
            }.onFailure {
                authError = it.message ?: "Could not create account"
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun dismissLocalImport() {
        currentUser?.uid?.let { markImported(it) }
        pendingLocalImport = false
    }

    fun importLocalDataToCloud() {
        val uid = currentUser?.uid ?: return
        if (isImporting) return
        isImporting = true
        viewModelScope.launch {
            runCatching {
                val localClients = localStore.loadClients()
                val localSessions = localStore.loadSessions()
                val template = localStore.loadWelcomeSmsTemplate()
                repository.importLocalData(uid, localClients, localSessions, template)
                markImported(uid)
                pendingLocalImport = false
            }.onFailure {
                cloudError = it.message ?: "Import failed"
            }
            isImporting = false
        }
    }

    fun updateClient(updated: Client) {
        val index = clients.indexOfFirst { it.id == updated.id }
        if (index >= 0) clients[index] = updated
        val uid = currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.upsertClient(uid, updated) }
                .onFailure { cloudError = it.message }
        }
    }

    fun toggleClientActive(clientId: String) {
        val index = clients.indexOfFirst { it.id == clientId }
        if (index >= 0) {
            val current = clients[index]
            updateClient(current.copy(isActive = !current.isActive))
        }
    }

    fun addClient(client: Client) {
        clients.add(0, client)
        highlightClientId = client.id
        val uid = currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.upsertClient(uid, client) }
                .onFailure { cloudError = it.message }
        }
    }

    fun clearHighlightClientId() {
        highlightClientId = null
    }

    fun updateWelcomeSmsTemplate(template: String) {
        val saved = template.trim().ifEmpty { defaultWelcomeSmsTemplate() }
        welcomeSmsTemplate = saved
        val uid = currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.updateWelcomeSmsTemplate(uid, saved) }
                .onFailure { cloudError = it.message }
        }
    }

    fun deleteClient(clientId: String) {
        clients.removeAll { it.id == clientId }
        sessions.removeAll { it.clientId == clientId }
        val uid = currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.deleteClient(uid, clientId) }
                .onFailure { cloudError = it.message }
        }
    }

    fun addSession(session: Session) {
        sessions.add(session)
        val uid = currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.upsertSession(uid, session) }
                .onFailure { cloudError = it.message }
        }
    }

    fun updateSession(updated: Session) {
        val index = sessions.indexOfFirst { it.id == updated.id }
        if (index >= 0) sessions[index] = updated
        val uid = currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching { repository.upsertSession(uid, updated) }
                .onFailure { cloudError = it.message }
        }
    }

    private fun startCloud(user: FirebaseUser) {
        listenersJob?.cancel()
        isCloudReady = false
        cloudError = null
        pendingLocalImport = hasUnimportedLocalData(user.uid)
        listenersJob = viewModelScope.launch {
            runCatching { repository.ensurePracticeDocument(user.uid) }
                .onFailure { cloudError = it.message }

            launch {
                repository.observeClients(user.uid).collectLatest { remote ->
                    clients.clear()
                    clients.addAll(remote)
                    isCloudReady = true
                }
            }
            launch {
                repository.observeSessions(user.uid).collectLatest { remote ->
                    sessions.clear()
                    sessions.addAll(remote)
                    isCloudReady = true
                }
            }
            launch {
                repository.observeSettings(user.uid).collectLatest { settings ->
                    welcomeSmsTemplate = settings.welcomeSmsTemplate
                }
            }
        }
    }

    private fun stopCloud() {
        listenersJob?.cancel()
        listenersJob = null
        clients.clear()
        sessions.clear()
        welcomeSmsTemplate = defaultWelcomeSmsTemplate()
        highlightClientId = null
        isCloudReady = false
        pendingLocalImport = false
    }

    private fun importKey(uid: String) = "imported_$uid"

    private fun markImported(uid: String) {
        prefs.edit().putBoolean(importKey(uid), true).apply()
    }

    private fun hasUnimportedLocalData(uid: String): Boolean {
        if (prefs.getBoolean(importKey(uid), false)) return false
        val hasClients = localStore.hasClientsFile() && localStore.loadClients().isNotEmpty()
        val hasSessions = localStore.loadSessions().isNotEmpty()
        return hasClients || hasSessions
    }
}
