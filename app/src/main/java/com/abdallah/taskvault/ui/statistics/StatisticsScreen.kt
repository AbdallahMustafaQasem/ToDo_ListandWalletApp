package com.abdallah.taskvault.ui.statistics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Priority
import com.abdallah.taskvault.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_statistics)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.title_statistics),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${uiState.completedCount}/${uiState.totalCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.stats_completion_rate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    )
                }
            }
            SummaryGrid(uiState = uiState)

            CompletionRingCard(percent = uiState.completionPercent)

            // ── 3. Weekly activity bar chart ──────────────────────────────
            WeeklyActivityCard(weeklyActivity = uiState.weeklyActivity)

            // ── 4. Priority breakdown (horizontal bars) ───────────────────
            PriorityBreakdownCard(countByPriority = uiState.countByPriority)

            // ── 5. Insight row: streak + most productive day ──────────────
            InsightRow(
                streakDays        = uiState.streakDays,
                mostProductiveDay = uiState.mostProductiveDay
            )

            // ── 6. 30-day heatmap ─────────────────────────────────────────
            if (uiState.heatmap30Days.isNotEmpty()) {
                HeatmapCard(counts = uiState.heatmap30Days)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── 1. Summary 2×2 Grid ──────────────────────────────────────────────────────

@Composable
private fun SummaryGrid(uiState: StatisticsUiState) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier       = Modifier.weight(1f),
                label          = stringResource(R.string.stats_total),
                count          = uiState.totalCount,
                icon           = Icons.Default.PieChart,
                containerColor = scheme.primaryContainer,
                contentColor   = scheme.onPrimaryContainer
            )
            SummaryCard(
                modifier       = Modifier.weight(1f),
                label          = stringResource(R.string.stats_completed),
                count          = uiState.completedCount,
                icon           = Icons.Default.CheckCircle,
                containerColor = scheme.secondaryContainer,
                contentColor   = scheme.onSecondaryContainer
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier       = Modifier.weight(1f),
                label          = stringResource(R.string.stats_remaining),
                count          = uiState.activeCount,
                icon           = Icons.Default.PendingActions,
                containerColor = Color(0xFFEDE7F6),
                contentColor   = Color(0xFF4527A0)
            )
            SummaryCard(
                modifier       = Modifier.weight(1f),
                label          = stringResource(R.string.stats_overdue),
                count          = uiState.overdueTodos,
                icon           = Icons.Default.Warning,
                containerColor = if (uiState.overdueTodos > 0)
                    scheme.errorContainer else scheme.surfaceVariant,
                contentColor   = if (uiState.overdueTodos > 0)
                    scheme.onErrorContainer else scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    count: Int,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue    = count,
        animationSpec  = tween(durationMillis = 800),
        label          = "summaryCount_$label"
    )
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector      = icon,
                contentDescription = null,
                tint             = contentColor.copy(alpha = 0.75f),
                modifier         = Modifier.size(22.dp)
            )
            Text(
                text       = animatedCount.toString(),
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = contentColor
            )
            Text(
                text      = label,
                style     = MaterialTheme.typography.labelMedium,
                color     = contentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── 2. Completion ring card ───────────────────────────────────────────────────

@Composable
private fun CompletionRingCard(percent: Float) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier            = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.stats_completion_rate),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            DualRing(
                percent  = percent,
                modifier = Modifier.size(180.dp)
            )
        }
    }
}

@Composable
private fun DualRing(percent: Float, modifier: Modifier = Modifier) {
    val animatedPercent by animateFloatAsState(
        targetValue    = percent,
        animationSpec  = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label          = "completionAnim"
    )
    val purple       = Purple40
    val teal         = Color(0xFF00897B)
    val trackColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
    val onSurface    = MaterialTheme.colorScheme.onSurface
    // Lerp from purple (0 %) to teal (100 %)
    val arcColor     = lerp(purple, teal, (animatedPercent / 100f).coerceIn(0f, 1f))

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerStroke = 18.dp.toPx()
            val innerStroke = 10.dp.toPx()
            val outerInset  = outerStroke / 2f
            val innerInset  = outerStroke + 8.dp.toPx() + innerStroke / 2f
            val sweepAngle  = 360f * (animatedPercent / 100f)

            // Outer track
            drawArc(
                color      = trackColor.copy(alpha = 1f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft    = Offset(outerInset, outerInset),
                size       = Size(size.width - outerStroke, size.height - outerStroke),
                style      = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )
            // Outer progress
            if (sweepAngle > 0f) {
                drawArc(
                    color      = arcColor,
                    startAngle = -90f, sweepAngle = sweepAngle, useCenter = false,
                    topLeft    = Offset(outerInset, outerInset),
                    size       = Size(size.width - outerStroke, size.height - outerStroke),
                    style      = Stroke(width = outerStroke, cap = StrokeCap.Round)
                )
            }
            // Inner track (30 % alpha)
            drawArc(
                color      = arcColor.copy(alpha = 0.30f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft    = Offset(innerInset, innerInset),
                size       = Size(size.width - innerInset * 2, size.height - innerInset * 2),
                style      = Stroke(width = innerStroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "${animatedPercent.toInt()}%",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = onSurface
            )
            Text(
                text  = stringResource(R.string.stats_completion_suffix),
                style = MaterialTheme.typography.labelMedium,
                color = onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ── 3. Weekly Activity chart ──────────────────────────────────────────────────

@Composable
private fun WeeklyActivityCard(weeklyActivity: List<DayActivity>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.stats_last_7_days),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            WeeklyBarChart(
                weeklyActivity = weeklyActivity,
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(
    weeklyActivity: List<DayActivity>,
    modifier: Modifier = Modifier
) {
    val primaryColor    = MaterialTheme.colorScheme.primary
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val labelColor      = MaterialTheme.colorScheme.onSurface

    val animProgress by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label         = "weeklyBarAnim"
    )

    val maxCount = weeklyActivity.maxOfOrNull { it.completedCount }?.coerceAtLeast(1) ?: 1
    // Today is the last element (index 6)
    val todayIndex = weeklyActivity.lastIndex

    Canvas(modifier = modifier) {
        val count       = weeklyActivity.size.coerceAtLeast(1)
        val totalWidth  = size.width
        val totalHeight = size.height
        val labelHeight = 28.dp.toPx()
        val countHeight = 22.dp.toPx()
        val barAreaTop  = countHeight
        val barAreaBot  = totalHeight - labelHeight
        val barAreaH    = barAreaBot - barAreaTop

        val gap      = 8.dp.toPx()
        val barWidth = (totalWidth - gap * (count + 1)) / count
        val cornerR  = 6.dp.toPx()

        weeklyActivity.forEachIndexed { index, day ->
            val barHeight = (day.completedCount.toFloat() / maxCount) * barAreaH * animProgress
            val left      = gap + index * (barWidth + gap)
            val barTop    = barAreaBot - barHeight
            val color     = if (index == todayIndex) primaryColor else secondaryContainerColor

            // Background track
            drawRoundRect(
                color        = secondaryContainerColor.copy(alpha = 0.35f),
                topLeft      = Offset(left, barAreaTop),
                size         = Size(barWidth, barAreaH),
                cornerRadius = CornerRadius(cornerR)
            )
            // Filled bar
            if (barHeight > 0f) {
                drawRoundRect(
                    color        = color,
                    topLeft      = Offset(left, barTop),
                    size         = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerR)
                )
            }
            // Count above bar
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textSize    = 11.sp.toPx()
                    textAlign   = android.graphics.Paint.Align.CENTER
                    this.color  = labelColor.toArgb()
                    isFakeBoldText = true
                }
                if (day.completedCount > 0) {
                    canvas.nativeCanvas.drawText(
                        day.completedCount.toString(),
                        left + barWidth / 2f,
                        (barTop - 4.dp.toPx()).coerceAtLeast(countHeight - 4.dp.toPx()),
                        paint
                    )
                }
            }
            // Day label below bar
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textSize   = 11.sp.toPx()
                    textAlign  = android.graphics.Paint.Align.CENTER
                    this.color = labelColor.copy(alpha = if (index == todayIndex) 1f else 0.65f).toArgb()
                    if (index == todayIndex) isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    day.dayLabel,
                    left + barWidth / 2f,
                    barAreaBot + 20.dp.toPx(),
                    paint
                )
            }
        }
    }
}

// ── 4. Priority breakdown (horizontal bars) ───────────────────────────────────

@Composable
private fun PriorityBreakdownCard(countByPriority: Map<Priority, Int>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                stringResource(R.string.stats_by_priority),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            val order  = listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW, Priority.NONE)
            val total  = countByPriority.values.sumOf { it }.coerceAtLeast(1)
            val colors = mapOf(
                Priority.HIGH   to PriorityHighColor,
                Priority.MEDIUM to PriorityMediumColor,
                Priority.LOW    to PriorityLowColor,
                Priority.NONE   to PriorityNoneColor
            )
            val labels = mapOf(
                Priority.HIGH   to stringResource(R.string.priority_high),
                Priority.MEDIUM to stringResource(R.string.priority_medium),
                Priority.LOW    to stringResource(R.string.priority_low),
                Priority.NONE   to stringResource(R.string.priority_none)
            )

            order.forEach { priority ->
                val count  = countByPriority[priority] ?: 0
                val frac   = count.toFloat() / total.toFloat()
                val pct    = (frac * 100).toInt()
                val color  = colors[priority] ?: PriorityNoneColor
                val label  = labels[priority] ?: priority.name
                HorizontalPriorityBar(
                    label    = label,
                    count    = count,
                    percent  = pct,
                    fraction = frac,
                    color    = color
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun HorizontalPriorityBar(
    label: String,
    count: Int,
    percent: Int,
    fraction: Float,
    color: Color
) {
    val animFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label         = "priorityBar_$label"
    )
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVar = MaterialTheme.colorScheme.surfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        // Priority label
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelMedium,
            color     = onSurface,
            modifier  = Modifier.width(54.dp),
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.width(8.dp))
        // Animated fill bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Track
                drawRoundRect(
                    color        = surfaceVar,
                    cornerRadius = CornerRadius(7.dp.toPx())
                )
                // Fill
                if (animFraction > 0f) {
                    drawRoundRect(
                        color        = color,
                        size         = Size(size.width * animFraction, size.height),
                        cornerRadius = CornerRadius(7.dp.toPx())
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        // Count + percent
        Text(
            text  = "$count ($percent%)",
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.75f),
            modifier = Modifier.width(62.dp),
            textAlign = TextAlign.End
        )
    }
}

// ── 5. Insight row ────────────────────────────────────────────────────────────

@Composable
private fun InsightRow(streakDays: Int, mostProductiveDay: String) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InsightCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Default.LocalFireDepartment,
            iconTint  = Color(0xFFFF6F00),
            title     = if (streakDays == 1) stringResource(R.string.stats_streak_one) else stringResource(R.string.stats_streak_other, streakDays),
            subtitle  = if (streakDays == 0) stringResource(R.string.stats_streak_start) else stringResource(R.string.stats_streak_keep),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor   = MaterialTheme.colorScheme.onTertiaryContainer
        )
        InsightCard(
            modifier  = Modifier.weight(1f),
            icon      = Icons.Default.EmojiEvents,
            iconTint  = Color(0xFFFFC107),
            title     = if (mostProductiveDay.isEmpty()) stringResource(R.string.stats_no_data) else mostProductiveDay,
            subtitle  = stringResource(R.string.stats_most_productive),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun InsightCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(32.dp)
            )
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = contentColor
            )
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

// ── 6. 30-day Activity Heatmap ────────────────────────────────────────────────

@Composable
private fun HeatmapCard(counts: List<Int>) {
    val maxCount  = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val baseColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text       = stringResource(R.string.stats_heatmap_title),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // 6 rows × 5 cols = 30 cells
            val cols = 6
            val rows = 5
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                for (row in 0 until rows) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        for (col in 0 until cols) {
                            val idx   = row * cols + col
                            val count = if (idx < counts.size) counts[idx] else 0
                            val alpha = if (count == 0) 0f else (count.toFloat() / maxCount).coerceIn(0.15f, 1f)
                            val color = if (count == 0) emptyColor else baseColor.copy(alpha = alpha)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
            // Legend
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = stringResource(R.string.stats_heatmap_less),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(0.15f, 0.35f, 0.55f, 0.75f, 1.0f).forEach { a ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(baseColor.copy(alpha = a))
                        )
                    }
                }
                Text(
                    text  = stringResource(R.string.stats_heatmap_more),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
