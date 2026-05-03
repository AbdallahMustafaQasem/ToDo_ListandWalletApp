package com.abdallah.taskvault.domain.model

data class Memoir(
    val id: Long = 0,
    val title: String,
    val content: String,
    val mood: String = "😊",
    val dateMillis: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
