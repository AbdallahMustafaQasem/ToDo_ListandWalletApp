package com.example.todoapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_budget")
data class WalletBudgetEntity(
    @PrimaryKey
    val id: Long = 1,
    @ColumnInfo(name = "monthly_budget")
    val monthlyBudget: Double
)
