package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = TodoEntity::class,
            parentColumns = ["id"],
            childColumns = ["todo_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("todo_id")]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "todo_id")
    val todoId: Long,

    @ColumnInfo(name = "author_name")
    val authorName: String = "",

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long
)
