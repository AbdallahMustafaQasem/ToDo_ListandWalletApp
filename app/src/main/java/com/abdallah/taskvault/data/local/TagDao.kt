package com.abdallah.taskvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT t.* FROM tags t INNER JOIN todo_tags tt ON t.id = tt.tag_id WHERE tt.todo_id = :todoId")
    fun getTagsForTodo(todoId: Long): Flow<List<TagEntity>>

    @Query("SELECT t.* FROM tags t INNER JOIN todo_tags tt ON t.id = tt.tag_id WHERE tt.todo_id = :todoId")
    suspend fun getTagsForTodoOnce(todoId: Long): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToTodo(crossRef: TodoTagCrossRef)

    @Delete
    suspend fun removeTagFromTodo(crossRef: TodoTagCrossRef)

    @Query("DELETE FROM todo_tags WHERE todo_id = :todoId")
    suspend fun removeAllTagsFromTodo(todoId: Long)
}
