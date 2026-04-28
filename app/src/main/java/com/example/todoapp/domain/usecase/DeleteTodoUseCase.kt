package com.example.todoapp.domain.usecase

import com.example.todoapp.domain.alarm.AlarmScheduler
import com.example.todoapp.domain.model.Todo
import com.example.todoapp.domain.repository.TodoRepository
import javax.inject.Inject

class DeleteTodoUseCase @Inject constructor(
    private val repository: TodoRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(todo: Todo) {
        alarmScheduler.cancel(todo)
        repository.deleteTodo(todo)
    }
}
