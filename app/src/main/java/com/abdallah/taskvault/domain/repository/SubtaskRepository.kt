package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Subtask
import kotlinx.coroutines.flow.Flow

interface SubtaskRepository {
    fun getSubtasksForTodo(todoId: Long): Flow<List<Subtask>>
    suspend fun insertSubtask(subtask: Subtask): Long
    suspend fun updateSubtask(subtask: Subtask)
    suspend fun deleteSubtask(id: Long)
    suspend fun toggleSubtaskCompletion(id: Long, isCompleted: Boolean)
    suspend fun deleteAllForTodo(todoId: Long)
}
