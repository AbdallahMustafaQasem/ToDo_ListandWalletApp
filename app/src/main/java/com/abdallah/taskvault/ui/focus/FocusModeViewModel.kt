package com.abdallah.taskvault.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.domain.usecase.GetTodoByIdUseCase
import com.abdallah.taskvault.domain.usecase.ToggleTodoCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusModeUiState(
    val todo: Todo? = null,
    val isRunning: Boolean = false
)

@HiltViewModel
class FocusModeViewModel @Inject constructor(
    private val getTodoByIdUseCase: GetTodoByIdUseCase,
    private val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusModeUiState())
    val uiState: StateFlow<FocusModeUiState> = _uiState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    fun loadTodo(id: Long) {
        viewModelScope.launch {
            val todo = getTodoByIdUseCase(id)
            _uiState.update { it.copy(todo = todo) }
        }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _elapsedSeconds.update { it + 1 }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _elapsedSeconds.value = 0L
        _uiState.update { it.copy(isRunning = false) }
    }

    fun markDone() {
        val todo = _uiState.value.todo ?: return
        viewModelScope.launch {
            toggleTodoCompletionUseCase(todo, true)
        }
        stopTimer()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
