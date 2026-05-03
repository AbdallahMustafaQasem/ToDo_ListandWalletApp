package com.abdallah.taskvault.ui.bills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R

private val CATEGORIES = listOf("Housing","Utilities","Subscriptions","Insurance","Loan","Groceries","Transport","Health","Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: BillDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id == -1L) stringResource(R.string.bill_detail_new)
                        else stringResource(R.string.bill_detail_edit),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = viewModel::save, enabled = state.name.isNotBlank() && state.amount.toDoubleOrNull() != null) {
                        Icon(Icons.Default.Check, stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = state.name, onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.bill_field_name)) },
                leadingIcon = { Icon(Icons.Default.Receipt, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = state.amount, onValueChange = viewModel::onAmountChange,
                label = { Text(stringResource(R.string.bill_field_amount)) },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.bill_field_due_day), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.onDueDayChange(state.dueDay - 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, null)
                    }
                    Text(state.dueDay.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.onDueDayChange(state.dueDay + 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = it }) {
                OutlinedTextField(
                    value = state.category, onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.bill_field_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                    CATEGORIES.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { viewModel.onCategoryChange(cat); showCategoryMenu = false })
                    }
                }
            }

            OutlinedTextField(
                value = state.notes, onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.bill_field_notes)) },
                modifier = Modifier.fillMaxWidth(), maxLines = 3, shape = MaterialTheme.shapes.medium
            )

            HorizontalDivider()

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.bill_field_reminder_enabled), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = state.reminderEnabled, onCheckedChange = { viewModel.onReminderToggle() })
            }

            if (state.reminderEnabled) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.bill_reminder_days, state.reminderDaysBefore), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Slider(
                        value = state.reminderDaysBefore.toFloat(),
                        onValueChange = { viewModel.onReminderDaysChange(it.toInt()) },
                        valueRange = 1f..14f, steps = 12,
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    }
}
