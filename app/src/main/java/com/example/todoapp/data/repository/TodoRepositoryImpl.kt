package com.example.todoapp.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.todoapp.data.local.TodoDao
import com.example.todoapp.data.mapper.toDomain
import com.example.todoapp.data.mapper.toEntity
import com.example.todoapp.domain.model.Todo
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.widget.TodoWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val dao: TodoDao,
    @ApplicationContext private val context: Context
) : TodoRepository {

    override fun getAllTodos(): Flow<List<Todo>> =
        dao.getAllTodos().map { it.map { e -> e.toDomain() } }

    override suspend fun getTodoById(id: Long): Todo? =
        dao.getTodoById(id)?.toDomain()

    override suspend fun insertTodo(todo: Todo): Long {
        val id = dao.insertTodo(todo.toEntity())
        refreshWidget()
        return id
    }

    override suspend fun updateTodo(todo: Todo) {
        dao.updateTodo(todo.toEntity())
        refreshWidget()
    }

    override suspend fun deleteTodo(todo: Todo) {
        dao.softDelete(todo.id, System.currentTimeMillis())
        refreshWidget()
    }

    override suspend fun toggleCompletion(id: Long, isCompleted: Boolean) {
        dao.updateCompletion(id, isCompleted, System.currentTimeMillis())
        refreshWidget()
    }

    override fun getDeletedTodos(): Flow<List<Todo>> =
        dao.getDeletedTodos().map { it.map { e -> e.toDomain() } }

    override suspend fun restoreTodo(id: Long) {
        dao.restore(id)
        refreshWidget()
    }

    override suspend fun permanentlyDeleteTodo(id: Long) {
        dao.permanentlyDelete(id)
        refreshWidget()
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
