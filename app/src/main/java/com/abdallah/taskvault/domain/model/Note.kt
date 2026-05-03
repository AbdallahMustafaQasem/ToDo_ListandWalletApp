package com.abdallah.taskvault.domain.model

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String = "#6750A4",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
