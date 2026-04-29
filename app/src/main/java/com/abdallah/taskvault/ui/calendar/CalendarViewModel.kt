package com.abdallah.taskvault.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.usecase.GetAllTodosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CalendarUiState(
    val allTodos: List<Todo> = emptyList(),
    val selectedDayMillis: Long? = null,
    val todosForSelectedDay: List<Todo> = emptyList(),
    val displayedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val displayedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val daysWithTodos: Set<Int> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getAllTodosUseCase: GetAllTodosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllTodosUseCase().collect { todos ->
                val current = _uiState.value
                val daysWithTodos = computeDaysWithTodos(todos, current.displayedYear, current.displayedMonth)
                val todosForDay = current.selectedDayMillis?.let { getTodosForDay(todos, it) } ?: emptyList()
                _uiState.update {
                    it.copy(
                        allTodos = todos,
                        daysWithTodos = daysWithTodos,
                        todosForSelectedDay = todosForDay,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onDaySelected(dayMillis: Long) {
        val todos = getTodosForDay(_uiState.value.allTodos, dayMillis)
        _uiState.update { it.copy(selectedDayMillis = dayMillis, todosForSelectedDay = todos) }
    }

    fun onMonthChanged(year: Int, month: Int) {
        val daysWithTodos = computeDaysWithTodos(_uiState.value.allTodos, year, month)
        _uiState.update {
            it.copy(
                displayedYear = year,
                displayedMonth = month,
                daysWithTodos = daysWithTodos,
                selectedDayMillis = null,
                todosForSelectedDay = emptyList()
            )
        }
    }

    private fun getTodosForDay(todos: List<Todo>, dayMillis: Long): List<Todo> {
        val cal = Calendar.getInstance().apply { timeInMillis = dayMillis }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return todos.filter { todo ->
            if (todo.isDeleted) return@filter false
            val due = todo.dueDateMillis ?: return@filter false
            val c = Calendar.getInstance().apply { timeInMillis = due }
            c.get(Calendar.YEAR) == year &&
                c.get(Calendar.MONTH) == month &&
                c.get(Calendar.DAY_OF_MONTH) == day
        }.sortedWith(compareBy({ it.isCompleted }, { it.priority.ordinal.inv() }))
    }

    private fun computeDaysWithTodos(todos: List<Todo>, year: Int, month: Int): Set<Int> {
        return todos.asSequence()
            .filter { !it.isDeleted && it.dueDateMillis != null }
            .mapNotNull { todo ->
                val c = Calendar.getInstance().apply { timeInMillis = todo.dueDateMillis!! }
                if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month)
                    c.get(Calendar.DAY_OF_MONTH)
                else null
            }
            .toSet()
    }
}
