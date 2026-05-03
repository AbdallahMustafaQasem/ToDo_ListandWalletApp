package com.abdallah.taskvault.ui.memoirs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Memoir
import com.abdallah.taskvault.domain.repository.MemoirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoirListUiState(
    val memoirs: List<Memoir> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class MemoirListViewModel @Inject constructor(
    private val repository: MemoirRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MemoirListUiState> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllMemoirs()
            else repository.searchMemoirs(query)
        }
        .map { memoirs ->
            MemoirListUiState(memoirs = memoirs, searchQuery = _searchQuery.value, isLoading = false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MemoirListUiState())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private var lastDeleted: Memoir? = null

    fun deleteMemoir(memoir: Memoir) {
        lastDeleted = memoir
        viewModelScope.launch {
            repository.deleteMemoir(memoir)
            analytics.logMemoirDeleted()
        }
    }

    fun undoDelete() {
        lastDeleted?.let { memoir ->
            viewModelScope.launch { repository.insertMemoir(memoir) }
            lastDeleted = null
        }
    }
}
