package com.abdallah.taskvault.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToMemoirs: () -> Unit,
    onNavigateToPasswords: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToAssign: () -> Unit,
    onNavigateToAssignedToMe: () -> Unit,
    onNavigateToAssignmentStats: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val greetingRes = remember { greetingRes() }
    val greeting = stringResource(greetingRes)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_title))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = stringResource(R.string.profile_title),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Greeting header ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.userName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Summary strip ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryChip(
                    label = stringResource(R.string.dashboard_tasks_count, uiState.activeTodoCount),
                    icon = Icons.Default.CheckCircle,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                SummaryChip(
                    label = stringResource(R.string.dashboard_notes_count, uiState.noteCount),
                    icon = Icons.Default.StickyNote2,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                SummaryChip(
                    label = stringResource(R.string.dashboard_memoirs_count, uiState.memoirCount),
                    icon = Icons.Default.MenuBook,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Feature grid ─────────────────────────────────────────────
            Text(
                stringResource(R.string.dashboard_features_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = stringResource(R.string.dashboard_feature_tasks),
                        subtitle = stringResource(R.string.dashboard_tasks_subtitle, uiState.activeTodoCount),
                        icon = Icons.Default.CheckCircle,
                        gradient = listOf(Color(0xFF6750A4), Color(0xFF9A82DB)),
                        onClick = onNavigateToTasks,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = stringResource(R.string.dashboard_feature_notes),
                        subtitle = stringResource(R.string.dashboard_notes_subtitle, uiState.noteCount),
                        icon = Icons.Default.StickyNote2,
                        gradient = listOf(Color(0xFFB5515D), Color(0xFFE07A5F)),
                        onClick = onNavigateToNotes,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = stringResource(R.string.dashboard_feature_memoirs),
                        subtitle = stringResource(R.string.dashboard_memoirs_subtitle, uiState.memoirCount),
                        icon = Icons.Default.MenuBook,
                        gradient = listOf(Color(0xFF2E7D63), Color(0xFF81B29A)),
                        onClick = onNavigateToMemoirs,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = stringResource(R.string.menu_wallet),
                        subtitle = stringResource(R.string.dashboard_wallet_subtitle),
                        icon = Icons.Default.AccountBalanceWallet,
                        gradient = listOf(Color(0xFF1565C0), Color(0xFF457B9D)),
                        onClick = onNavigateToWallet,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = stringResource(R.string.calendar_title),
                        subtitle = stringResource(R.string.dashboard_calendar_subtitle),
                        icon = Icons.Default.CalendarMonth,
                        gradient = listOf(Color(0xFFC77700), Color(0xFFF2CC8F)),
                        onClick = onNavigateToCalendar,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = stringResource(R.string.menu_statistics),
                        subtitle = stringResource(R.string.dashboard_stats_subtitle),
                        icon = Icons.Default.BarChart,
                        gradient = listOf(Color(0xFF3D405B), Color(0xFF6B7080)),
                        onClick = onNavigateToStatistics,
                        modifier = Modifier.weight(1f)
                    )
                }
                FeatureCard(
                    title = stringResource(R.string.passwords_title),
                    subtitle = stringResource(R.string.dashboard_passwords_subtitle, uiState.passwordCount),
                    icon = Icons.Default.Lock,
                    gradient = listOf(Color(0xFF6A0572), Color(0xFFAB47BC)),
                    onClick = onNavigateToPasswords,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = stringResource(R.string.habits_title),
                        subtitle = stringResource(R.string.dashboard_habits_subtitle, uiState.habitCount),
                        icon = Icons.Default.SelfImprovement,
                        gradient = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)),
                        onClick = onNavigateToHabits,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = stringResource(R.string.bills_title),
                        subtitle = stringResource(R.string.dashboard_bills_subtitle, uiState.billsDueSoonCount),
                        icon = Icons.Default.Receipt,
                        gradient = listOf(Color(0xFFD32F2F), Color(0xFFFF5252)),
                        onClick = onNavigateToBills,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = stringResource(R.string.assign_title),
                        subtitle = stringResource(R.string.dashboard_assign_subtitle),
                        icon = Icons.Default.PersonAdd,
                        gradient = listOf(Color(0xFF00695C), Color(0xFF26A69A)),
                        onClick = onNavigateToAssign,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = stringResource(R.string.assigned_to_me_title),
                        subtitle = stringResource(R.string.dashboard_assigned_to_me_subtitle),
                        icon = Icons.Default.AssignmentInd,
                        gradient = listOf(Color(0xFF4527A0), Color(0xFF7C4DFF)),
                        onClick = onNavigateToAssignedToMe,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = stringResource(R.string.assign_stats_title),
                        subtitle = stringResource(R.string.dashboard_assign_stats_subtitle),
                        icon = Icons.Default.BarChart,
                        gradient = listOf(Color(0xFFE65100), Color(0xFFFF9800)),
                        onClick = onNavigateToAssignmentStats,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = stringResource(R.string.contacts_title),
                        subtitle = stringResource(R.string.dashboard_contacts_subtitle),
                        icon = Icons.Default.People,
                        gradient = listOf(Color(0xFF37474F), Color(0xFF78909C)),
                        onClick = onNavigateToContacts,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun greetingRes(): Int {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11  -> R.string.dashboard_greeting_morning
        in 12..17 -> R.string.dashboard_greeting_afternoon
        in 18..21 -> R.string.dashboard_greeting_evening
        else      -> R.string.dashboard_greeting_night
    }
}
