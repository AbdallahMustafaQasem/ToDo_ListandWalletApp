package com.abdallah.taskvault.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val confettiColors = listOf(
    Color(0xFF6750A4), Color(0xFFEC407A), Color(0xFF29B6F6),
    Color(0xFF66BB6A), Color(0xFFFFCA28), Color(0xFFFF7043)
)

private data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    active: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    if (!active) return

    val particleCount = 60
    val particles = remember {
        List(particleCount) {
            Particle(
                x             = Random.nextFloat(),
                y             = Random.nextFloat() * 0.3f,
                vx            = (Random.nextFloat() - 0.5f) * 0.006f,
                vy            = Random.nextFloat() * 0.008f + 0.004f,
                color         = confettiColors.random(),
                size          = Random.nextFloat() * 12f + 6f,
                rotation      = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 8f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(16, easing = LinearEasing)),
        label = "confettiTick"
    )

    var frame by remember { mutableIntStateOf(0) }
    var particleState by remember { mutableStateOf(particles) }

    LaunchedEffect(active) {
        if (!active) return@LaunchedEffect
        kotlinx.coroutines.delay(2000)
        onFinished()
    }

    LaunchedEffect(tick) {
        frame++
        if (frame % 2 == 0) {
            particleState = particleState.map { p ->
                p.copy(
                    y        = p.y + p.vy,
                    x        = p.x + p.vx,
                    rotation = p.rotation + p.rotationSpeed
                )
            }
        }
    }

    Canvas(modifier = modifier) {
        particleState.forEach { p ->
            val px = p.x * size.width
            val py = p.y * size.height
            if (py < size.height + 50f) {
                rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                    drawRect(
                        color    = p.color,
                        topLeft  = Offset(px - p.size / 2, py - p.size / 4),
                        size     = androidx.compose.ui.geometry.Size(p.size, p.size / 2)
                    )
                }
            }
        }
    }
}
