package com.abdallah.taskvault.domain.model

data class TodoList(
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#6750A4",
    val icon: String = "📋",
    val createdAtMillis: Long
)
