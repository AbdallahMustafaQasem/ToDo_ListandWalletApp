package com.abdallah.taskvault.ui.addedit

import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.RecurrenceRule
import com.abdallah.taskvault.domain.model.Subtask
import com.abdallah.taskvault.domain.model.TodoList

data class AddEditTodoUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val dueDateMillis: Long? = null,
    val dueTimeHour: Int? = null,
    val dueTimeMinute: Int? = null,
    val priority: Priority = Priority.NONE,
    val reminderEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val savedEvent: Boolean = false,
    val deletedEvent: Boolean = false,
    val isEditMode: Boolean = false,
    val titleError: String? = null,
    val originalCreatedAtMillis: Long = 0L,
    val recurrenceRule: RecurrenceRule = RecurrenceRule.NONE,
    val listId: Long? = null,
    val subtasks: List<Subtask> = emptyList(),
    val newSubtaskTitle: String = "",
    val availableLists: List<TodoList> = emptyList()
) {
    val isSaveEnabled: Boolean
        get() = title.isNotBlank() && !isSaving

    val combinedDueDateMillis: Long?
        get() {
            val date = dueDateMillis ?: return null
            // If a time has been specified, merge it with the date; otherwise return date at midnight.
            val hour = dueTimeHour ?: return date
            val minute = dueTimeMinute ?: return date
            // Combine date (midnight) with time
            val cal = java.util.Calendar.getInstance().apply {
                timeInMillis = date
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
}
