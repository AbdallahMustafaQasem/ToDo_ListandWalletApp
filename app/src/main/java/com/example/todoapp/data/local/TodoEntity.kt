package com.example.todoapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todoapp.domain.model.Priority

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "due_date_millis")
    val dueDateMillis: Long? = null,

    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.NONE,

    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = false,

    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,

    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at_millis")
    val deletedAtMillis: Long? = null
)
