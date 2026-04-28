package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<Todo>>
    suspend fun getTodoById(id: Long): Todo?
    suspend fun insertTodo(todo: Todo): Long
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todo: Todo)          // soft-delete → moves to trash
    suspend fun toggleCompletion(id: Long, isCompleted: Boolean)

    // Trash
    fun getDeletedTodos(): Flow<List<Todo>>
    suspend fun restoreTodo(id: Long)
    suspend fun permanentlyDeleteTodo(id: Long)
    suspend fun purgeOldDeleted(cutoffMillis: Long)
}
