package com.abdallah.taskvault.data.mapper

import com.abdallah.taskvault.data.local.TodoListEntity
import com.abdallah.taskvault.domain.model.TodoList

fun TodoListEntity.toDomain(): TodoList = TodoList(
    id = id,
    name = name,
    colorHex = colorHex,
    icon = icon,
    createdAtMillis = createdAtMillis
)

fun TodoList.toEntity(): TodoListEntity = TodoListEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    icon = icon,
    createdAtMillis = createdAtMillis
)
