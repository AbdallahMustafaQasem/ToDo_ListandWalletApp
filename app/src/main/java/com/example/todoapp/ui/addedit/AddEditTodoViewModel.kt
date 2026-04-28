package com.example.todoapp.ui.addedit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.R
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.Todo
import com.example.todoapp.domain.usecase.AddTodoUseCase
import com.example.todoapp.domain.usecase.DeleteTodoUseCase
import com.example.todoapp.domain.usecase.GetTodoByIdUseCase
import com.example.todoapp.domain.usecase.UpdateTodoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTodoByIdUseCase: GetTodoByIdUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val todoId: Long = savedStateHandle.get<Long>("todoId") ?: 0L

    private val _uiState = MutableStateFlow(AddEditTodoUiState())
    val uiState: StateFlow<AddEditTodoUiState> = _uiState.asStateFlow()

    init {
        if (todoId != 0L) {
            loadTodo(todoId)
        }
    }

    private fun loadTodo(id: Long) {
        viewModelScope.launch {
            val todo = getTodoByIdUseCase(id) ?: return@launch
            val cal = Calendar.getInstance()
            todo.dueDateMillis?.let { millis ->
                cal.timeInMillis = millis
            }
            _uiState.update {
                it.copy(
                    id = todo.id,
                    title = todo.title,
                    description = todo.description,
                    dueDateMillis = todo.dueDateMillis?.let { millis ->
                        // Store date-only (midnight) part
                        Calendar.getInstance().apply {
                            timeInMillis = millis
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    },
                    dueTimeHour = todo.dueDateMillis?.let { cal.get(Calendar.HOUR_OF_DAY) },
                    dueTimeMinute = todo.dueDateMillis?.let { cal.get(Calendar.MINUTE) },
                    priority = todo.priority,
                    reminderEnabled = todo.reminderEnabled,
                    isEditMode = true,
                    originalCreatedAtMillis = todo.createdAtMillis
                )
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title.take(100), titleError = null) }
    }

    fun onDescriptionChanged(desc: String) {
        _uiState.update { it.copy(description = desc.take(500)) }
    }

    fun onDueDateChanged(millis: Long?) {
        _uiState.update { state ->
            state.copy(
                dueDateMillis = millis,
                // Reset time if date cleared
                dueTimeHour = if (millis == null) null else state.dueTimeHour,
                dueTimeMinute = if (millis == null) null else state.dueTimeMinute,
                reminderEnabled = if (millis == null) false else state.reminderEnabled
            )
        }
    }

    fun onDueTimeChanged(hour: Int, minute: Int) {
        _uiState.update { it.copy(dueTimeHour = hour, dueTimeMinute = minute) }
    }

    fun onPriorityChanged(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onReminderToggled(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = context.getString(R.string.validation_title_required)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val dueDateMillis = state.combinedDueDateMillis
            val todo = Todo(
                id = state.id,
                title = state.title.trim(),
                description = state.description.trim(),
                isCompleted = false,
                dueDateMillis = dueDateMillis,
                priority = state.priority,
                reminderEnabled = state.reminderEnabled && dueDateMillis != null,
                createdAtMillis = if (state.isEditMode) state.originalCreatedAtMillis else now,
                updatedAtMillis = now
            )

            try {
                if (state.isEditMode) {
                    updateTodoUseCase(todo)
                } else {
                    addTodoUseCase(todo)
                }
                _uiState.update { it.copy(isSaving = false, savedEvent = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onDelete() {
        val state = _uiState.value
        if (!state.isEditMode) return
        viewModelScope.launch {
            val todo = getTodoByIdUseCase(state.id) ?: return@launch
            deleteTodoUseCase(todo)
            _uiState.update { it.copy(deletedEvent = true) }
        }
    }

    fun consumeSavedEvent() {
        _uiState.update { it.copy(savedEvent = false) }
    }

    fun consumeDeletedEvent() {
        _uiState.update { it.copy(deletedEvent = false) }
    }
}
