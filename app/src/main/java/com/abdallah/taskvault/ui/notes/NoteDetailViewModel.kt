package com.abdallah.taskvault.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Note
import com.abdallah.taskvault.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteDetailUiState(
    val title: String = "",
    val content: String = "",
    val colorHex: String = "#6750A4",
    val isPinned: Boolean = false,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val analytics: AnalyticsHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    private var originalNote: Note? = null

    init {
        if (noteId != -1L) {
            viewModelScope.launch {
                val note = repository.getNoteById(noteId)
                if (note != null) {
                    originalNote = note
                    _uiState.value = NoteDetailUiState(
                        title = note.title,
                        content = note.content,
                        colorHex = note.colorHex,
                        isPinned = note.isPinned,
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

    fun onColorChanged(colorHex: String) {
        _uiState.value = _uiState.value.copy(colorHex = colorHex)
    }

    fun onPinToggled() {
        _uiState.value = _uiState.value.copy(isPinned = !_uiState.value.isPinned)
    }

    fun saveNote() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.value = state.copy(isSaved = true)
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (noteId == -1L) {
                repository.insertNote(
                    Note(
                        title = state.title,
                        content = state.content,
                        colorHex = state.colorHex,
                        isPinned = state.isPinned,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                analytics.logNoteCreated()
            } else {
                repository.updateNote(
                    Note(
                        id = noteId,
                        title = state.title,
                        content = state.content,
                        colorHex = state.colorHex,
                        isPinned = state.isPinned,
                        createdAt = originalNote?.createdAt ?: now,
                        updatedAt = now
                    )
                )
                analytics.logNoteUpdated()
            }
            _uiState.value = state.copy(isSaved = true)
        }
    }
}
