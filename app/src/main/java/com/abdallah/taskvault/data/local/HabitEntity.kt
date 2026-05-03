package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abdallah.taskvault.domain.model.Habit

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    @ColumnInfo(name = "color_hex")     val colorHex: String = "#6750A4",
    val emoji: String = "⭐",
    val streak: Int = 0,
    @ColumnInfo(name = "longest_streak") val longestStreak: Int = 0,
    @ColumnInfo(name = "last_completed_date") val lastCompletedDate: String? = null,
    @ColumnInfo(name = "created_at")    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Habit(id, name, description, colorHex, emoji, streak, longestStreak, lastCompletedDate, createdAt)
}

fun Habit.toEntity() = HabitEntity(id, name, description, colorHex, emoji, streak, longestStreak, lastCompletedDate, createdAt)
