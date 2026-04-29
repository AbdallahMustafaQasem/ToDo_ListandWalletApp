package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abdallah.taskvault.domain.model.TransactionType

@Entity(
    tableName = "wallet_transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "type")
    val type: TransactionType,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "category_id")
    val categoryId: Long?,
    @ColumnInfo(name = "date_millis")
    val dateMillis: Long,
    @ColumnInfo(name = "notes")
    val notes: String = ""
)
