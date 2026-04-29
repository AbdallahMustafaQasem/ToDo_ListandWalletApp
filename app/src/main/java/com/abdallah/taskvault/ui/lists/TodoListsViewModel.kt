package com.abdallah.taskvault.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.model.TodoList
import com.abdallah.taskvault.domain.repository.TodoListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodoListsUiState(
    val lists: List<TodoList> = emptyList(),
    val isLoading: Boolean = true,
    val editingList: TodoList? = null,
    val showDialog: Boolean = false
)

@HiltViewModel
class TodoListsViewModel @Inject constructor(
    private val repository: TodoListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListsUiState())
    val uiState: StateFlow<TodoListsUiState> = _uiState.asStateFlow()

    init {
        repository.getAllLists()
            .onEach { lists -> _uiState.update { it.copy(lists = lists, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun onAddClick() {
        _uiState.update { it.copy(editingList = null, showDialog = true) }
    }

    fun onEditClick(list: TodoList) {
        _uiState.update { it.copy(editingList = list, showDialog = true) }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(showDialog = false, editingList = null) }
    }

    fun onSaveList(name: String, colorHex: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val editing = _uiState.value.editingList
            if (editing != null) {
                repository.updateList(editing.copy(name = name, colorHex = colorHex, icon = icon))
            } else {
                repository.insertList(
                    TodoList(
                        name = name,
                        colorHex = colorHex,
                        icon = icon,
                        createdAtMillis = System.currentTimeMillis()
                    )
                )
            }
            _uiState.update { it.copy(showDialog = false, editingList = null) }
        }
    }

    fun onDeleteList(list: TodoList) {
        viewModelScope.launch {
            repository.deleteList(list.id)
        }
    }
}
