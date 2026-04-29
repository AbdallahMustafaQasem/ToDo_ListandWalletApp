package com.abdallah.taskvault.domain.usecase

import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.repository.TodoRepository
import javax.inject.Inject

class GetTodoByIdUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: Long): Todo? = repository.getTodoById(id)
}
