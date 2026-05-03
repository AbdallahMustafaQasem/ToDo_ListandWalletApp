package com.abdallah.taskvault.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Habit
import com.abdallah.taskvault.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitDetailUiState(
    val id: Long = -1L,
    val name: String = "",
    val description: String = "",
    val colorHex: String = "#6750A4",
    val emoji: String = "⭐",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HabitRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>("habitId") ?: -1L
    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    init {
        if (habitId != -1L) {
            viewModelScope.launch {
                repository.getById(habitId)?.let { h ->
                    _uiState.value = HabitDetailUiState(
                        id = h.id, name = h.name, description = h.description,
                        colorHex = h.colorHex, emoji = h.emoji, isLoading = false
                    )
                } ?: run { _uiState.value = _uiState.value.copy(isLoading = false) }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onNameChange(v: String)        { _uiState.value = _uiState.value.copy(name = v) }
    fun onDescriptionChange(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun onColorChange(v: String)       { _uiState.value = _uiState.value.copy(colorHex = v) }
    fun onEmojiChange(v: String)       { _uiState.value = _uiState.value.copy(emoji = v) }

    fun save() {
        val s = _uiState.value
        if (s.name.isBlank()) return
        viewModelScope.launch {
            if (habitId == -1L) {
                repository.insert(Habit(name = s.name, description = s.description, colorHex = s.colorHex, emoji = s.emoji))
                analytics.logHabitCreated()
            } else {
                val existing = repository.getById(habitId) ?: return@launch
                repository.update(existing.copy(name = s.name, description = s.description, colorHex = s.colorHex, emoji = s.emoji))
                analytics.logHabitUpdated()
            }
            _uiState.value = s.copy(isSaved = true)
        }
    }
}
