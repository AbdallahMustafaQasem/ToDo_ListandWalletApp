package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.WalletBudget
import com.example.todoapp.domain.model.WalletCategory
import com.example.todoapp.domain.model.WalletTransaction
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
