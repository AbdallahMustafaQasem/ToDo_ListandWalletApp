package com.abdallah.taskvault.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTemplatesScreen(
    onNavigateBack: () -> Unit,
    onApplyTemplate: (title: String, description: String) -> Unit,
    viewModel: TaskTemplatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddTemplateDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, title, desc ->
                viewModel.addTemplate(name, title, desc)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.templates_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.templates_add))
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.templates.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text(stringResource(R.string.templates_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.templates, key = { it.id }) { template ->
                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(template.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(template.title, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (template.description.isNotBlank()) {
                                    Text(template.description, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2)
                                }
                            }
                            IconButton(onClick = { onApplyTemplate(template.title, template.description) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.templates_use),
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteTemplate(template.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTemplateDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, title: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.templates_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.templates_name_label)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text(stringResource(R.string.templates_task_title_label)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text(stringResource(R.string.field_description_label)) },
                    minLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank() && title.isNotBlank()) onConfirm(name, title, description) },
                enabled = name.isNotBlank() && title.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
