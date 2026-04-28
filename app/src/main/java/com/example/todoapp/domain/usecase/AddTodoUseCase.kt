package com.example.todoapp.domain.usecase

import com.example.todoapp.domain.alarm.AlarmScheduler
import com.example.todoapp.domain.model.Todo
import com.example.todoapp.domain.repository.TodoRepository
import javax.inject.Inject

class AddTodoUseCase @Inject constructor(
    private val repository: TodoRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(todo: Todo): Long {
        require(todo.title.isNotBlank()) { "Title must not be blank" }
        require(todo.title.length <= 100) { "Title must not exceed 100 characters" }
        require(todo.description.length <= 500) { "Description must not exceed 500 characters" }

        val newId = repository.insertTodo(todo)
        val savedTodo = todo.copy(id = newId)

        if (savedTodo.reminderEnabled && savedTodo.dueDateMillis != null) {
            alarmScheduler.schedule(savedTodo)
        }

        return newId
    }
}
