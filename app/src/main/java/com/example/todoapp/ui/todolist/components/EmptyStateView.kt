package com.example.todoapp.ui.todolist.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyStateView(
    message: String = "No todos yet.\nTap + to add one!",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyStateAnimation(modifier = Modifier.size(160.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyStateAnimation(modifier: Modifier = Modifier) {
    // Infinite float animation driving everything
    val infiniteTransition = rememberInfiniteTransition(label = "emptyAnim")

    // Floating bob
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Checkmark draw progress
    val checkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "check"
    )

    // Circle pulse alpha
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.4f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primary      = MaterialTheme.colorScheme.primary
    val secondary    = MaterialTheme.colorScheme.secondary
    val surfaceVar   = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2 - bobOffset

        // Outer pulse circle
        drawCircle(
            color  = primary.copy(alpha = pulseAlpha),
            radius = size.minDimension * 0.48f,
            center = Offset(cx, cy)
        )

        // Main circle
        drawCircle(
            color  = surfaceVar,
            radius = size.minDimension * 0.38f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color  = primary,
            radius = size.minDimension * 0.38f,
            center = Offset(cx, cy),
            style  = Stroke(width = 4.dp.toPx())
        )

        // Animated checkmark drawn as Path
        val r   = size.minDimension * 0.20f
        val strokeW = 5.dp.toPx()

        // checkmark: start from left-bottom, go to mid-bottom, then up-right
        val startX   = cx - r * 0.55f
        val startY   = cy
        val midX     = cx - r * 0.10f
        val midY     = cy + r * 0.45f
        val endX     = cx + r * 0.65f
        val endY     = cy - r * 0.55f

        // total path length in 2 segments
        val seg1Len = Math.sqrt(
            ((midX - startX) * (midX - startX) + (midY - startY) * (midY - startY)).toDouble()
        ).toFloat()
        val seg2Len = Math.sqrt(
            ((endX - midX) * (endX - midX) + (endY - midY) * (endY - midY)).toDouble()
        ).toFloat()
        val total = seg1Len + seg2Len

        val drawn = total * checkProgress.coerceIn(0f, 1f)

        val path = Path()
        if (drawn > 0f) {
            path.moveTo(startX, startY)
            if (drawn <= seg1Len) {
                val t = drawn / seg1Len
                path.lineTo(startX + t * (midX - startX), startY + t * (midY - startY))
            } else {
                path.lineTo(midX, midY)
                val t2 = (drawn - seg1Len) / seg2Len
                path.lineTo(midX + t2 * (endX - midX), midY + t2 * (endY - midY))
            }
        }

        drawPath(
            path  = path,
            color = primary,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Three small lines below (checklist items hint)
        val lineY1  = cy + size.minDimension * 0.52f + bobOffset * 0.2f
        val lineX1s = cx - size.minDimension * 0.30f
        val lineX1e = cx + size.minDimension * 0.25f
        val gap2    = 10.dp.toPx()

        listOf(0, 1, 2).forEach { i ->
            val ly = lineY1 + i * gap2
            val endFrac = when (i) { 0 -> 1f; 1 -> 0.7f; else -> 0.5f }
            drawLine(
                color       = secondary.copy(alpha = 0.4f),
                start       = Offset(lineX1s, ly),
                end         = Offset(lineX1s + (lineX1e - lineX1s) * endFrac, ly),
                strokeWidth = 3.dp.toPx(),
                cap         = StrokeCap.Round
            )
        }
    }
}
