package com.abdallah.taskvault.domain.model

data class Bill(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDay: Int,
    val category: String = "Other",
    val notes: String = "",
    val isPaid: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderDaysBefore: Int = 1,
    val nextDueDateMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)
