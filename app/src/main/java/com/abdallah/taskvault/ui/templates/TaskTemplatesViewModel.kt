package com.abdallah.taskvault.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskTemplate(
    val id: Long,
    val name: String,
    val title: String,
    val description: String
)

data class TemplatesUiState(
    val templates: List<TaskTemplate> = emptyList()
)

@HiltViewModel
class TaskTemplatesViewModel @Inject constructor() : ViewModel() {

    private val _templates = MutableStateFlow<List<TaskTemplate>>(emptyList())
    val uiState: StateFlow<TemplatesUiState> = _templates
        .map { TemplatesUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TemplatesUiState())

    fun addTemplate(name: String, title: String, description: String) {
        val new = TaskTemplate(
            id = System.currentTimeMillis(),
            name = name,
            title = title,
            description = description
        )
        _templates.update { it + new }
    }

    fun deleteTemplate(id: Long) {
        _templates.update { it.filter { t -> t.id != id } }
    }
}
