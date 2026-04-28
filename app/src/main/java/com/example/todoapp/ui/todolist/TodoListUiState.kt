package com.example.todoapp.ui.todolist

import com.example.todoapp.data.preferences.SortOrder
import com.example.todoapp.domain.model.Todo

enum class FilterOption {
    ALL, ACTIVE, COMPLETED
}

data class TodoListUiState(
    val allTodos: List<Todo> = emptyList(),
    val filteredTodos: List<Todo> = emptyList(),
    val isLoading: Boolean = true,
    val filter: FilterOption = FilterOption.ALL,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.CREATION_DATE,
    val showExactAlarmRationale: Boolean = false,
    val isDarkTheme: Boolean? = null,           // null = follow system
    val activeTodoCount: Int = 0,
    val languageCode: String? = null
)
