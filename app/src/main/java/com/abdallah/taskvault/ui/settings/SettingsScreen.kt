package com.abdallah.taskvault.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog  by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            current   = uiState.languageCode,
            onDismiss = { showLanguageDialog = false },
            onSelect  = { code ->
                viewModel.setLanguage(code)
                showLanguageDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            current   = uiState.currencySymbol,
            onDismiss = { showCurrencyDialog = false },
            onSelect  = { symbol ->
                viewModel.setCurrency(symbol)
                showCurrencyDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {

            // ── Account ──────────────────────────────────────────────
            item { SettingsSectionHeader(stringResource(R.string.settings_section_account)) }
            item {
                val user = uiState.currentUser
                SettingsItem(
                    icon      = Icons.Default.AccountCircle,
                    title     = user?.displayName ?: stringResource(R.string.settings_not_signed_in),
                    subtitle  = user?.email,
                    onClick   = onNavigateToProfile
                )
            }

            // ── Appearance ───────────────────────────────────────────
            item { SettingsSectionHeader(stringResource(R.string.settings_section_appearance)) }

            item {
                ThemeSetting(
                    current  = uiState.isDarkTheme,
                    onChange = { viewModel.setTheme(it) }
                )
            }

            item {
                val languageLabels = mapOf(
                    null to stringResource(R.string.language_system_default),
                    "en" to "English", "ar" to "العربية", "bn" to "বাংলা",
                    "de" to "Deutsch", "es" to "Español", "fr" to "Français",
                    "hi" to "हिन्दी", "ja" to "日本語", "pt" to "Português",
                    "ru" to "Русский", "ur" to "اردو", "zh" to "中文"
                )
                SettingsItem(
                    icon     = Icons.Default.Language,
                    title    = stringResource(R.string.menu_language),
                    subtitle = languageLabels[uiState.languageCode] ?: stringResource(R.string.language_system_default),
                    onClick  = { showLanguageDialog = true }
                )
            }

            // ── Wallet ───────────────────────────────────────────────
            item { SettingsSectionHeader(stringResource(R.string.settings_section_wallet)) }
            item {
                val currencyDisplay = if (uiState.currencySymbol.isBlank())
                    stringResource(R.string.wallet_currency_no_symbol)
                else uiState.currencySymbol
                SettingsItem(
                    icon     = Icons.Default.AttachMoney,
                    title    = stringResource(R.string.wallet_change_currency),
                    subtitle = currencyDisplay,
                    onClick  = { showCurrencyDialog = true }
                )
            }

            // ── Security ─────────────────────────────────────────────
            item { SettingsSectionHeader(stringResource(R.string.settings_section_security)) }
            item {
                SettingsToggleItem(
                    icon     = Icons.Default.Lock,
                    title    = stringResource(R.string.menu_app_lock),
                    subtitle = stringResource(
                        if (uiState.appLockEnabled) R.string.settings_app_lock_on
                        else R.string.settings_app_lock_off
                    ),
                    checked  = uiState.appLockEnabled,
                    onToggle = { viewModel.setAppLock(it) }
                )
            }

            // ── Info ─────────────────────────────────────────────────
            item { SettingsSectionHeader(stringResource(R.string.settings_section_info)) }
            item {
                SettingsItem(
                    icon    = Icons.Default.Info,
                    title   = stringResource(R.string.menu_about),
                    onClick = onNavigateToAbout
                )
            }
        }
    }
}

// ─── Section header ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelMedium,
        color    = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ─── Standard tappable row ───────────────────────────────────────────────────

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent  = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = subtitle?.let { { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        leadingContent   = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                }
            }
        },
        trailingContent  = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

// ─── Toggle row ──────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent  = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = subtitle?.let { { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        leadingContent   = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                }
            }
        },
        trailingContent  = { Switch(checked = checked, onCheckedChange = onToggle) }
    )
}

// ─── Theme segmented control ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSetting(
    current: Boolean?,
    onChange: (Boolean?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = when (current) {
                            true  -> Icons.Default.DarkMode
                            false -> Icons.Default.LightMode
                            null  -> Icons.Default.BrightnessMedium
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.bodyLarge)
        }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = current == null,
                onClick  = { onChange(null) },
                shape    = SegmentedButtonDefaults.itemShape(0, 3),
                label    = { Text(stringResource(R.string.settings_theme_system)) }
            )
            SegmentedButton(
                selected = current == false,
                onClick  = { onChange(false) },
                shape    = SegmentedButtonDefaults.itemShape(1, 3),
                label    = { Text(stringResource(R.string.settings_theme_light)) }
            )
            SegmentedButton(
                selected = current == true,
                onClick  = { onChange(true) },
                shape    = SegmentedButtonDefaults.itemShape(2, 3),
                label    = { Text(stringResource(R.string.settings_theme_dark)) }
            )
        }
    }
}

// ─── Language dialog ─────────────────────────────────────────────────────────

private data class LangOption(val code: String?, val label: String)

@Composable
private fun LanguagePickerDialog(
    current: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    val options = listOf(
        LangOption(null, stringResource(R.string.language_system_default)),
        LangOption("en", "English"), LangOption("ar", "العربية"),
        LangOption("bn", "বাংলা"),  LangOption("de", "Deutsch"),
        LangOption("es", "Español"), LangOption("fr", "Français"),
        LangOption("hi", "हिन्दी"), LangOption("ja", "日本語"),
        LangOption("pt", "Português"), LangOption("ru", "Русский"),
        LangOption("ur", "اردو"),   LangOption("zh", "中文")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_dialog_title)) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                options.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(opt.code) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = current == opt.code, onClick = { onSelect(opt.code) })
                        Spacer(Modifier.width(8.dp))
                        Text(opt.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

// ─── Currency dialog ─────────────────────────────────────────────────────────

private data class CurrOption(val symbol: String, val label: String)

@Composable
private fun CurrencyPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val options = listOf(
        CurrOption("",  stringResource(R.string.wallet_currency_no_symbol)),
        CurrOption("$", "$ — Dollar"),  CurrOption("€", "€ — Euro"),
        CurrOption("£", "£ — Pound"),   CurrOption("¥", "¥ — Yen"),
        CurrOption("₹", "₹ — Rupee"),   CurrOption("₽", "₽ — Ruble"),
        CurrOption("₺", "₺ — Lira"),    CurrOption("₩", "₩ — Won"),
        CurrOption("₱", "₱ — Peso"),    CurrOption("₫", "₫ — Dong")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wallet_currency_title)) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                options.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(opt.symbol) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = current == opt.symbol, onClick = { onSelect(opt.symbol) })
                        Spacer(Modifier.width(8.dp))
                        Text(opt.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
