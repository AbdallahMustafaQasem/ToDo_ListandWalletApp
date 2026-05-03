package com.abdallah.taskvault.domain.model

data class Password(
    val id: Long = 0,
    val title: String,
    val username: String = "",
    val password: String,
    val url: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
