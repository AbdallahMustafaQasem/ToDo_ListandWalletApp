package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abdallah.taskvault.domain.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String = "#6750A4"
) {
    fun toDomain() = Tag(id = id, name = name, colorHex = colorHex)
}

fun Tag.toEntity() = TagEntity(id = id, name = name, colorHex = colorHex)
