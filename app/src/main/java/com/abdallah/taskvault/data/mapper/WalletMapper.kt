package com.abdallah.taskvault.data.mapper

import com.abdallah.taskvault.data.local.WalletBudgetEntity
import com.abdallah.taskvault.data.local.WalletCategoryEntity
import com.abdallah.taskvault.data.local.WalletTransactionEntity
import com.abdallah.taskvault.domain.model.WalletBudget
import com.abdallah.taskvault.domain.model.WalletCategory
import com.abdallah.taskvault.domain.model.WalletTransaction

fun WalletCategoryEntity.toDomain(): WalletCategory = WalletCategory(
    id = id,
    name = name,
    icon = icon,
    isDefault = isDefault
)

fun WalletCategory.toEntity(): WalletCategoryEntity = WalletCategoryEntity(
    id = id,
    name = name,
    icon = icon,
    isDefault = isDefault
)

fun WalletBudgetEntity.toDomain(): WalletBudget = WalletBudget(
    id = id,
    monthlyBudget = monthlyBudget
)

fun WalletBudget.toEntity(): WalletBudgetEntity = WalletBudgetEntity(
    id = id,
    monthlyBudget = monthlyBudget
)

fun WalletTransactionEntity.toDomain(category: WalletCategory?): WalletTransaction = WalletTransaction(
    id = id,
    type = type,
    amount = amount,
    categoryId = categoryId,
    categoryName = category?.name ?: "Uncategorized",
    categoryIcon = category?.icon ?: "💼",
    dateMillis = dateMillis,
    notes = notes
)

fun WalletTransaction.toEntity(): WalletTransactionEntity = WalletTransactionEntity(
    id = id,
    type = type,
    amount = amount,
    categoryId = categoryId,
    dateMillis = dateMillis,
    notes = notes
)
