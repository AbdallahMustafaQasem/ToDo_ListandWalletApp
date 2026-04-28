package com.example.todoapp.domain.usecase

import com.example.todoapp.domain.alarm.AlarmScheduler
import com.example.todoapp.domain.model.Todo
import com.example.todoapp.domain.repository.TodoRepository
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
