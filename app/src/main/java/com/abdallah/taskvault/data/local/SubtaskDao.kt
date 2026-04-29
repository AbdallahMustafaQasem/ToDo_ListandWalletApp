package com.abdallah.taskvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {

    @Query("SELECT * FROM subtasks WHERE todo_id = :todoId ORDER BY position ASC, created_at_millis ASC")
    fun getSubtasksForTodo(todoId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE todo_id = :todoId ORDER BY position ASC, created_at_millis ASC")
    suspend fun getSubtasksForTodoOnce(todoId: Long): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE id = :id")
    suspend fun deleteSubtaskById(id: Long)

    @Query("UPDATE subtasks SET is_completed = :isCompleted WHERE id = :id")
    suspend fun toggleSubtaskCompletion(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM subtasks WHERE todo_id = :todoId")
    suspend fun deleteAllForTodo(todoId: Long)
}
