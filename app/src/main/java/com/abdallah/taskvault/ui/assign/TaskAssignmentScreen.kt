package com.abdallah.taskvault.ui.assign

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Priority
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAssignmentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToContacts: () -> Unit,
    viewModel: TaskAssignmentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackbarHostState.showSnackbar("Task assigned successfully!")
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.assign_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // Clear form
                    IconButton(onClick = viewModel::clearForm) {
                        Icon(Icons.Default.ClearAll, contentDescription = stringResource(R.string.assign_clear))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Title ──
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.assign_field_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.titleError,
                supportingText = if (state.titleError) {{ Text(stringResource(R.string.assign_error_title)) }} else null,
                leadingIcon = { Icon(Icons.Default.Title, null) },
                shape = MaterialTheme.shapes.medium
            )

            // ── Description ──
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.assign_field_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                leadingIcon = { Icon(Icons.Default.Description, null) },
                shape = MaterialTheme.shapes.medium
            )

            // ── Priority ──
            Text(
                stringResource(R.string.assign_field_priority),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Priority.entries.forEach { priority ->
                    val selected = state.priority == priority
                    val color = when (priority) {
                        Priority.HIGH -> MaterialTheme.colorScheme.error
                        Priority.MEDIUM -> Color(0xFFF57C00)
                        Priority.LOW -> Color(0xFF388E3C)
                        Priority.NONE -> MaterialTheme.colorScheme.outline
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onPriorityChange(priority) },
                        label = { Text(priority.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.15f),
                            selectedLabelColor = color
                        ),
                        leadingIcon = if (selected) {{ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }} else null
                    )
                }
            }

            // ── Due Date ──
            ElevatedCard(
                onClick = { viewModel.showDatePicker() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.assign_field_due_date), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (state.dueDateMillis != null) dateFormat.format(Date(state.dueDateMillis!!))
                            else stringResource(R.string.assign_no_date),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (state.dueDateMillis != null) {
                        IconButton(onClick = { viewModel.onDueDateChange(null) }) {
                            Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        }
                    }
                }
            }

            HorizontalDivider()

            // ── Assignees Section ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.assign_section_assignees),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNavigateToContacts) {
                    Icon(Icons.Default.People, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.assign_manage_contacts))
                }
            }

            if (state.assigneeError) {
                Text(
                    stringResource(R.string.assign_error_assignee),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Contact chips
            if (state.contacts.isEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.assign_no_contacts), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = onNavigateToContacts) {
                            Text(stringResource(R.string.assign_add_contacts))
                        }
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.contacts, key = { it.id }) { contact ->
                        val selected = state.selectedContacts.contains(contact.id)
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleContactSelection(contact.id) },
                            label = { Text(contact.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            try { Color(contact.avatarColor.toColorInt()) }
                                            catch (_: Exception) { MaterialTheme.colorScheme.primary }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) {
                                        Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = Color.White)
                                    } else {
                                        Text(
                                            contact.displayName.take(1).uppercase(),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                // Selected count
                val count = state.selectedContacts.size
                if (count > 0) {
                    Text(
                        stringResource(R.string.assign_selected_count, count),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Manual User ID
            OutlinedTextField(
                value = state.manualUserId,
                onValueChange = viewModel::onManualUserIdChange,
                label = { Text(stringResource(R.string.assign_manual_user_id)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Fingerprint, null) },
                supportingText = { Text(stringResource(R.string.assign_manual_hint)) },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(8.dp))

            // ── Actions ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSubmitting
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.assign_submit))
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Date picker dialog
        if (state.showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { viewModel.hideDatePicker() },
                confirmButton = {
                    TextButton(onClick = { viewModel.onDueDateChange(datePickerState.selectedDateMillis) }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDatePicker() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
