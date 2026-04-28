package com.example.todoapp.ui.addedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todoapp.R
import com.example.todoapp.domain.model.Priority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedEvent) {
        if (uiState.savedEvent) {
            onNavigateBack()
            viewModel.consumeSavedEvent()
        }
    }
    LaunchedEffect(uiState.deletedEvent) {
        if (uiState.deletedEvent) {
            onNavigateBack()
            viewModel.consumeDeletedEvent()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDueDateChanged(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.dueTimeHour ?: 8,
            initialMinute = uiState.dueTimeMinute ?: 0,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDueTimeChanged(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.dialog_delete_todo_title)) },
            text = {
                Text(stringResource(R.string.dialog_delete_todo_body, uiState.title))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) stringResource(R.string.title_edit_todo) else stringResource(R.string.title_new_todo)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::onSave,
                        enabled = uiState.isSaveEnabled
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.save))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (uiState.isEditMode) stringResource(R.string.title_edit_todo) else stringResource(R.string.title_new_todo),
                        style = MaterialTheme.typography.titleLarge
                    )
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChanged,
                        label = { Text(stringResource(R.string.field_title_label)) },
                        singleLine = true,
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { err -> { Text(err) } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.field_title_placeholder)) }
                    )
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        label = { Text(stringResource(R.string.field_description_label)) },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.field_description_placeholder)) }
                    )
                }
            }

            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.priority_label), style = MaterialTheme.typography.titleMedium)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = uiState.dueDateMillis?.let { formatDateOnly(it) } ?: "",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.field_due_date_label)) },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.field_due_date_placeholder)) },
                            trailingIcon = {
                                if (uiState.dueDateMillis != null) {
                                    IconButton(onClick = { viewModel.onDueDateChanged(null) }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear_date))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showDatePicker = true }
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (uiState.dueTimeHour != null && uiState.dueTimeMinute != null)
                                formatTime(uiState.dueTimeHour!!, uiState.dueTimeMinute!!)
                            else "",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.field_due_time_label)) },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.field_due_time_placeholder)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (uiState.dueDateMillis != null)
                                    MaterialTheme.colorScheme.outline
                                else MaterialTheme.colorScheme.outlineVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        if (uiState.dueDateMillis != null) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { showTimePicker = true }
                            )
                        }
                    }
                    PrioritySelector(
                        selected = uiState.priority,
                        onSelected = viewModel::onPriorityChanged
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.reminder_title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (uiState.dueDateMillis == null || uiState.dueTimeHour == null)
                                    stringResource(R.string.reminder_needs_date)
                                else stringResource(R.string.reminder_notify_due),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.reminderEnabled,
                            onCheckedChange = viewModel::onReminderToggled,
                            enabled = uiState.dueDateMillis != null && uiState.dueTimeHour != null
                        )
                    }
                }
            }

            if (uiState.isEditMode) {
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(stringResource(R.string.button_delete_todo))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrioritySelector(
    selected: Priority,
    onSelected: (Priority) -> Unit
) {
    val priorities = Priority.entries.toTypedArray()
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        priorities.forEachIndexed { index, priority ->
            SegmentedButton(
                selected = selected == priority,
                onClick = { onSelected(priority) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = priorities.size),
                label = {
                    Text(stringResource(when (priority) {
                        Priority.NONE   -> R.string.priority_none
                        Priority.LOW    -> R.string.priority_low
                        Priority.MEDIUM -> R.string.priority_medium
                        Priority.HIGH   -> R.string.priority_high
                    }))
                }
            )
        }
    }
}

private fun formatDateOnly(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

private fun formatTime(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)
}
