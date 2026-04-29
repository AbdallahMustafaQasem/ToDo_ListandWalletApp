package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.SubtaskDao
import com.abdallah.taskvault.data.mapper.toDomain
import com.abdallah.taskvault.data.mapper.toEntity
import com.abdallah.taskvault.domain.model.Subtask
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.repository.SubtaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtaskRepositoryImpl @Inject constructor(
    private val dao: SubtaskDao,
    private val sync: FirebaseSyncRepository
) : SubtaskRepository {

    override fun getSubtasksForTodo(todoId: Long): Flow<List<Subtask>> =
        dao.getSubtasksForTodo(todoId).map { it.map { e -> e.toDomain() } }

    override suspend fun insertSubtask(subtask: Subtask): Long {
        val id = dao.insertSubtask(subtask.toEntity())
        sync.syncSubtask(subtask.copy(id = id))
        return id
    }

    override suspend fun updateSubtask(subtask: Subtask) {
        dao.updateSubtask(subtask.toEntity())
        sync.syncSubtask(subtask)
    }

    override suspend fun deleteSubtask(id: Long) {
        dao.deleteSubtaskById(id)
        sync.deleteSubtaskSync(id)
    }

    override suspend fun toggleSubtaskCompletion(id: Long, isCompleted: Boolean) {
        dao.toggleSubtaskCompletion(id, isCompleted)
    }

    override suspend fun deleteAllForTodo(todoId: Long) =
        dao.deleteAllForTodo(todoId)
}
