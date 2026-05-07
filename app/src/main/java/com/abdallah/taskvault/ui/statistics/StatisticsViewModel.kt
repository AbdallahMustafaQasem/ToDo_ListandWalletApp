package com.abdallah.taskvault.ui.statistics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.usecase.GetAllTodosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DayActivity(
    val dayLabel: String,       // 3-char abbreviation e.g. "Mon"
    val completedCount: Int     // number of todos completed on that calendar day
)

data class StatisticsUiState(
    // ── Existing ────────────────────────────────────────────────────────────────
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val completionPercent: Float = 0f,
    val countByPriority: Map<Priority, Int> = emptyMap(),
    val streakDays: Int = 0,
    val overdueTodos: Int = 0,
    val isLoading: Boolean = true,

    // ── NEW ───────────────────────────────────────────────────────────────────
    val weeklyActivity: List<DayActivity> = emptyList(),
    val mostProductiveDay: String = "",
    // 30-day heatmap: index 0 = 29 days ago, index 29 = today
    val heatmap30Days: List<Int> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getAllTodosUseCase: GetAllTodosUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllTodosUseCase().collect { todos ->
                val total     = todos.size
                val completed = todos.count { it.isCompleted }
                val active    = total - completed
                val percent   = if (total == 0) 0f else (completed.toFloat() / total.toFloat()) * 100f

                val byPriority = Priority.entries.associateWith { p ->
                    todos.count { it.priority == p }
                }

                val now = System.currentTimeMillis()
                val overdue = todos.count { t ->
                    !t.isCompleted && t.dueDateMillis != null && t.dueDateMillis < now
                }

                // ── Streak ────────────────────────────────────────────────
                val completedDates = todos
                    .filter { it.isCompleted }
                    .map { dayOf(it.updatedAtMillis) }
                    .toSet()

                var streak = 0
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                while (completedDates.contains(cal.timeInMillis)) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }

                // ── Weekly Activity (last 7 days, index 0 = oldest) ───────
                val weeklyActivity = buildWeeklyActivity(todos.filter { it.isCompleted })

                // ── Most Productive Day (all-time) ─────────────────────────
                val mostProductiveDay = computeMostProductiveDay(todos.filter { it.isCompleted })

                val heatmap = buildHeatmap30Days(todos.filter { it.isCompleted })

                _uiState.update {
                    it.copy(
                        totalCount        = total,
                        completedCount    = completed,
                        activeCount       = active,
                        completionPercent = percent,
                        countByPriority   = byPriority,
                        streakDays        = streak,
                        overdueTodos      = overdue,
                        weeklyActivity    = weeklyActivity,
                        mostProductiveDay = mostProductiveDay,
                        heatmap30Days     = heatmap,
                        isLoading         = false
                    )
                }
            }
        }
    }

    /** Returns a list of 30 ints: index 0 = 29 days ago, index 29 = today. */
    private fun buildHeatmap30Days(completedTodos: List<Todo>): List<Int> {
        val result = mutableListOf<Int>()
        for (daysBack in 29 downTo 0) {
            val ref = Calendar.getInstance()
            ref.set(Calendar.HOUR_OF_DAY, 0); ref.set(Calendar.MINUTE, 0)
            ref.set(Calendar.SECOND, 0); ref.set(Calendar.MILLISECOND, 0)
            ref.add(Calendar.DAY_OF_YEAR, -daysBack)
            val dayStart = ref.timeInMillis
            val dayEnd   = dayStart + 86_400_000L - 1L
            result.add(completedTodos.count { it.updatedAtMillis in dayStart..dayEnd })
        }
        return result
    }

    /** Returns a list of 7 [DayActivity] items: index 0 = 6 days ago, index 6 = today. */
    private fun buildWeeklyActivity(completedTodos: List<Todo>): List<DayActivity> {
        val result = mutableListOf<DayActivity>()

        for (daysBack in 6 downTo 0) {
            val ref = Calendar.getInstance()
            ref.set(Calendar.HOUR_OF_DAY, 0)
            ref.set(Calendar.MINUTE, 0)
            ref.set(Calendar.SECOND, 0)
            ref.set(Calendar.MILLISECOND, 0)
            ref.add(Calendar.DAY_OF_YEAR, -daysBack)

            val dayStart = ref.timeInMillis
            val dayEnd   = dayStart + 86_400_000L - 1L

            val count = completedTodos.count { t ->
                t.updatedAtMillis in dayStart..dayEnd
            }

            val label = when (ref.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY    -> context.getString(R.string.day_sun)
                Calendar.MONDAY    -> context.getString(R.string.day_mon)
                Calendar.TUESDAY   -> context.getString(R.string.day_tue)
                Calendar.WEDNESDAY -> context.getString(R.string.day_wed)
                Calendar.THURSDAY  -> context.getString(R.string.day_thu)
                Calendar.FRIDAY    -> context.getString(R.string.day_fri)
                Calendar.SATURDAY  -> context.getString(R.string.day_sat)
                else               -> "???"
            }

            result.add(DayActivity(dayLabel = label, completedCount = count))
        }
        return result
    }

    /** Returns the full day-of-week name (e.g. "Monday") with the most completed todos all-time. */
    private fun computeMostProductiveDay(completedTodos: List<Todo>): String {
        if (completedTodos.isEmpty()) return ""

        val dayNames = mapOf(
            Calendar.SUNDAY    to context.getString(R.string.day_sunday),
            Calendar.MONDAY    to context.getString(R.string.day_monday),
            Calendar.TUESDAY   to context.getString(R.string.day_tuesday),
            Calendar.WEDNESDAY to context.getString(R.string.day_wednesday),
            Calendar.THURSDAY  to context.getString(R.string.day_thursday),
            Calendar.FRIDAY    to context.getString(R.string.day_friday),
            Calendar.SATURDAY  to context.getString(R.string.day_saturday)
        )

        val counts = IntArray(8) // indexed by Calendar.DAY_OF_WEEK (1–7)
        val cal = Calendar.getInstance()
        for (todo in completedTodos) {
            cal.timeInMillis = todo.updatedAtMillis
            counts[cal.get(Calendar.DAY_OF_WEEK)]++
        }

        val bestDow = counts.indices.maxByOrNull { counts[it] } ?: return ""
        return dayNames[bestDow] ?: ""
    }

    private fun dayOf(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
