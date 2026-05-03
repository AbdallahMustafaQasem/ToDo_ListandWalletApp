package com.abdallah.taskvault.ui.assign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun AssignmentStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssignmentStatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.assign_stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.assigned_refresh))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(onClick = { viewModel.load() }) { Text(stringResource(R.string.assigned_retry)) }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Summary cards ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            label = stringResource(R.string.assign_stats_total),
                            value = state.total.toString(),
                            icon = Icons.Default.Assignment,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            label = stringResource(R.string.assign_stats_acceptance),
                            value = "${(state.acceptanceRate * 100).toInt()}%",
                            icon = Icons.Default.ThumbUp,
                            color = Color(0xFF388E3C),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            label = stringResource(R.string.assign_stats_completion),
                            value = "${(state.completionRate * 100).toInt()}%",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF1976D2),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── Status breakdown ──
                item {
                    Text(
                        stringResource(R.string.assign_stats_breakdown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AssignmentStatus.entries.forEach { status ->
                                val count = state.countByStatus[status] ?: 0
                                val fraction = if (state.total == 0) 0f else count.toFloat() / state.total
                                StatusRow(status = status, count = count, fraction = fraction)
                            }
                        }
                    }
                }

                // ── Recent assignments ──
                if (state.recentAssignments.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.assign_stats_recent),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(state.recentAssignments, key = { it.id }) { assignment ->
                        RecentAssignmentCard(assignment = assignment, dateFormat = dateFormat)
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, Modifier.size(28.dp), tint = color)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusRow(status: AssignmentStatus, count: Int, fraction: Float) {
    val color = statusColor(status)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                    Text(
                        statusLabel(status),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                }
            }
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.12f)
        )
    }
}

@Composable
private fun RecentAssignmentCard(assignment: TaskAssignment, dateFormat: SimpleDateFormat) {
    val priorityColor = when (assignment.priority) {
        Priority.HIGH -> MaterialTheme.colorScheme.error
        Priority.MEDIUM -> Color(0xFFF57C00)
        Priority.LOW -> Color(0xFF388E3C)
        Priority.NONE -> MaterialTheme.colorScheme.outline
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Flag, null, Modifier.size(20.dp), tint = priorityColor)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    assignment.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.assign_stats_assignees, assignment.assigneeNames.joinToString(", ")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                assignment.dueDateMillis?.let {
                    Text(
                        dateFormat.format(Date(it)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                color = statusColor(assignment.status).copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    statusLabel(assignment.status),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor(assignment.status)
                )
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
