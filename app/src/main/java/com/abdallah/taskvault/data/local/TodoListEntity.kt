package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_hex", defaultValue = "#6750A4")
    val colorHex: String = "#6750A4",

    @ColumnInfo(name = "icon", defaultValue = "📋")
    val icon: String = "📋",

    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long
)
