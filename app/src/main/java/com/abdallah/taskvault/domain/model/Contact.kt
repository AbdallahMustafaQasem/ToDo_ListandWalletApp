package com.abdallah.taskvault.domain.model

data class Contact(
    val id: Long = 0,
    val userId: String,
    val displayName: String,
    val role: String = "",
    val avatarColor: String = "#6750A4",
    val addedAtMillis: Long = System.currentTimeMillis()
)
