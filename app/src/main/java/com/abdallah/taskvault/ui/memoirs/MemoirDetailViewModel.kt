package com.abdallah.taskvault.ui.memoirs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Memoir
import com.abdallah.taskvault.domain.repository.MemoirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoirDetailUiState(
    val title: String = "",
    val content: String = "",
    val mood: String = "😊",
    val dateMillis: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

@HiltViewModel
class MemoirDetailViewModel @Inject constructor(
    private val repository: MemoirRepository,
    private val analytics: AnalyticsHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoirId: Long = savedStateHandle.get<Long>("memoirId") ?: -1L

    private val _uiState = MutableStateFlow(MemoirDetailUiState())
    val uiState: StateFlow<MemoirDetailUiState> = _uiState.asStateFlow()

    private var originalCreatedAt: Long = System.currentTimeMillis()

    init {
        if (memoirId != -1L) {
            viewModelScope.launch {
                val memoir = repository.getMemoirById(memoirId)
                if (memoir != null) {
                    originalCreatedAt = memoir.createdAt
                    _uiState.value = MemoirDetailUiState(
                        title = memoir.title,
                        content = memoir.content,
                        mood = memoir.mood,
                        dateMillis = memoir.dateMillis,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onContentChanged(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun onMoodChanged(mood: String) {
        _uiState.value = _uiState.value.copy(mood = mood)
    }

    fun onDateChanged(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(dateMillis = dateMillis)
    }

    fun saveMemoir() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.value = state.copy(isSaved = true)
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (memoirId == -1L) {
                repository.insertMemoir(
                    Memoir(
                        title = state.title,
                        content = state.content,
                        mood = state.mood,
                        dateMillis = state.dateMillis,
                        createdAt = now
                    )
                )
                analytics.logMemoirCreated()
            } else {
                repository.updateMemoir(
                    Memoir(
                        id = memoirId,
                        title = state.title,
                        content = state.content,
                        mood = state.mood,
                        dateMillis = state.dateMillis,
                        createdAt = originalCreatedAt
                    )
                )
                analytics.logMemoirUpdated()
            }
            _uiState.value = state.copy(isSaved = true)
        }
    }
}
