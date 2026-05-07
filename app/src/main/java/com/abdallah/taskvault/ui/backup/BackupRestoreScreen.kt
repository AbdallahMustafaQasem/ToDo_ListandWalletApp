package com.abdallah.taskvault.ui.backup

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.restoreFromUri(context, it) }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup section
            ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.backup_create_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(stringResource(R.string.backup_create_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = {
                            viewModel.createBackup { file ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                                        context, "${context.packageName}.provider", file
                                    ))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, context.getString(R.string.backup_share)))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading && uiState.operation == BackupOperation.BACKUP) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.backup_create_btn))
                        }
                    }
                }
            }

            // Restore section
            ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text(stringResource(R.string.backup_restore_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Text(stringResource(R.string.backup_restore_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("application/json") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading && uiState.operation == BackupOperation.RESTORE) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.backup_restore_btn))
                        }
                    }
                }
            }

            // Warning
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.backup_restore_warning), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}
