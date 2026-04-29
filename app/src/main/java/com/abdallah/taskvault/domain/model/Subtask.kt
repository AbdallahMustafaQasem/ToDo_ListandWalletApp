package com.abdallah.taskvault.domain.model

data class Subtask(
    val id: Long = 0,
    val todoId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val position: Int = 0,
    val createdAtMillis: Long
)
