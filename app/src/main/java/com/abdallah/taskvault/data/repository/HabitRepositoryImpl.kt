package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.HabitDao
import com.abdallah.taskvault.data.local.toEntity
import com.abdallah.taskvault.domain.model.Habit
import com.abdallah.taskvault.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao
) : HabitRepository {

    override fun getAll(): Flow<List<Habit>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Habit? = dao.getById(id)?.toDomain()

    override suspend fun insert(habit: Habit): Long = dao.insert(habit.toEntity())

    override suspend fun update(habit: Habit) = dao.update(habit.toEntity())

    override suspend fun delete(habit: Habit) = dao.delete(habit.toEntity())

    override fun getCount(): Flow<Int> = dao.count()

    override suspend fun markCompletedToday(habit: Habit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (habit.lastCompletedDate == today) return

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)
        val newStreak = if (habit.lastCompletedDate == yesterdayStr) habit.streak + 1 else 1
        val newLongest = maxOf(habit.longestStreak, newStreak)

        dao.update(habit.copy(streak = newStreak, longestStreak = newLongest, lastCompletedDate = today).toEntity())
    }
}
