package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.TodoListDao
import com.abdallah.taskvault.data.mapper.toDomain
import com.abdallah.taskvault.data.mapper.toEntity
import com.abdallah.taskvault.domain.model.TodoList
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.repository.TodoListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoListRepositoryImpl @Inject constructor(
    private val dao: TodoListDao,
    private val sync: FirebaseSyncRepository
) : TodoListRepository {

    override fun getAllLists(): Flow<List<TodoList>> =
        dao.getAllLists().map { it.map { e -> e.toDomain() } }

    override suspend fun getListById(id: Long): TodoList? =
        dao.getListById(id)?.toDomain()

    override suspend fun insertList(list: TodoList): Long {
        val id = dao.insertList(list.toEntity())
        dao.getListById(id)?.toDomain()?.let { sync.syncTodoList(it) }
        return id
    }

    override suspend fun updateList(list: TodoList) {
        dao.updateList(list.toEntity())
        sync.syncTodoList(list)
    }

    override suspend fun deleteList(id: Long) {
        dao.deleteListById(id)
        sync.deleteTodoListSync(id)
    }
}
