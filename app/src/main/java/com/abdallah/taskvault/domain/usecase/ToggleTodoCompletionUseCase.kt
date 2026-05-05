package com.abdallah.taskvault.domain.usecase

import com.abdallah.taskvault.domain.alarm.AlarmScheduler
import com.abdallah.taskvault.domain.model.RecurrenceRule
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.repository.TodoRepository
import java.util.Calendar
import javax.inject.Inject

class ToggleTodoCompletionUseCase @Inject constructor(
    private val repository: TodoRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(todo: Todo, isCompleted: Boolean) {
        repository.toggleCompletion(todo.id, isCompleted)
        if (isCompleted) {
            alarmScheduler.cancel(todo)
            // Auto-reschedule: create next occurrence for recurring tasks
            if (todo.recurrenceRule != RecurrenceRule.NONE && todo.dueDateMillis != null) {
                val nextDue = computeNextDueDate(todo.dueDateMillis, todo.recurrenceRule)
                val now = System.currentTimeMillis()
                val nextTodo = todo.copy(
                    id = 0,
                    isCompleted = false,
                    dueDateMillis = nextDue,
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
                val newId = repository.insertTodo(nextTodo)
                if (nextTodo.reminderEnabled) {
                    alarmScheduler.schedule(nextTodo.copy(id = newId))
                }
            }
        } else if (todo.reminderEnabled && todo.dueDateMillis != null) {
            alarmScheduler.schedule(todo.copy(isCompleted = false))
        }
    }

    private fun computeNextDueDate(currentDueMillis: Long, rule: RecurrenceRule): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDueMillis }
        when (rule) {
            RecurrenceRule.DAILY   -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurrenceRule.WEEKLY  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceRule.MONTHLY -> cal.add(Calendar.MONTH, 1)
            RecurrenceRule.NONE    -> { /* no-op */ }
        }
        // If the computed date is still in the past, advance until it's in the future
        val now = System.currentTimeMillis()
        while (cal.timeInMillis <= now) {
            when (rule) {
                RecurrenceRule.DAILY   -> cal.add(Calendar.DAY_OF_YEAR, 1)
                RecurrenceRule.WEEKLY  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                RecurrenceRule.MONTHLY -> cal.add(Calendar.MONTH, 1)
                RecurrenceRule.NONE    -> break
            }
        }
        return cal.timeInMillis
    }
}
