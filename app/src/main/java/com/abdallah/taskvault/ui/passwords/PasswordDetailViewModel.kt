package com.abdallah.taskvault.ui.passwords

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Password
import com.abdallah.taskvault.domain.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordDetailUiState(
    val id: Long = -1L,
    val title: String = "",
    val username: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class PasswordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PasswordRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val passwordId: Long = savedStateHandle.get<Long>("passwordId") ?: -1L

    private val _uiState = MutableStateFlow(PasswordDetailUiState())
    val uiState: StateFlow<PasswordDetailUiState> = _uiState.asStateFlow()

    init {
        if (passwordId != -1L) {
            viewModelScope.launch {
                repository.getById(passwordId)?.let { p ->
                    _uiState.value = PasswordDetailUiState(
                        id = p.id,
                        title = p.title,
                        username = p.username,
                        password = p.password,
                        url = p.url,
                        notes = p.notes
                    )
                }
            }
        }
    }

    fun onTitleChange(v: String)    { _uiState.value = _uiState.value.copy(title    = v) }
    fun onUsernameChange(v: String) { _uiState.value = _uiState.value.copy(username = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }
    fun onUrlChange(v: String)      { _uiState.value = _uiState.value.copy(url      = v) }
    fun onNotesChange(v: String)    { _uiState.value = _uiState.value.copy(notes    = v) }

    fun save() {
        val s = _uiState.value
        if (s.title.isBlank() || s.password.isBlank()) return
        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            val now = System.currentTimeMillis()
            if (s.id == -1L) {
                repository.insert(
                    Password(
                        title = s.title.trim(), username = s.username.trim(),
                        password = s.password, url = s.url.trim(),
                        notes = s.notes.trim(), createdAt = now, updatedAt = now
                    )
                )
                analytics.logPasswordCreated()
            } else {
                repository.update(
                    Password(
                        id = s.id, title = s.title.trim(), username = s.username.trim(),
                        password = s.password, url = s.url.trim(),
                        notes = s.notes.trim(), updatedAt = now,
                        createdAt = _uiState.value.id
                    )
                )
                analytics.logPasswordUpdated()
            }
            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }
}
