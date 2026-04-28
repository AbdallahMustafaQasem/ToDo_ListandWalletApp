package com.example.todoapp.domain.usecase

import com.example.todoapp.domain.repository.TodoRepository
import javax.inject.Inject

class PermanentlyDeleteTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: Long) = repository.permanentlyDeleteTodo(id)
}
