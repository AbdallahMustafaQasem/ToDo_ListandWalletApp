package com.abdallah.taskvault.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.repository.BillRepository
import com.abdallah.taskvault.domain.repository.ContactRepository
import com.abdallah.taskvault.domain.repository.HabitRepository
import com.abdallah.taskvault.domain.repository.MemoirRepository
import com.abdallah.taskvault.domain.repository.NoteRepository
import com.abdallah.taskvault.domain.repository.PasswordRepository
import com.abdallah.taskvault.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SearchResultType { TODO, NOTE, MEMOIR, PASSWORD, CONTACT, HABIT, BILL }

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
    val passwords: List<SearchResult> = emptyList(),
    val contacts: List<SearchResult> = emptyList(),
    val habits: List<SearchResult> = emptyList(),
    val bills: List<SearchResult> = emptyList()
) {
    val isEmpty get() = todos.isEmpty() && notes.isEmpty() && memoirs.isEmpty() &&
            passwords.isEmpty() && contacts.isEmpty() && habits.isEmpty() && bills.isEmpty()
    val totalCount get() = todos.size + notes.size + memoirs.size + passwords.size +
            contacts.size + habits.size + bills.size
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val noteRepository: NoteRepository,
    private val memoirRepository: MemoirRepository,
    private val passwordRepository: PasswordRepository,
    private val contactRepository: ContactRepository,
    private val habitRepository: HabitRepository,
    private val billRepository: BillRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val _query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SearchUiState> = _query
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) return@flatMapLatest flowOf(SearchUiState(query = q))

            val todosFlow = todoRepository.getAllTodos()
            val notesFlow = noteRepository.searchNotes(q)
            val memoirsFlow = memoirRepository.searchMemoirs(q)
            val passwordsFlow = passwordRepository.search(q)
            val contactsFlow = contactRepository.search(q)
            val habitsFlow = habitRepository.getAll()
            val billsFlow = billRepository.getAll()

            combine(
                combine(todosFlow, notesFlow, memoirsFlow, passwordsFlow) { t, n, m, p -> listOf(t, n, m, p) },
                combine(contactsFlow, habitsFlow, billsFlow) { c, h, b -> Triple(c, h, b) }
            ) { first, (contacts, habits, bills) ->
                @Suppress("UNCHECKED_CAST")
                val todos = first[0] as List<com.abdallah.taskvault.domain.model.Todo>
                @Suppress("UNCHECKED_CAST")
                val notes = first[1] as List<com.abdallah.taskvault.domain.model.Note>
                @Suppress("UNCHECKED_CAST")
                val memoirs = first[2] as List<com.abdallah.taskvault.domain.model.Memoir>
                @Suppress("UNCHECKED_CAST")
                val passwords = first[3] as List<com.abdallah.taskvault.domain.model.Password>

                SearchUiState(
                    query = q,
                    todos = todos.filter {
                        !it.isDeleted && (it.title.contains(q, ignoreCase = true) ||
                            it.description.contains(q, ignoreCase = true))
                    }.map { SearchResult(it.id, SearchResultType.TODO, it.title, it.description.take(60)) },
                    notes = notes.map { SearchResult(it.id, SearchResultType.NOTE, it.title.ifBlank { "Untitled" }, it.content.take(60)) },
                    memoirs = memoirs.map { SearchResult(it.id, SearchResultType.MEMOIR, it.title.ifBlank { it.mood }, it.content.take(60)) },
                    passwords = passwords.map { SearchResult(it.id, SearchResultType.PASSWORD, it.title, it.username) },
                    contacts = contacts.map { SearchResult(it.id, SearchResultType.CONTACT, it.displayName, it.userId) },
                    habits = habits.filter {
                        it.name.contains(q, ignoreCase = true) ||
                            it.description.contains(q, ignoreCase = true)
                    }.map { SearchResult(it.id, SearchResultType.HABIT, it.name, it.description.take(60)) },
                    bills = bills.filter {
                        it.name.contains(q, ignoreCase = true)
                    }.map { SearchResult(it.id, SearchResultType.BILL, it.name, it.amount.toString()) }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun onQueryChange(q: String) {
        _query.value = q
        if (q.length >= 2) analytics.logEvent("search_performed")
    }
}
