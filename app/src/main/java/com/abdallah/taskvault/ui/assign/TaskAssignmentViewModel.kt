package com.abdallah.taskvault.ui.assign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.model.AssignmentStatus
import com.abdallah.taskvault.domain.model.Contact
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.TaskAssignment
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.abdallah.taskvault.domain.repository.ContactRepository
import com.abdallah.taskvault.domain.repository.TaskAssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskAssignmentUiState(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.NONE,
    val dueDateMillis: Long? = null,
    val contacts: List<Contact> = emptyList(),
    val selectedContacts: Set<Long> = emptySet(),
    val manualUserId: String = "",
    val showContactPicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val titleError: Boolean = false,
    val assigneeError: Boolean = false
)

@HiltViewModel
class TaskAssignmentViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val taskAssignmentRepository: TaskAssignmentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskAssignmentUiState())
    val uiState: StateFlow<TaskAssignmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            contactRepository.getAll().collect { list ->
                _uiState.update { it.copy(contacts = list) }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = false) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onPriorityChange(p: Priority) = _uiState.update { it.copy(priority = p) }
    fun onDueDateChange(millis: Long?) = _uiState.update { it.copy(dueDateMillis = millis, showDatePicker = false) }
    fun onManualUserIdChange(v: String) = _uiState.update { it.copy(manualUserId = v) }
    fun showDatePicker() = _uiState.update { it.copy(showDatePicker = true) }
    fun hideDatePicker() = _uiState.update { it.copy(showDatePicker = false) }
    fun toggleContactPicker() = _uiState.update { it.copy(showContactPicker = !it.showContactPicker) }

    fun toggleContactSelection(contactId: Long) {
        _uiState.update { state ->
            val current = state.selectedContacts.toMutableSet()
            if (current.contains(contactId)) current.remove(contactId) else current.add(contactId)
            state.copy(selectedContacts = current, assigneeError = false)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearForm() {
        _uiState.update {
            TaskAssignmentUiState(contacts = it.contacts)
        }
    }

    fun submit() {
        val state = _uiState.value

        // Validation
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        if (state.selectedContacts.isEmpty() && state.manualUserId.isBlank()) {
            _uiState.update { it.copy(assigneeError = true, error = "Select at least one assignee") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val currentUser = authRepository.currentUser.value
                val assignerId = currentUser?.uid ?: authRepository.getCurrentUserId() ?: "unknown"
                val assignerName = currentUser?.displayName ?: "Unknown"

                val selectedContacts = state.contacts.filter { state.selectedContacts.contains(it.id) }
                val assigneeIds = selectedContacts.map { it.userId }.toMutableList()
                val assigneeNames = selectedContacts.map { it.displayName }.toMutableList()

                // Add manual user if provided
                if (state.manualUserId.isNotBlank()) {
                    val manualId = state.manualUserId.trim()
                    if (!assigneeIds.contains(manualId)) {
                        assigneeIds.add(manualId)
                        assigneeNames.add(manualId)
                    }
                }

                val assignment = TaskAssignment(
                    title = state.title.trim(),
                    description = state.description.trim(),
                    priority = state.priority,
                    dueDateMillis = state.dueDateMillis,
                    assignerId = assignerId,
                    assignerName = assignerName,
                    assigneeIds = assigneeIds,
                    assigneeNames = assigneeNames,
                    status = AssignmentStatus.PENDING
                )

                taskAssignmentRepository.assignTask(assignment)
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Failed to assign task") }
            }
        }
    }
}
