package com.abdallah.taskvault.domain.model

data class WalletTransaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String,
    val categoryIcon: String,
    val dateMillis: Long,
    val notes: String = ""
)
