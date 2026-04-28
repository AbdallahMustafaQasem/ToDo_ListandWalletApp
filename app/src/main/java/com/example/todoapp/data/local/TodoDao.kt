package com.example.todoapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos WHERE is_deleted = 0 ORDER BY " +
           "CASE WHEN due_date_millis IS NULL THEN 1 ELSE 0 END ASC, " +
           "due_date_millis ASC, created_at_millis DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("UPDATE todos SET is_completed = :isCompleted, updated_at_millis = :now WHERE id = :id")
    suspend fun updateCompletion(id: Long, isCompleted: Boolean, now: Long)

    @Query("SELECT * FROM todos WHERE is_deleted = 0 AND is_completed = 0 ORDER BY " +
           "CASE WHEN due_date_millis IS NULL THEN 1 ELSE 0 END ASC, " +
           "due_date_millis ASC LIMIT 3")
    fun getUpcomingTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE reminder_enabled = 1 AND is_completed = 0 AND is_deleted = 0 AND due_date_millis > :now")
    suspend fun getActiveReminders(now: Long): List<TodoEntity>

    // ── Trash ──────────────────────────────────────────────────────────────────

    @Query("UPDATE todos SET is_deleted = 1, deleted_at_millis = :deletedAt, reminder_enabled = 0 WHERE id = :id")
    suspend fun softDelete(id: Long, deletedAt: Long)

    @Query("UPDATE todos SET is_deleted = 0, deleted_at_millis = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun permanentlyDelete(id: Long)

    @Query("SELECT * FROM todos WHERE is_deleted = 1 ORDER BY deleted_at_millis DESC")
    fun getDeletedTodos(): Flow<List<TodoEntity>>

    @Query("DELETE FROM todos WHERE is_deleted = 1 AND deleted_at_millis < :cutoffMillis")
    suspend fun purgeOldDeleted(cutoffMillis: Long)
}
