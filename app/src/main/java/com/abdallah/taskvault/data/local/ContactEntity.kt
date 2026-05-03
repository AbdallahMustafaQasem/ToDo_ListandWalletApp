package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abdallah.taskvault.domain.model.Contact

@Entity(tableName = "contacts", indices = [Index(value = ["user_id"], unique = true)])
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id")      val userId: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    val role: String = "",
    @ColumnInfo(name = "avatar_color") val avatarColor: String = "#6750A4",
    @ColumnInfo(name = "added_at")     val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Contact(id, userId, displayName, role, avatarColor, addedAt)
}

fun Contact.toEntity() = ContactEntity(id, userId, displayName, role, avatarColor, addedAtMillis)
