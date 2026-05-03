package com.abdallah.taskvault.ui.assign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.model.AssignmentStatus
import com.abdallah.taskvault.domain.model.TaskAssignment
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.abdallah.taskvault.domain.repository.TaskAssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignmentStatsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignments: List<TaskAssignment> = emptyList()
) {
    val total get() = assignments.size
    val countByStatus: Map<AssignmentStatus, Int> get() =
        AssignmentStatus.entries.associateWith { status -> assignments.count { it.status == status } }
    val acceptanceRate: Float get() =
        if (total == 0) 0f
        else assignments.count { it.status != AssignmentStatus.PENDING && it.status != AssignmentStatus.DECLINED }.toFloat() / total
    val completionRate: Float get() =
        if (total == 0) 0f
        else assignments.count { it.status == AssignmentStatus.COMPLETED }.toFloat() / total
    val recentAssignments get() = assignments.sortedByDescending { it.createdAtMillis }.take(5)
}

@HiltViewModel
class AssignmentStatsViewModel @Inject constructor(
    private val taskAssignmentRepository: TaskAssignmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignmentStatsUiState())
    val uiState: StateFlow<AssignmentStatsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = authRepository.currentUser.value?.uid
                    ?: authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Not signed in") }
                    return@launch
                }
                val list = taskAssignmentRepository.getAssignmentsByAssigner(userId)
                _uiState.update { it.copy(isLoading = false, assignments = list) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
            }
        }
    }
}
