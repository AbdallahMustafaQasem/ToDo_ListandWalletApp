package com.example.todoapp.ui.todolist

import android.app.AlarmManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.preferences.SortOrder
import com.example.todoapp.data.preferences.UserPreferencesRepository
import com.example.todoapp.domain.model.Priority
import com.example.todoapp.domain.model.Todo
import com.example.todoapp.domain.usecase.AddTodoUseCase
import com.example.todoapp.domain.usecase.DeleteTodoUseCase
import com.example.todoapp.domain.usecase.GetAllTodosUseCase
import com.example.todoapp.domain.usecase.ToggleTodoCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val getAllTodosUseCase: GetAllTodosUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase,
    private val alarmManager: AlarmManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    private var lastDeletedTodo: Todo? = null
    private var undoJob: Job? = null

    init {
        observeTodos()
        checkExactAlarmPermission()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.isDarkTheme.collect { dark ->
                _uiState.update { it.copy(isDarkTheme = dark) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.sortOrder.collect { order ->
                _uiState.update { state ->
                    state.copy(
                        sortOrder = order,
                        filteredTodos = applyFilters(state.allTodos, state.filter, state.searchQuery, order)
                    )
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.languageCode.collect { languageCode ->
                _uiState.update { it.copy(languageCode = languageCode) }
            }
        }
    }

    private fun observeTodos() {
        viewModelScope.launch {
            getAllTodosUseCase().collect { todos ->
                val current = _uiState.value
                _uiState.update {
                    it.copy(
                        allTodos = todos,
                        filteredTodos = applyFilters(todos, current.filter, current.searchQuery, current.sortOrder),
                        activeTodoCount = todos.count { t -> !t.isCompleted },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            _uiState.update {
                it.copy(showExactAlarmRationale = !alarmManager.canScheduleExactAlarms())
            }
        }
    }

    fun onFilterChanged(filter: FilterOption) {
        _uiState.update { state ->
            state.copy(
                filter = filter,
                filteredTodos = applyFilters(state.allTodos, filter, state.searchQuery, state.sortOrder)
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredTodos = applyFilters(state.allTodos, state.filter, query, state.sortOrder)
            )
        }
    }

    fun onSortOrderChanged(order: SortOrder) {
        viewModelScope.launch {
            userPreferencesRepository.setSortOrder(order)
        }
    }

    fun onToggleCompletion(todo: Todo, isCompleted: Boolean) {
        viewModelScope.launch {
            toggleTodoCompletionUseCase(todo, isCompleted)
        }
    }

    fun onDeleteTodo(todo: Todo) {
        viewModelScope.launch {
            deleteTodoUseCase(todo)
        }
        lastDeletedTodo = todo
        undoJob?.cancel()
        undoJob = viewModelScope.launch {
            delay(4_000)
            lastDeletedTodo = null
        }
    }

    fun onUndoDelete() {
        undoJob?.cancel()
        val todo = lastDeletedTodo ?: return
        lastDeletedTodo = null
        viewModelScope.launch {
            addTodoUseCase(todo)
        }
    }

    fun dismissExactAlarmRationale() {
        _uiState.update { it.copy(showExactAlarmRationale = false) }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = _uiState.value.isDarkTheme
            // cycle: null(system) -> true(dark) -> false(light) -> null
            val next: Boolean? = when (current) {
                null  -> true
                true  -> false
                false -> null
            }
            if (next == null) {
                // Remove the preference key entirely so the app follows the system theme.
                userPreferencesRepository.clearDarkTheme()
                _uiState.update { it.copy(isDarkTheme = null) }
            } else {
                userPreferencesRepository.setDarkTheme(next)
            }
        }
    }

    fun onLanguageSelected(languageCode: String?) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguageCode(languageCode)
        }
    }

    private fun applyFilters(
        todos: List<Todo>,
        filter: FilterOption,
        query: String,
        sortOrder: SortOrder
    ): List<Todo> {
        val filtered = when (filter) {
            FilterOption.ALL       -> todos
            FilterOption.ACTIVE    -> todos.filter { !it.isCompleted }
            FilterOption.COMPLETED -> todos.filter { it.isCompleted }
        }
        val queried = if (query.isBlank()) filtered
        else filtered.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        return when (sortOrder) {
            SortOrder.CREATION_DATE -> queried.sortedByDescending { it.createdAtMillis }
            SortOrder.DUE_DATE      -> queried.sortedWith(
                compareBy(nullsLast<Long>()) { it.dueDateMillis }
            )
            SortOrder.PRIORITY      -> queried.sortedByDescending { it.priority.ordinal }
            SortOrder.ALPHABETICAL  -> queried.sortedBy { it.title.lowercase() }
        }
    }
}
