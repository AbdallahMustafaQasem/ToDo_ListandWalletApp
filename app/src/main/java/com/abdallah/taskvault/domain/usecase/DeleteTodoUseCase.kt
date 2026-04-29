package com.abdallah.taskvault.domain.usecase

import com.abdallah.taskvault.domain.alarm.AlarmScheduler
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.repository.TodoRepository
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
