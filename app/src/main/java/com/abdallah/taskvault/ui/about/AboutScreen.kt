package com.abdallah.taskvault.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdallah.taskvault.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val packageName = context.packageName

    // ── Entrance animations ──
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Pulsing logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(12.dp))

            // ── App Logo + Name ──
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(tween(600, easing = EaseOutBack)) + fadeIn(tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        // Glow ring
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .alpha(glowAlpha),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {}
                        // Logo circle
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .scale(pulseScale),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "T",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.about_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.about_tagline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Description Card ──
            AnimatedCard(visible = visible, delayMs = 150) {
                Text(
                    stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Feature Highlights ──
            AnimatedCard(visible = visible, delayMs = 300) {
                Text(
                    stringResource(R.string.about_features_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                FeatureRow(Icons.Default.CheckCircle, stringResource(R.string.about_feature_tasks))
                FeatureRow(Icons.Default.StickyNote2, stringResource(R.string.about_feature_notes))
                FeatureRow(Icons.Default.Book, stringResource(R.string.about_feature_memoirs))
                FeatureRow(Icons.Default.Lock, stringResource(R.string.about_feature_passwords))
                FeatureRow(Icons.Default.AccountBalanceWallet, stringResource(R.string.about_feature_wallet))
                FeatureRow(Icons.Default.Loop, stringResource(R.string.about_feature_habits))
                FeatureRow(Icons.Default.Receipt, stringResource(R.string.about_feature_bills))
                FeatureRow(Icons.Default.Cloud, stringResource(R.string.about_feature_sync))
            }

            Spacer(Modifier.height(16.dp))

            // ── Rate & Share ──
            AnimatedCard(visible = visible, delayMs = 450) {
                Text(
                    stringResource(R.string.about_enjoying),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rate button
                    Button(
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                                )
                            } catch (e: ActivityNotFoundException) {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.about_rate))
                    }

                    // Share button
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    context.getString(R.string.about_share_message, "https://play.google.com/store/apps/details?id=$packageName")
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.about_share_via)))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.about_share))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Tech Stack ──
            AnimatedCard(visible = visible, delayMs = 600) {
                Text(
                    stringResource(R.string.about_built_with),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                FlowChips(
                    listOf(
                        "Kotlin", "Jetpack Compose", "Material 3",
                        "Room DB", "Hilt", "Firebase",
                        "Coroutines", "DataStore", "Clean Architecture"
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Developer ──
            AnimatedCard(visible = visible, delayMs = 750) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            stringResource(R.string.about_developer_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(R.string.about_developer_name),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Footer ──
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 900))
            ) {
                Text(
                    stringResource(R.string.about_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Animated Card wrapper with stagger delay ──
@Composable
private fun AnimatedCard(
    visible: Boolean,
    delayMs: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs.toLong())
            show = true
        }
    }
    AnimatedVisibility(
        visible = show,
        enter = slideInVertically(tween(450, easing = EaseOutCubic)) { it / 3 } + fadeIn(tween(450))
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

// ── Feature row ──
@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon, null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

// ── Tech stack chips ──
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(items: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { label ->
            SuggestionChip(
                onClick = {},
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                shape = MaterialTheme.shapes.small
            )
        }
    }
}
