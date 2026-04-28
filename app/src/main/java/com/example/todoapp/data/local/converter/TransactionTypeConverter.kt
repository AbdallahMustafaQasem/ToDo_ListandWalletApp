package com.example.todoapp.data.local.converter

import androidx.room.TypeConverter
import com.example.todoapp.domain.model.TransactionType

class TransactionTypeConverter {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
