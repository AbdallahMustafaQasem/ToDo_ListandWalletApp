package com.example.todoapp.domain.usecase

import com.example.todoapp.domain.alarm.AlarmScheduler
import com.example.todoapp.domain.model.Todo
import com.example.todoapp.domain.repository.TodoRepository
import javax.inject.Inject

class UpdateTodoUseCase @Inject constructor(
    private val repository: TodoRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(todo: Todo) {
        require(todo.title.isNotBlank()) { "Title must not be blank" }
        require(todo.title.length <= 100) { "Title must not exceed 100 characters" }
        require(todo.description.length <= 500) { "Description must not exceed 500 characters" }

        repository.updateTodo(todo)

        // Cancel any existing alarm and reschedule if needed
        alarmScheduler.cancel(todo)
        if (todo.reminderEnabled && todo.dueDateMillis != null && !todo.isCompleted) {
            alarmScheduler.schedule(todo)
        }
    }
}
