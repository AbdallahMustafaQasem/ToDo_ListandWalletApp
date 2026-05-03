package com.abdallah.taskvault.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.repository.BillRepository
import com.abdallah.taskvault.domain.repository.HabitRepository
import com.abdallah.taskvault.domain.repository.MemoirRepository
import com.abdallah.taskvault.domain.repository.NoteRepository
import com.abdallah.taskvault.domain.repository.PasswordRepository
import com.abdallah.taskvault.domain.repository.TodoRepository
import com.google.firebase.auth.FirebaseUser
import com.abdallah.taskvault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val activeTodoCount: Int = 0,
    val noteCount: Int = 0,
    val memoirCount: Int = 0,
    val passwordCount: Int = 0,
    val habitCount: Int = 0,
    val billsDueSoonCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val noteRepository: NoteRepository,
    private val memoirRepository: MemoirRepository,
    private val passwordRepository: PasswordRepository,
    private val habitRepository: HabitRepository,
    private val billRepository: BillRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            authRepository.currentUser,
            todoRepository.getAllTodos(),
            noteRepository.getNoteCount()
        ) { user, todos, noteCount -> Triple(user, todos, noteCount) },
        combine(
            memoirRepository.getMemoirCount(),
            passwordRepository.getCount(),
            habitRepository.getCount()
        ) { memoirCount, passwordCount, habitCount -> Triple(memoirCount, passwordCount, habitCount) },
        billRepository.getDueSoonCount()
    ) { (user, todos, noteCount), (memoirCount, passwordCount, habitCount), billsDueSoon ->
        DashboardUiState(
            userName = user?.displayName?.substringBefore(" ") ?: "there",
            userPhotoUrl = user?.photoUrl?.toString(),
            activeTodoCount = todos.count { !it.isCompleted && !it.isDeleted },
            noteCount = noteCount,
            memoirCount = memoirCount,
            passwordCount = passwordCount,
            habitCount = habitCount,
            billsDueSoonCount = billsDueSoon
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
