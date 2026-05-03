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

data class AssignedToMeUiState(
    val assignments: List<TaskAssignment> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val snackbar: String? = null,
    val filterStatus: AssignmentStatus? = null
)

@HiltViewModel
class AssignedToMeViewModel @Inject constructor(
    private val taskAssignmentRepository: TaskAssignmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignedToMeUiState())
    val uiState: StateFlow<AssignedToMeUiState> = _uiState.asStateFlow()

    init {
        loadAssignments()
    }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = authRepository.currentUser.value?.uid
                    ?: authRepository.getCurrentUserId()
                if (userId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Not signed in") }
                    return@launch
                }
                val assignments = taskAssignmentRepository.getAssignmentsForAssignee(userId)
                _uiState.update { it.copy(assignments = assignments, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    fun updateStatus(assignmentId: String, status: AssignmentStatus) {
        viewModelScope.launch {
            try {
                taskAssignmentRepository.updateStatus(assignmentId, status)
                _uiState.update { it.copy(snackbar = "Status updated") }
                loadAssignments()
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbar = e.message ?: "Failed to update") }
            }
        }
    }

    fun setFilterStatus(status: AssignmentStatus?) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbar = null) }

    val filteredAssignments: StateFlow<List<TaskAssignment>> =
        _uiState.map { state ->
            val filter = state.filterStatus
            if (filter == null) state.assignments
            else state.assignments.filter { it.status == filter }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
