package com.abdallah.taskvault.domain.model

data class Comment(
    val id: Long = 0,
    val todoId: Long,
    val authorName: String = "",
    val text: String,
    val timestampMillis: Long
)
