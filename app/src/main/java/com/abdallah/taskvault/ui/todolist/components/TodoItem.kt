package com.abdallah.taskvault.ui.todolist.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.domain.model.Todo
import com.abdallah.taskvault.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Priority → left-border colour
@Composable
fun Priority.borderColor(): Color = when (this) {
    Priority.NONE   -> PriorityNoneColor
    Priority.LOW    -> PriorityLowColor
    Priority.MEDIUM -> PriorityMediumColor
    Priority.HIGH   -> PriorityHighColor
}

@Composable
fun Priority.labelText(): String = when (this) {
    Priority.NONE   -> stringResource(R.string.priority_none)
    Priority.LOW    -> stringResource(R.string.priority_low)
    Priority.MEDIUM -> stringResource(R.string.priority_med)
    Priority.HIGH   -> stringResource(R.string.priority_high)
}

// Due-date status
private enum class DueDateStatus { OVERDUE, TODAY, UPCOMING }

private fun dueDateStatus(millis: Long?, isCompleted: Boolean): DueDateStatus? {
    if (millis == null || isCompleted) return null
    val now = System.currentTimeMillis()
    if (millis < now) return DueDateStatus.OVERDUE
    val endOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis
    return if (millis <= endOfToday) DueDateStatus.TODAY else DueDateStatus.UPCOMING
}

@Composable
private fun DueDateStatus.chipColor(): Color = when (this) {
    DueDateStatus.OVERDUE  -> OverdueColor
    DueDateStatus.TODAY    -> DueTodayColor
    DueDateStatus.UPCOMING -> UpcomingColor
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoItem(
    todo: Todo,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onComplete: () -> Unit = { onCheckedChange(!todo.isCompleted) },
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDismiss()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onComplete()
                    false   // don't dismiss card; let state update rebind
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = !isSelected,
        enableDismissFromEndToStart = !isSelected,
        backgroundContent = {
            SwipeBackground(dismissState = dismissState, isCompleted = todo.isCompleted)
        }
    ) {
        Box(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
        ) {
            TodoCardContent(
                todo            = todo,
                onCheckedChange = onCheckedChange,
                onClick         = onClick
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(
    dismissState: SwipeToDismissBoxState,
    isCompleted: Boolean
) {
    // dismissDirection was removed in Material3 1.3.x; use currentValue / targetValue instead.
    val currentValue = dismissState.currentValue
    val targetValue  = dismissState.targetValue
    val isStartToEnd = targetValue == SwipeToDismissBoxValue.StartToEnd ||
                       (currentValue == SwipeToDismissBoxValue.StartToEnd)
    val isEndToStart = targetValue == SwipeToDismissBoxValue.EndToStart ||
                       (currentValue == SwipeToDismissBoxValue.EndToStart)

    val bgColor by animateColorAsState(
        targetValue = when {
            isStartToEnd -> if (isCompleted) MaterialTheme.colorScheme.secondaryContainer
                           else Color(0xFF43A047)
            isEndToStart -> MaterialTheme.colorScheme.errorContainer
            else         -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "swipeBg"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 24.dp),
        contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        if (isStartToEnd) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.RadioButtonUnchecked else Icons.Default.TaskAlt,
                contentDescription = if (isCompleted) stringResource(R.string.cd_mark_incomplete) else stringResource(R.string.cd_complete),
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        } else if (isEndToStart) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.cd_delete),
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TodoCardContent(
    todo: Todo,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val status = dueDateStatus(todo.dueDateMillis, todo.isCompleted)

    // Animate left-border colour when priority changes
    val borderColor by animateColorAsState(
        targetValue = todo.priority.borderColor(),
        animationSpec = tween(500),
        label = "priorityBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Coloured left border — fillMaxHeight() works because IntrinsicSize.Min resolves Row height first
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(borderColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated checkbox
                AnimatedCheckbox(
                    checked  = todo.isCompleted,
                    onChange = onCheckedChange
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title with animated strikethrough
                    AnimatedTodoTitle(
                        title       = todo.title,
                        isCompleted = todo.isCompleted
                    )

                    // Description preview
                    if (todo.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text     = todo.description,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Chips row
                    if (todo.dueDateMillis != null || todo.priority != Priority.NONE) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            // Due date chip
                            if (todo.dueDateMillis != null) {
                                val chipBg = status?.chipColor()
                                    ?: MaterialTheme.colorScheme.surfaceVariant
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text  = formatDate(todo.dueDateMillis),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (status != null) Color.White
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = chipBg
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = false,
                                        borderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }

                            // Priority badge
                            if (todo.priority != Priority.NONE) {
                                val badgeColor by animateColorAsState(
                                    targetValue = todo.priority.borderColor(),
                                    animationSpec = tween(500),
                                    label = "priorityBadge"
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text  = todo.priority.labelText(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Animated checkbox with spring scale ──────────────────────────────────────

@Composable
private fun AnimatedCheckbox(
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "checkScale"
    )
    val tintColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "checkTint"
    )

    IconButton(
        onClick  = { onChange(!checked) },
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckCircle
                          else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (checked) stringResource(R.string.cd_mark_incomplete) else stringResource(R.string.cd_mark_complete),
            tint   = tintColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

// ── Animated strikethrough title ──────────────────────────────────────────────

@Composable
private fun AnimatedTodoTitle(title: String, isCompleted: Boolean) {
    val alphaTarget  = if (isCompleted) 0.5f else 1f
    val alpha by animateFloatAsState(
        targetValue   = alphaTarget,
        animationSpec = tween(300),
        label         = "titleAlpha"
    )
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None

    Text(
        text           = title,
        style          = MaterialTheme.typography.bodyLarge.copy(
            textDecoration = textDecoration,
            color          = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        ),
        maxLines       = 2,
        overflow       = TextOverflow.Ellipsis
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatDate(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = millis - now
    val days = diff / (1000 * 60 * 60 * 24)
    if (days in 0..6) {
        val sdf = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }
    return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
}
