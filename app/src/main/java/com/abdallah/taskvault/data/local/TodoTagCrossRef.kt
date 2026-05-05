package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "todo_tags",
    primaryKeys = ["todo_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = TodoEntity::class,
            parentColumns = ["id"],
            childColumns = ["todo_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("todo_id"), Index("tag_id")]
)
data class TodoTagCrossRef(
    @ColumnInfo(name = "todo_id") val todoId: Long,
    @ColumnInfo(name = "tag_id")  val tagId: Long
)
