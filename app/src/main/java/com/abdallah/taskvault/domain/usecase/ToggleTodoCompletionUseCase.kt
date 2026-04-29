package com.abdallah.taskvault.domain.usecase

import com.abdallah.taskvault.domain.alarm.AlarmScheduler
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.repository.TodoRepository
import javax.inject.Inject

class ToggleTodoCompletionUseCase @Inject constructor(
    private val repository: TodoRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(todo: Todo, isCompleted: Boolean) {
        repository.toggleCompletion(todo.id, isCompleted)
        if (isCompleted) {
            alarmScheduler.cancel(todo)
        } else if (todo.reminderEnabled && todo.dueDateMillis != null) {
            alarmScheduler.schedule(todo.copy(isCompleted = false))
        }
    }
}
