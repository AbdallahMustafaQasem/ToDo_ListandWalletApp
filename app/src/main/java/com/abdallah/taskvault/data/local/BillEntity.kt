package com.abdallah.taskvault.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abdallah.taskvault.domain.model.Bill

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    @ColumnInfo(name = "due_day")         val dueDay: Int,
    val category: String = "Other",
    val notes: String = "",
    @ColumnInfo(name = "is_paid")         val isPaid: Boolean = false,
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Boolean = false,
    @ColumnInfo(name = "reminder_days_before") val reminderDaysBefore: Int = 1,
    @ColumnInfo(name = "next_due_date_millis") val nextDueDateMillis: Long,
    @ColumnInfo(name = "created_at")      val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain() = Bill(id, name, amount, dueDay, category, notes, isPaid, reminderEnabled, reminderDaysBefore, nextDueDateMillis, createdAt)
}

fun Bill.toEntity() = BillEntity(id, name, amount, dueDay, category, notes, isPaid, reminderEnabled, reminderDaysBefore, nextDueDateMillis, createdAt)
