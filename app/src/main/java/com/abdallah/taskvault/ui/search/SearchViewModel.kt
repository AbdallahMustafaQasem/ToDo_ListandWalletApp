package com.abdallah.taskvault.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.repository.MemoirRepository
import com.abdallah.taskvault.domain.repository.NoteRepository
import com.abdallah.taskvault.domain.repository.PasswordRepository
import com.abdallah.taskvault.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SearchResultType { TODO, NOTE, MEMOIR, PASSWORD }

data class SearchResult(
    val id: Long,
    val type: SearchResultType,
    val title: String,
    val subtitle: String
)

data class SearchUiState(
    val query: String = "",
    val todos: List<SearchResult> = emptyList(),
    val notes: List<SearchResult> = emptyList(),
    val memoirs: List<SearchResult> = emptyList(),
    val passwords: List<SearchResult> = emptyList()
) {
    val isEmpty get() = todos.isEmpty() && notes.isEmpty() && memoirs.isEmpty() && passwords.isEmpty()
    val totalCount get() = todos.size + notes.size + memoirs.size + passwords.size
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val noteRepository: NoteRepository,
    private val memoirRepository: MemoirRepository,
    private val passwordRepository: PasswordRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val _query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = _query
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) return@flatMapLatest flowOf(SearchUiState(query = q))
            combine(
                todoRepository.getAllTodos(),
                noteRepository.searchNotes(q),
                memoirRepository.searchMemoirs(q),
                passwordRepository.search(q)
            ) { todos, notes, memoirs, passwords ->
                SearchUiState(
                    query = q,
                    todos = todos.filter {
                        !it.isDeleted && (it.title.contains(q, ignoreCase = true) ||
                            it.description.contains(q, ignoreCase = true))
                    }.map { SearchResult(it.id, SearchResultType.TODO, it.title, it.description.take(60)) },
                    notes = notes.map { SearchResult(it.id, SearchResultType.NOTE, it.title.ifBlank { "Untitled" }, it.content.take(60)) },
                    memoirs = memoirs.map { SearchResult(it.id, SearchResultType.MEMOIR, it.title.ifBlank { it.mood }, it.content.take(60)) },
                    passwords = passwords.map { SearchResult(it.id, SearchResultType.PASSWORD, it.title, it.username) }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun onQueryChange(q: String) {
        _query.value = q
        if (q.length >= 2) analytics.logEvent("search_performed")
    }
}
