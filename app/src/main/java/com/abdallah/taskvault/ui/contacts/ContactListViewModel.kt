package com.abdallah.taskvault.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.model.Contact
import com.abdallah.taskvault.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactListUiState(
    val contacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingContact: Contact? = null,
    val addUserId: String = "",
    val addDisplayName: String = "",
    val addRole: String = "",
    val snackbar: String? = null,
    val error: String? = null
)

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState: StateFlow<ContactListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQuery.debounce(300).collectLatest { query ->
                val flow = if (query.isBlank()) contactRepository.getAll()
                           else contactRepository.search(query)
                flow.collect { list ->
                    _uiState.update { it.copy(contacts = list, isLoading = false) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showAddDialog()  = _uiState.update { it.copy(showAddDialog = true, addUserId = "", addDisplayName = "", addRole = "") }
    fun hideAddDialog()  = _uiState.update { it.copy(showAddDialog = false) }
    fun showEditDialog(contact: Contact) = _uiState.update { it.copy(showEditDialog = true, editingContact = contact, addUserId = contact.userId, addDisplayName = contact.displayName, addRole = contact.role) }
    fun hideEditDialog() = _uiState.update { it.copy(showEditDialog = false, editingContact = null) }

    fun onUserIdChange(v: String) = _uiState.update { it.copy(addUserId = v) }
    fun onDisplayNameChange(v: String) = _uiState.update { it.copy(addDisplayName = v) }
    fun onRoleChange(v: String) = _uiState.update { it.copy(addRole = v) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbar = null) }

    fun addContact() {
        val state = _uiState.value
        if (state.addUserId.isBlank() || state.addDisplayName.isBlank()) {
            _uiState.update { it.copy(error = "User ID and Name are required") }
            return
        }
        viewModelScope.launch {
            val existing = contactRepository.getByUserId(state.addUserId)
            if (existing != null) {
                _uiState.update { it.copy(snackbar = "Contact already exists", showAddDialog = false) }
                return@launch
            }
            val colors = listOf("#6750A4", "#D32F2F", "#388E3C", "#1976D2", "#F57C00", "#7B1FA2", "#00796B")
            contactRepository.insert(
                Contact(
                    userId = state.addUserId.trim(),
                    displayName = state.addDisplayName.trim(),
                    role = state.addRole.trim(),
                    avatarColor = colors.random()
                )
            )
            _uiState.update { it.copy(showAddDialog = false, snackbar = "Contact added") }
        }
    }

    fun updateContact() {
        val state = _uiState.value
        val editing = state.editingContact ?: return
        if (state.addDisplayName.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        viewModelScope.launch {
            contactRepository.update(
                editing.copy(
                    displayName = state.addDisplayName.trim(),
                    role = state.addRole.trim()
                )
            )
            _uiState.update { it.copy(showEditDialog = false, editingContact = null, snackbar = "Contact updated") }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactRepository.delete(contact)
            _uiState.update { it.copy(snackbar = "Contact removed") }
        }
    }
}
