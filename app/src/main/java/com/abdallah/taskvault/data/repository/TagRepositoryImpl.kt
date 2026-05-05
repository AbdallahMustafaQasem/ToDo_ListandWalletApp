package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.TagDao
import com.abdallah.taskvault.data.local.TagEntity
import com.abdallah.taskvault.data.local.TodoTagCrossRef
import com.abdallah.taskvault.data.local.toEntity
import com.abdallah.taskvault.domain.model.Tag
import com.abdallah.taskvault.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val dao: TagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insertTag(tag: Tag): Long =
        dao.insert(tag.toEntity())

    override suspend fun updateTag(tag: Tag) =
        dao.update(tag.toEntity())

    override suspend fun deleteTag(tag: Tag) =
        dao.delete(tag.toEntity())

    override fun getTagsForTodo(todoId: Long): Flow<List<Tag>> =
        dao.getTagsForTodo(todoId).map { list -> list.map { it.toDomain() } }

    override suspend fun getTagsForTodoOnce(todoId: Long): List<Tag> =
        dao.getTagsForTodoOnce(todoId).map { it.toDomain() }

    override suspend fun addTagToTodo(todoId: Long, tagId: Long) =
        dao.addTagToTodo(TodoTagCrossRef(todoId, tagId))

    override suspend fun removeTagFromTodo(todoId: Long, tagId: Long) =
        dao.removeTagFromTodo(TodoTagCrossRef(todoId, tagId))

    override suspend fun setTagsForTodo(todoId: Long, tagIds: List<Long>) {
        dao.removeAllTagsFromTodo(todoId)
        tagIds.forEach { tagId -> dao.addTagToTodo(TodoTagCrossRef(todoId, tagId)) }
    }
}
