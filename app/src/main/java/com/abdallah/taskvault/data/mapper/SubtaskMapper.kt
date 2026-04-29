package com.abdallah.taskvault.data.mapper

import com.abdallah.taskvault.data.local.SubtaskEntity
import com.abdallah.taskvault.domain.model.Subtask

fun SubtaskEntity.toDomain(): Subtask = Subtask(
    id = id,
    todoId = todoId,
    title = title,
    isCompleted = isCompleted,
    position = position,
    createdAtMillis = createdAtMillis
)

fun Subtask.toEntity(): SubtaskEntity = SubtaskEntity(
    id = id,
    todoId = todoId,
    title = title,
    isCompleted = isCompleted,
    position = position,
    createdAtMillis = createdAtMillis
)
