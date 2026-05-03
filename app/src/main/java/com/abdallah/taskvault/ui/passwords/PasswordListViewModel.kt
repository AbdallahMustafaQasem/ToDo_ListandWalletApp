package com.abdallah.taskvault.ui.passwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Password
import com.abdallah.taskvault.domain.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordListUiState(
    val passwords: List<Password> = emptyList(),
    val query: String = ""
)

@HiltViewModel
class PasswordListViewModel @Inject constructor(
    private val repository: PasswordRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<PasswordListUiState> = combine(
        _query,
        _query.flatMapLatest { q ->
            if (q.isBlank()) repository.getAll() else repository.search(q)
        }
    ) { q, passwords ->
        PasswordListUiState(passwords = passwords, query = q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PasswordListUiState())

    fun onQueryChange(q: String) { _query.value = q }

    fun delete(password: Password) {
        viewModelScope.launch {
            repository.delete(password)
            analytics.logPasswordDeleted()
        }
    }
}
