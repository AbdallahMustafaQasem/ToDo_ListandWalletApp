package com.abdallah.taskvault.data.mapper

import com.abdallah.taskvault.data.local.TodoEntity
import com.abdallah.taskvault.domain.model.RecurrenceRule
import com.abdallah.taskvault.domain.model.Todo

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
    deletedAtMillis = deletedAtMillis,
    recurrenceRule = RecurrenceRule.fromString(recurrenceRule),
    listId = listId
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
    deletedAtMillis = deletedAtMillis,
    recurrenceRule = if (recurrenceRule == RecurrenceRule.NONE) null else recurrenceRule.name,
    listId = listId
)
