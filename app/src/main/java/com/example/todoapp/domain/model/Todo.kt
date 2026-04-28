package com.example.todoapp.domain.model

data class Todo(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val dueDateMillis: Long? = null,
    val priority: Priority = Priority.NONE,
    val reminderEnabled: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isDeleted: Boolean = false,
    val deletedAtMillis: Long? = null
)
