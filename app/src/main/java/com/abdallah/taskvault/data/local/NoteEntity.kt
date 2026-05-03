package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    @ColumnInfo(name = "color_hex") val colorHex: String = "#6750A4",
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "created_at_millis") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at_millis") val updatedAt: Long = System.currentTimeMillis()
)
