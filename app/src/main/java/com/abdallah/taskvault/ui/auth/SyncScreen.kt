package com.abdallah.taskvault.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abdallah.taskvault.R

@Composable
fun SyncScreen(
    message: String = "",
    onRetry: (() -> Unit)? = null,
    isError: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            if (isError) {
                // ── Error state ───────────────────────────────────────
                Surface(
                    shape    = CircleShape,
                    color    = scheme.errorContainer,
                    modifier = Modifier.size(104.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector        = Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier           = Modifier.size(52.dp),
                            tint               = scheme.onErrorContainer
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.sync_failed_title),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = scheme.error
                    )
                    Text(
                        text      = message,
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                if (onRetry != null) {
                    Button(
                        onClick  = onRetry,
                        modifier = Modifier.fillMaxWidth(0.65f)
                    ) {
                        Text(stringResource(R.string.sync_retry))
                    }
                }

            } else {
                // ── Syncing state ─────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier.size(104.dp)
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp,
                        color       = scheme.primary
                    )
                    Icon(
                        imageVector        = Icons.Default.Cloud,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                        tint               = scheme.primary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.sync_title),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (message.isNotBlank()) {
                        Text(
                            text      = message,
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
