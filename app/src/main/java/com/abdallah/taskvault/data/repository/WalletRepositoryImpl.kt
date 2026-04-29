package com.abdallah.taskvault.data.repository

import android.content.Context
import com.abdallah.taskvault.R
import com.abdallah.taskvault.data.local.WalletDao
import com.abdallah.taskvault.data.local.WalletCategoryEntity
import com.abdallah.taskvault.data.mapper.toDomain
import com.abdallah.taskvault.data.mapper.toEntity
import com.abdallah.taskvault.domain.model.WalletBudget
import com.abdallah.taskvault.domain.model.WalletCategory
import com.abdallah.taskvault.domain.model.WalletTransaction
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.repository.WalletRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val sync: FirebaseSyncRepository,
    @ApplicationContext private val context: Context
) : WalletRepository {

    override fun getTransactions(): Flow<List<WalletTransaction>> =
        combine(walletDao.getTransactions(), walletDao.getCategories()) { transactions, categories ->
            val categoryMap = categories.associateBy { it.id }
            transactions.map { transaction ->
                transaction.toDomain(categoryMap[transaction.categoryId]?.toDomain())
            }
        }

    override fun getCategories(): Flow<List<WalletCategory>> =
        walletDao.getCategories().map { items -> items.map { it.toDomain() } }

    override fun getBudget(): Flow<WalletBudget?> =
        walletDao.getBudget().map { it?.toDomain() }

    override suspend fun upsertTransaction(transaction: WalletTransaction) {
        if (transaction.id == 0L) {
            val newId = walletDao.insertTransaction(transaction.toEntity())
            sync.syncTransaction(transaction.copy(id = newId))
        } else {
            walletDao.updateTransaction(transaction.toEntity())
            sync.syncTransaction(transaction)
        }
    }

    override suspend fun deleteTransaction(id: Long) {
        walletDao.deleteTransaction(id)
        sync.deleteTransactionSync(id)
    }

    override suspend fun upsertCategory(category: WalletCategory) {
        if (category.id == 0L) {
            val newId = walletDao.insertCategory(category.toEntity())
            sync.syncCategory(category.copy(id = newId))
        } else {
            walletDao.updateCategory(category.toEntity())
            sync.syncCategory(category)
        }
    }

    override suspend fun deleteCategory(id: Long) {
        walletDao.clearCategoryReferences(id)
        walletDao.deleteCategory(id)
        sync.deleteCategorySync(id)
    }

    override suspend fun setMonthlyBudget(amount: Double) {
        walletDao.upsertBudget(WalletBudget(monthlyBudget = amount).toEntity())
        sync.syncBudget(WalletBudget(monthlyBudget = amount))
    }

    override suspend fun seedDefaultCategories() {
        val categories = walletDao.getCategories().first()
        val food = context.getString(R.string.wallet_category_food)
        val transportation = context.getString(R.string.wallet_category_transportation)
        val bills = context.getString(R.string.wallet_category_bills)
        val entertainment = context.getString(R.string.wallet_category_entertainment)
        if (categories.none { it.name.equals(food, true) }) {
            walletDao.insertCategory(WalletCategoryEntity(name = food, icon = "🍔", isDefault = true))
        }
        if (categories.none { it.name.equals(transportation, true) }) {
            walletDao.insertCategory(WalletCategoryEntity(name = transportation, icon = "🚗", isDefault = true))
        }
        if (categories.none { it.name.equals(bills, true) }) {
            walletDao.insertCategory(WalletCategoryEntity(name = bills, icon = "💡", isDefault = true))
        }
        if (categories.none { it.name.equals(entertainment, true) }) {
            walletDao.insertCategory(WalletCategoryEntity(name = entertainment, icon = "🎮", isDefault = true))
        }
    }
}
