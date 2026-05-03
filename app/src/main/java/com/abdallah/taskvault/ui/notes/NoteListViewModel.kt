package com.abdallah.taskvault.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Note
import com.abdallah.taskvault.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NoteListUiState> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllNotes()
            else repository.searchNotes(query)
        }
        .map { notes ->
            _isLoading.value = false
            NoteListUiState(
                notes = notes,
                searchQuery = _searchQuery.value,
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteListUiState())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private var lastDeleted: Note? = null

    fun deleteNote(note: Note) {
        lastDeleted = note
        viewModelScope.launch {
            repository.deleteNote(note)
            analytics.logNoteDeleted()
        }
    }

    fun undoDelete() {
        lastDeleted?.let { note ->
            viewModelScope.launch { repository.insertNote(note) }
            lastDeleted = null
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }
}
