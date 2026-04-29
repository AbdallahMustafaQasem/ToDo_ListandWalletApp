package com.abdallah.taskvault.data.local.converter

import androidx.room.TypeConverter
import com.abdallah.taskvault.domain.model.Priority

class PriorityConverter {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)
}
