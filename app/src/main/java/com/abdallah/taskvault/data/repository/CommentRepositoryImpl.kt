package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.CommentDao
import com.abdallah.taskvault.data.local.CommentEntity
import com.abdallah.taskvault.domain.model.Comment
import com.abdallah.taskvault.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val dao: CommentDao
) : CommentRepository {

    override fun getCommentsForTodo(todoId: Long): Flow<List<Comment>> =
        dao.getForTodo(todoId).map { list ->
            list.map { Comment(id = it.id, todoId = it.todoId, authorName = it.authorName, text = it.text, timestampMillis = it.timestampMillis) }
        }

    override suspend fun addComment(comment: Comment): Long =
        dao.insert(CommentEntity(id = comment.id, todoId = comment.todoId, authorName = comment.authorName, text = comment.text, timestampMillis = comment.timestampMillis))

    override suspend fun deleteComment(comment: Comment) =
        dao.delete(CommentEntity(id = comment.id, todoId = comment.todoId, authorName = comment.authorName, text = comment.text, timestampMillis = comment.timestampMillis))

    override suspend fun deleteAllForTodo(todoId: Long) =
        dao.deleteAllForTodo(todoId)
}
