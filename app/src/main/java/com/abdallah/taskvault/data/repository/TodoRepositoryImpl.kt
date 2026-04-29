package com.abdallah.taskvault.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.abdallah.taskvault.data.local.TodoDao
import com.abdallah.taskvault.data.mapper.toDomain
import com.abdallah.taskvault.data.mapper.toEntity
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.repository.TodoRepository
import com.abdallah.taskvault.widget.TodoWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val dao: TodoDao,
    private val sync: FirebaseSyncRepository,
    @ApplicationContext private val context: Context
) : TodoRepository {

    override fun getAllTodos(): Flow<List<Todo>> =
        dao.getAllTodos().map { it.map { e -> e.toDomain() } }

    override suspend fun getTodoById(id: Long): Todo? =
        dao.getTodoById(id)?.toDomain()

    override suspend fun insertTodo(todo: Todo): Long {
        val id = dao.insertTodo(todo.toEntity())
        refreshWidget()
        dao.getTodoById(id)?.toDomain()?.let { sync.syncTodo(it) }
        return id
    }

    override suspend fun updateTodo(todo: Todo) {
        dao.updateTodo(todo.toEntity())
        refreshWidget()
        sync.syncTodo(todo)
    }

    override suspend fun deleteTodo(todo: Todo) {
        dao.softDelete(todo.id, System.currentTimeMillis())
        refreshWidget()
        dao.getTodoById(todo.id)?.toDomain()?.let { sync.syncTodo(it) }
    }

    override suspend fun toggleCompletion(id: Long, isCompleted: Boolean) {
        dao.updateCompletion(id, isCompleted, System.currentTimeMillis())
        refreshWidget()
        dao.getTodoById(id)?.toDomain()?.let { sync.syncTodo(it) }
    }

    override fun getDeletedTodos(): Flow<List<Todo>> =
        dao.getDeletedTodos().map { it.map { e -> e.toDomain() } }

    override suspend fun restoreTodo(id: Long) {
        dao.restore(id)
        refreshWidget()
        dao.getTodoById(id)?.toDomain()?.let { sync.syncTodo(it) }
    }

    override suspend fun permanentlyDeleteTodo(id: Long) {
        dao.permanentlyDelete(id)
        refreshWidget()
        sync.deleteTodoSync(id)
    }

    override suspend fun purgeOldDeleted(cutoffMillis: Long) {
        dao.purgeOldDeleted(cutoffMillis)
    }

    private suspend fun refreshWidget() {
        try {
            TodoWidget().updateAll(context)
        } catch (_: Exception) {
        }
    }
}
