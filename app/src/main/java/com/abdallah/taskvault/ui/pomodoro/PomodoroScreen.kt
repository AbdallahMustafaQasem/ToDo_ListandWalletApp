package com.abdallah.taskvault.ui.pomodoro

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    onNavigateBack: () -> Unit,
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pomodoro_title)) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Phase tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PomodoroPhase.entries.forEach { phase ->
                    FilterChip(
                        selected = state.phase == phase,
                        onClick  = { viewModel.switchPhase(phase) },
                        label    = { Text(stringResource(phase.labelRes())) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Session counter
            Text(
                text = stringResource(R.string.pomodoro_session, state.completedSessions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Circular progress timer
            val progress = state.remainingSeconds.toFloat() / state.totalSeconds.toFloat()
            val arcColor = if (state.phase == PomodoroPhase.WORK)
                MaterialTheme.colorScheme.primary
            else
                Color(0xFF388E3C)
            val trackColor = arcColor.copy(alpha = 0.15f)

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    val inset = stroke.width / 2
                    drawArc(
                        color       = trackColor,
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        topLeft     = Offset(inset, inset),
                        size        = Size(size.width - stroke.width, size.height - stroke.width),
                        style       = stroke
                    )
                    drawArc(
                        color       = arcColor,
                        startAngle  = -90f,
                        sweepAngle  = 360f * progress,
                        useCenter   = false,
                        topLeft     = Offset(inset, inset),
                        size        = Size(size.width - stroke.width, size.height - stroke.width),
                        style       = stroke
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = state.formattedTime,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = stringResource(state.phase.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.reset() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.pomodoro_reset))
                }

                FloatingActionButton(
                    onClick        = { if (state.isRunning) viewModel.pause() else viewModel.start() },
                    containerColor = if (state.phase == PomodoroPhase.WORK)
                        MaterialTheme.colorScheme.primary
                    else Color(0xFF388E3C),
                    modifier       = Modifier.size(72.dp),
                    shape          = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isRunning) stringResource(R.string.pomodoro_pause) else stringResource(R.string.pomodoro_start),
                        modifier = Modifier.size(32.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = { viewModel.skip() },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = stringResource(R.string.pomodoro_skip))
                }
            }

            // Stats card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = stringResource(R.string.pomodoro_completed), value = state.completedSessions.toString())
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    StatItem(label = stringResource(R.string.pomodoro_focus_min), value = "${state.completedSessions * 25}")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun PomodoroPhase.labelRes(): Int = when (this) {
    PomodoroPhase.WORK       -> R.string.pomodoro_phase_work
    PomodoroPhase.SHORT_BREAK -> R.string.pomodoro_phase_short_break
    PomodoroPhase.LONG_BREAK  -> R.string.pomodoro_phase_long_break
}
