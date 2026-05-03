package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAll(): Flow<List<Habit>>
    suspend fun getById(id: Long): Habit?
    suspend fun insert(habit: Habit): Long
    suspend fun update(habit: Habit)
    suspend fun delete(habit: Habit)
    fun getCount(): Flow<Int>
    suspend fun markCompletedToday(habit: Habit)
}
