package com.abdallah.taskvault.ui.focus

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    todoId: Long,
    onNavigateBack: () -> Unit,
    viewModel: FocusModeViewModel = hiltViewModel()
) {
    LaunchedEffect(todoId) { viewModel.loadTodo(todoId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val elapsed by viewModel.elapsedSeconds.collectAsStateWithLifecycle()

    val hours = elapsed / 3600
    val mins  = (elapsed % 3600) / 60
    val secs  = elapsed % 60
    val timeLabel = if (hours > 0) "%d:%02d:%02d".format(hours, mins, secs)
                    else "%02d:%02d".format(mins, secs)

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.92f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.focus_mode_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.stopTimer(); onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Task title
                uiState.todo?.let { todo ->
                    ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Text(
                                text = todo.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (todo.description.isNotBlank()) {
                                Text(
                                    text = todo.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }

                // Elapsed time circle
                Box(
                    modifier = Modifier
                        .size((200 * (if (uiState.isRunning) pulse else 1f)).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeLabel,
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetTimer() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.pomodoro_reset))
                    }

                    Button(
                        onClick = {
                            if (uiState.isRunning) viewModel.pauseTimer()
                            else viewModel.startTimer()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (uiState.isRunning) stringResource(R.string.pomodoro_pause)
                            else stringResource(R.string.pomodoro_start)
                        )
                    }
                }

                // Mark done
                uiState.todo?.let { todo ->
                    if (!todo.isCompleted) {
                        FilledTonalButton(
                            onClick = { viewModel.markDone(); onNavigateBack() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.focus_mark_done))
                        }
                    }
                }
            }
        }
    }
}
