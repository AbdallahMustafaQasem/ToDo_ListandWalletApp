package com.abdallah.taskvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoListDao {

    @Query("SELECT * FROM todo_lists ORDER BY created_at_millis ASC")
    fun getAllLists(): Flow<List<TodoListEntity>>

    @Query("SELECT * FROM todo_lists WHERE id = :id")
    suspend fun getListById(id: Long): TodoListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TodoListEntity): Long

    @Update
    suspend fun updateList(list: TodoListEntity)

    @Delete
    suspend fun deleteList(list: TodoListEntity)

    @Query("DELETE FROM todo_lists WHERE id = :id")
    suspend fun deleteListById(id: Long)
}
