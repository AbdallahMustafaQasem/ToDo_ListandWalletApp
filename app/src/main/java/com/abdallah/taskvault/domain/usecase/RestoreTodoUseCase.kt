package com.abdallah.taskvault.domain.usecase

import com.abdallah.taskvault.domain.repository.TodoRepository
import javax.inject.Inject

class RestoreTodoUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: Long) = repository.restoreTodo(id)
}
