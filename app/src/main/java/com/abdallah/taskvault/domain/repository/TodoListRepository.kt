package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.TodoList
import kotlinx.coroutines.flow.Flow

interface TodoListRepository {
    fun getAllLists(): Flow<List<TodoList>>
    suspend fun getListById(id: Long): TodoList?
    suspend fun insertList(list: TodoList): Long
    suspend fun updateList(list: TodoList)
    suspend fun deleteList(id: Long)
}
