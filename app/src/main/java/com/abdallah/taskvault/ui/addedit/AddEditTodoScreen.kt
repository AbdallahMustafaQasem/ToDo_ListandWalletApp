package com.abdallah.taskvault.ui.addedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.RecurrenceRule
import com.abdallah.taskvault.domain.model.Subtask
import com.abdallah.taskvault.domain.model.TodoList
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFocus: (Long) -> Unit = {},
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
                    if (uiState.isEditMode) {
                        IconButton(onClick = { onNavigateToFocus(uiState.id) }) {
                            Icon(
                                Icons.Default.CenterFocusStrong,
                                contentDescription = "Focus Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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

            RecurrenceCard(
                selected = uiState.recurrenceRule,
                onSelected = viewModel::onRecurrenceChanged
            )

            if (uiState.availableLists.isNotEmpty()) {
                ListPickerCard(
                    lists = uiState.availableLists,
                    selectedListId = uiState.listId,
                    onListSelected = viewModel::onListSelected
                )
            }

            SubtasksCard(
                subtasks = uiState.subtasks,
                newSubtaskTitle = uiState.newSubtaskTitle,
                onNewSubtaskTitleChanged = viewModel::onNewSubtaskTitleChanged,
                onAddSubtask = viewModel::onAddSubtask,
                onToggleSubtask = viewModel::onToggleSubtask,
                onDeleteSubtask = viewModel::onDeleteSubtask
            )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceCard(
    selected: RecurrenceRule,
    onSelected: (RecurrenceRule) -> Unit
) {
    data class RuleOption(
        val rule: RecurrenceRule,
        val labelRes: Int,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val descRes: Int
    )
    val options = listOf(
        RuleOption(RecurrenceRule.NONE,    R.string.recurrence_none,    Icons.Default.Block,         R.string.recurrence_none_desc),
        RuleOption(RecurrenceRule.DAILY,   R.string.recurrence_daily,   Icons.Default.Today,         R.string.recurrence_daily_desc),
        RuleOption(RecurrenceRule.WEEKLY,  R.string.recurrence_weekly,  Icons.Default.DateRange,     R.string.recurrence_weekly_desc),
        RuleOption(RecurrenceRule.MONTHLY, R.string.recurrence_monthly, Icons.Default.CalendarMonth, R.string.recurrence_monthly_desc)
    )

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.recurrence_label), style = MaterialTheme.typography.titleMedium)
            options.forEach { opt ->
                val isSelected = selected == opt.rule
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSelected(opt.rule) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            opt.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(opt.labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                stringResource(opt.descRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListPickerCard(
    lists: List<TodoList>,
    selectedListId: Long?,
    onListSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedList = lists.find { it.id == selectedListId }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.list_picker_label), style = MaterialTheme.typography.titleMedium)
            Box {
                OutlinedTextField(
                    value = selectedList?.let { "${it.icon} ${it.name}" } ?: stringResource(R.string.list_picker_none),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { expanded = true }
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.list_picker_none)) },
                    onClick = { onListSelected(null); expanded = false },
                    leadingIcon = { if (selectedListId == null) Icon(Icons.Default.Check, null) }
                )
                lists.forEach { list ->
                    DropdownMenuItem(
                        text = { Text("${list.icon} ${list.name}") },
                        onClick = { onListSelected(list.id); expanded = false },
                        leadingIcon = { if (selectedListId == list.id) Icon(Icons.Default.Check, null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtasksCard(
    subtasks: List<Subtask>,
    newSubtaskTitle: String,
    onNewSubtaskTitleChanged: (String) -> Unit,
    onAddSubtask: () -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onDeleteSubtask: (Subtask) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.subtasks_label),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (subtasks.isNotEmpty()) {
                    val done = subtasks.count { it.isCompleted }
                    Text(
                        "$done/${subtasks.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            subtasks.forEach { subtask ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = subtask.isCompleted,
                        onCheckedChange = { onToggleSubtask(subtask) }
                    )
                    Text(
                        text = subtask.title,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { onDeleteSubtask(subtask) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.subtask_delete_cd),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newSubtaskTitle,
                    onValueChange = onNewSubtaskTitleChanged,
                    placeholder = { Text(stringResource(R.string.subtask_add_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(
                    onClick = onAddSubtask,
                    enabled = newSubtaskTitle.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.subtask_add_cd),
                        tint = if (newSubtaskTitle.isNotBlank()) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
