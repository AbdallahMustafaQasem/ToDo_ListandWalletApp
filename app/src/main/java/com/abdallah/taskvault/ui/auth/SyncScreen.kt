package com.abdallah.taskvault.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SyncScreen(
    message: String = "Syncing your data…",
    onRetry: (() -> Unit)? = null,
    isError: Boolean = false
) {
    val rotation by rememberInfiniteTransition(label = "rotate").animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing)
        ),
        label = "icon_rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            if (isError) {
                Icon(
                    imageVector   = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text      = "Sync failed",
                    style     = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color     = MaterialTheme.colorScheme.error
                )
                Text(
                    text      = message,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (onRetry != null) {
                    Button(onClick = onRetry) { Text("Retry") }
                }
            } else {
                Icon(
                    imageVector   = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text      = "Syncing",
                    style     = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text      = message,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
            }
        }
    }
}
