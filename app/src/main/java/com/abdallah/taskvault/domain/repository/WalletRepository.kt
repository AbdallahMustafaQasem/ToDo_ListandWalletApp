package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.WalletBudget
import com.abdallah.taskvault.domain.model.WalletCategory
import com.abdallah.taskvault.domain.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getTransactions(): Flow<List<WalletTransaction>>
    fun getCategories(): Flow<List<WalletCategory>>
    fun getBudget(): Flow<WalletBudget?>
    suspend fun upsertTransaction(transaction: WalletTransaction)
    suspend fun deleteTransaction(id: Long)
    suspend fun upsertCategory(category: WalletCategory)
    suspend fun deleteCategory(id: Long)
    suspend fun setMonthlyBudget(amount: Double)
    suspend fun seedDefaultCategories()
}
