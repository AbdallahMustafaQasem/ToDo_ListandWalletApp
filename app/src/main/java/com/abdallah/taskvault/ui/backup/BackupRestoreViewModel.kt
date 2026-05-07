package com.abdallah.taskvault.ui.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.domain.usecase.BackupRestoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class BackupOperation { NONE, BACKUP, RESTORE }

data class BackupUiState(
    val isLoading: Boolean = false,
    val operation: BackupOperation = BackupOperation.NONE,
    val message: String? = null
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreUseCase: BackupRestoreUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun createBackup(onFile: (File) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, operation = BackupOperation.BACKUP) }
            try {
                val file = backupRestoreUseCase.createBackup()
                _uiState.update { it.copy(isLoading = false, operation = BackupOperation.NONE) }
                onFile(file)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, operation = BackupOperation.NONE, message = "Backup failed: ${e.message}") }
            }
        }
    }

    fun restoreFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, operation = BackupOperation.RESTORE) }
            try {
                val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    ?: throw Exception("Could not read file")
                val count = backupRestoreUseCase.restoreBackup(jsonText)
                _uiState.update { it.copy(isLoading = false, operation = BackupOperation.NONE, message = "Restored $count tasks") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, operation = BackupOperation.NONE, message = "Restore failed: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
