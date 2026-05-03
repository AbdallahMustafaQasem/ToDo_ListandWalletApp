package com.abdallah.taskvault.domain.model

data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#6750A4",
    val emoji: String = "⭐",
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
