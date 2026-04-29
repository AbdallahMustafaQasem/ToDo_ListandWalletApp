package com.abdallah.taskvault.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY date_millis DESC, id DESC")
    fun getTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: WalletTransactionEntity)

    @Query("DELETE FROM wallet_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT * FROM wallet_categories ORDER BY is_default DESC, name ASC")
    fun getCategories(): Flow<List<WalletCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: WalletCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: WalletCategoryEntity)

    @Query("DELETE FROM wallet_categories WHERE id = :id AND is_default = 0")
    suspend fun deleteCategory(id: Long)

    @Query("UPDATE wallet_transactions SET category_id = NULL WHERE category_id = :categoryId")
    suspend fun clearCategoryReferences(categoryId: Long)

    @Query("SELECT * FROM wallet_budget WHERE id = 1 LIMIT 1")
    fun getBudget(): Flow<WalletBudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: WalletBudgetEntity)
}
