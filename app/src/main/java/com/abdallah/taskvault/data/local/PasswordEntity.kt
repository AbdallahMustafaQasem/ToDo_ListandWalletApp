package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val username: String = "",
    val password: String,
    val url: String = "",
    val notes: String = "",
    @ColumnInfo(name = "created_at_millis") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at_millis") val updatedAt: Long = System.currentTimeMillis()
)
