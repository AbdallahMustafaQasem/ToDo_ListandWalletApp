package com.example.todoapp.data.mapper

import com.example.todoapp.data.local.TodoEntity
import com.example.todoapp.domain.model.Todo

fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDateMillis = dueDateMillis,
    priority = priority,
    reminderEnabled = reminderEnabled,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isDeleted = isDeleted,
    deletedAtMillis = deletedAtMillis
)

fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDateMillis = dueDateMillis,
    priority = priority,
    reminderEnabled = reminderEnabled,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isDeleted = isDeleted,
    deletedAtMillis = deletedAtMillis
)
