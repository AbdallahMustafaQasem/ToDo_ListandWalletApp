package com.abdallah.taskvault.ui.assign

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.AssignmentStatus
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.TaskAssignment
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignedToMeScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssignedToMeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val filtered by viewModel.filteredAssignments.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.assigned_to_me_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignments() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.assigned_refresh))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.filterStatus == null,
                        onClick = { viewModel.setFilterStatus(null) },
                        label = { Text(stringResource(R.string.assigned_filter_all)) }
                    )
                }
                items(AssignmentStatus.entries.toList()) { status ->
                    FilterChip(
                        selected = state.filterStatus == status,
                        onClick = { viewModel.setFilterStatus(status) },
                        label = { Text(statusLabel(status)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = statusColor(status).copy(alpha = 0.15f),
                            selectedLabelColor = statusColor(status)
                        )
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(12.dp))
                            FilledTonalButton(onClick = { viewModel.loadAssignments() }) {
                                Text(stringResource(R.string.assigned_retry))
                            }
                        }
                    }
                }
                filtered.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AssignmentInd,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.assigned_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.assigned_empty_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { assignment ->
                            AssignmentCard(
                                assignment = assignment,
                                dateFormat = dateFormat,
                                onAccept = { viewModel.updateStatus(assignment.id, AssignmentStatus.ACCEPTED) },
                                onDecline = { viewModel.updateStatus(assignment.id, AssignmentStatus.DECLINED) },
                                onStart = { viewModel.updateStatus(assignment.id, AssignmentStatus.IN_PROGRESS) },
                                onComplete = { viewModel.updateStatus(assignment.id, AssignmentStatus.COMPLETED) }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentCard(
    assignment: TaskAssignment,
    dateFormat: SimpleDateFormat,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    val priorityColor = when (assignment.priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> Color(0xFFF57C00)
        Priority.LOW -> Color(0xFF388E3C)
        Priority.NONE -> MaterialTheme.colorScheme.outline
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: title + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    assignment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = statusColor(assignment.status).copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        statusLabel(assignment.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor(assignment.status)
                    )
                }
            }

            // Description
            if (assignment.description.isNotBlank()) {
                Text(
                    assignment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Meta row: priority, due date, assigner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Flag, null, modifier = Modifier.size(14.dp), tint = priorityColor)
                    Text(assignment.priority.name, style = MaterialTheme.typography.labelSmall, color = priorityColor)
                }

                // Due date
                assignment.dueDateMillis?.let { millis ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(dateFormat.format(Date(millis)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Assigned by
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(R.string.assigned_by, assignment.assignerName),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Action buttons based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (assignment.status) {
                    AssignmentStatus.PENDING -> {
                        TextButton(onClick = onDecline, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.assigned_decline))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = onAccept) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.assigned_accept))
                        }
                    }
                    AssignmentStatus.ACCEPTED -> {
                        Button(onClick = onStart) {
                            Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.assigned_start))
                        }
                    }
                    AssignmentStatus.IN_PROGRESS -> {
                        Button(onClick = onComplete) {
                            Icon(Icons.Default.Done, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.assigned_complete))
                        }
                    }
                    AssignmentStatus.COMPLETED -> {
                        Text(
                            stringResource(R.string.assigned_completed_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF388E3C),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    AssignmentStatus.DECLINED -> {
                        Text(
                            stringResource(R.string.assigned_declined_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: AssignmentStatus): String = when (status) {
    AssignmentStatus.PENDING -> stringResource(R.string.assigned_status_pending)
    AssignmentStatus.ACCEPTED -> stringResource(R.string.assigned_status_accepted)
    AssignmentStatus.IN_PROGRESS -> stringResource(R.string.assigned_status_in_progress)
    AssignmentStatus.COMPLETED -> stringResource(R.string.assigned_status_completed)
    AssignmentStatus.DECLINED -> stringResource(R.string.assigned_status_declined)
}

@Composable
private fun statusColor(status: AssignmentStatus): Color = when (status) {
    AssignmentStatus.PENDING -> Color(0xFFF57C00)
    AssignmentStatus.ACCEPTED -> Color(0xFF1976D2)
    AssignmentStatus.IN_PROGRESS -> Color(0xFF7B1FA2)
    AssignmentStatus.COMPLETED -> Color(0xFF388E3C)
    AssignmentStatus.DECLINED -> Color(0xFFD32F2F)
}
