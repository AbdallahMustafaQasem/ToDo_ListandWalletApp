package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getCommentsForTodo(todoId: Long): Flow<List<Comment>>
    suspend fun addComment(comment: Comment): Long
    suspend fun deleteComment(comment: Comment)
    suspend fun deleteAllForTodo(todoId: Long)
}
