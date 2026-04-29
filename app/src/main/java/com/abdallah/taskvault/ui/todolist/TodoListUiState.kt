package com.abdallah.taskvault.ui.todolist

import com.abdallah.taskvault.data.preferences.SortOrder
import com.abdallah.taskvault.domain.model.Todo

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
