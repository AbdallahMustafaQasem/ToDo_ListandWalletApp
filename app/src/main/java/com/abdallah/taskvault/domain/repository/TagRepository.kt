package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun insertTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
    fun getTagsForTodo(todoId: Long): Flow<List<Tag>>
    suspend fun getTagsForTodoOnce(todoId: Long): List<Tag>
    suspend fun addTagToTodo(todoId: Long, tagId: Long)
    suspend fun removeTagFromTodo(todoId: Long, tagId: Long)
    suspend fun setTagsForTodo(todoId: Long, tagIds: List<Long>)
}
