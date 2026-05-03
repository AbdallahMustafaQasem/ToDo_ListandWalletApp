package com.abdallah.taskvault.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Habit
import com.abdallah.taskvault.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HabitListUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val uiState: StateFlow<HabitListUiState> = repository.getAll()
        .map { HabitListUiState(habits = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitListUiState())

    fun toggleToday(habit: Habit) {
        viewModelScope.launch {
            if (habit.lastCompletedDate == todayStr) {
                repository.update(habit.copy(streak = (habit.streak - 1).coerceAtLeast(0), lastCompletedDate = null))
            } else {
                repository.markCompletedToday(habit)
                analytics.logHabitCompleted()
            }
        }
    }

    fun delete(habit: Habit) {
        viewModelScope.launch {
            repository.delete(habit)
            analytics.logHabitDeleted()
        }
    }

    fun isCompletedToday(habit: Habit) = habit.lastCompletedDate == todayStr
}
