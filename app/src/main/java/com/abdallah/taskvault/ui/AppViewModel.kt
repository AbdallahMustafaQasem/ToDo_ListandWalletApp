package com.abdallah.taskvault.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.abdallah.taskvault.domain.repository.BillRepository
import com.abdallah.taskvault.domain.repository.HabitRepository
import com.abdallah.taskvault.domain.repository.MemoirRepository
import com.abdallah.taskvault.domain.repository.NoteRepository
import com.abdallah.taskvault.domain.repository.PasswordRepository
import com.abdallah.taskvault.domain.repository.TodoListRepository
import com.abdallah.taskvault.domain.repository.TodoRepository
import com.abdallah.taskvault.domain.repository.WalletRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

enum class SyncState { IDLE, SYNCING, DONE, ERROR }
enum class SyncStep  { IDLE, RESTORE, UPLOAD }
enum class DeleteAccountState { IDLE, DELETING, DONE, ERROR }

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: FirebaseSyncRepository,
    private val todoRepository: TodoRepository,
    private val todoListRepository: TodoListRepository,
    private val walletRepository: WalletRepository,
    private val noteRepository: NoteRepository,
    private val memoirRepository: MemoirRepository,
    private val passwordRepository: PasswordRepository,
    private val habitRepository: HabitRepository,
    private val billRepository: BillRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    companion object { private const val TAG = "TaskVault" }

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _syncStep = MutableStateFlow(SyncStep.IDLE)
    val syncStep: StateFlow<SyncStep> = _syncStep.asStateFlow()

    private val _deleteAccountState = MutableStateFlow(DeleteAccountState.IDLE)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState.asStateFlow()

    private val _deleteAccountError = MutableStateFlow<String?>(null)
    val deleteAccountError: StateFlow<String?> = _deleteAccountError.asStateFlow()

    fun onSignedIn() {
        viewModelScope.launch {
            Log.d(TAG, "[AppViewModel] onSignedIn() uid=${authRepository.getCurrentUserId()}")
            _syncState.value = SyncState.SYNCING

            try {
                // Step 1 — pull cloud data into Room
                _syncStep.value = SyncStep.RESTORE
                Log.d(TAG, "[AppViewModel] Starting restoreFromCloud()")
                val restored = syncRepository.restoreFromCloud()
                Log.d(TAG, "[AppViewModel] restoreFromCloud() result=$restored")

                // Step 2 — push Room data to cloud (covers first-time users)
                Log.d(TAG, "[AppViewModel] syncAll() start")
                _syncState.value = SyncState.SYNCING
                _syncStep.value = SyncStep.UPLOAD
                viewModelScope.launch {
                    try {
                        val todos = todoRepository.getAllTodos().first()
                        val lists = todoListRepository.getAllLists().first()
                        val transactions = walletRepository.getTransactions().first()
                        val categories = walletRepository.getCategories().first()
                        val budget = walletRepository.getBudget().first()
                        val notes = noteRepository.getAllNotes().first()
                        val memoirs = memoirRepository.getAllMemoirs().first()
                        val passwords = passwordRepository.getAll().first()
                        val habits = habitRepository.getAll().first()
                        val bills = billRepository.getAll().first()
                        syncRepository.syncAll(todos, lists, transactions, categories, budget, notes, memoirs, passwords, habits, bills)
                        Log.d(TAG, "[AppViewModel] syncAll() complete")
                        analytics.logUserSignedIn()

                        _syncState.value = SyncState.DONE
                    } catch (e: Exception) {
                        Log.e(TAG, "[AppViewModel] syncAll() failed", e)
                        _syncState.value = SyncState.ERROR
                    }
                }

                _syncStep.value = SyncStep.IDLE
            } catch (e: Exception) {
                Log.e(TAG, "[AppViewModel] Sync FAILED: ${e.message}", e)
                _syncStep.value = SyncStep.IDLE
                _syncState.value = SyncState.ERROR
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            Log.d(TAG, "[AppViewModel] signOut() called")
            analytics.logUserSignedOut()
            _syncState.value = SyncState.IDLE
            authRepository.signOut()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            Log.d(TAG, "[AppViewModel] deleteAccount() called")
            _deleteAccountState.value = DeleteAccountState.DELETING
            _deleteAccountError.value = null
            try {
                syncRepository.deleteAllCloudData()
                val firebaseUser = authRepository.currentUser.value
                firebaseUser?.delete()?.await()
                _syncState.value = SyncState.IDLE
                _deleteAccountState.value = DeleteAccountState.DONE
                authRepository.signOut()
                Log.d(TAG, "[AppViewModel] deleteAccount() DONE")
            } catch (e: Exception) {
                Log.e(TAG, "[AppViewModel] deleteAccount() FAILED: ${e.message}", e)
                _deleteAccountError.value = e.message
                _deleteAccountState.value = DeleteAccountState.ERROR
            }
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = DeleteAccountState.IDLE
        _deleteAccountError.value = null
    }
}
