package com.abdallah.taskvault.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.usecase.GetDeletedTodosUseCase
import com.abdallah.taskvault.domain.usecase.PermanentlyDeleteTodoUseCase
import com.abdallah.taskvault.domain.usecase.RestoreTodoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val items: List<Todo> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    getDeletedTodos: GetDeletedTodosUseCase,
    private val restore: RestoreTodoUseCase,
    private val permanentlyDelete: PermanentlyDeleteTodoUseCase
) : ViewModel() {

    val uiState: StateFlow<TrashUiState> = getDeletedTodos()
        .map { TrashUiState(items = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrashUiState())

    fun onRestore(id: Long) {
        viewModelScope.launch { restore(id) }
    }

    fun onPermanentlyDelete(id: Long) {
        viewModelScope.launch { permanentlyDelete(id) }
    }

    fun onEmptyTrash() {
        viewModelScope.launch {
            uiState.value.items.forEach { permanentlyDelete(it.id) }
        }
    }
}
