package com.abdallah.taskvault.domain.usecase

import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    operator fun invoke(): Flow<List<Todo>> = repository.getAllTodos()
}
