package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memoirs")
data class MemoirEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val mood: String = "😊",
    @ColumnInfo(name = "date_millis") val dateMillis: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at_millis") val createdAt: Long = System.currentTimeMillis()
)
