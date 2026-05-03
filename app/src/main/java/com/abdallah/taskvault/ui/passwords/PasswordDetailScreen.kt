package com.abdallah.taskvault.ui.passwords

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.abdallah.taskvault.utils.PasswordGenerator
import com.abdallah.taskvault.utils.PasswordStrength
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PasswordDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    val canSave = state.title.isNotBlank() && state.password.isNotBlank()

    var showGenerator by remember { mutableStateOf(false) }
    var genLength by remember { mutableStateOf(16f) }
    var genUppercase by remember { mutableStateOf(true) }
    var genNumbers by remember { mutableStateOf(true) }
    var genSymbols by remember { mutableStateOf(true) }
    var generatedPwd by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id == -1L) stringResource(R.string.password_detail_new)
                        else stringResource(R.string.password_detail_edit)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::save, enabled = canSave && !state.isSaving) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, stringResource(R.string.save))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.password_field_title)) },
                leadingIcon = { Icon(Icons.Default.Label, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text(stringResource(R.string.password_field_username)) },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(R.string.password_field_password)) },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                if (showPassword) stringResource(R.string.password_hide)
                                else stringResource(R.string.password_show)
                            )
                        }
                        IconButton(onClick = { clipboard.setText(AnnotatedString(state.password)) }) {
                            Icon(Icons.Default.ContentCopy, stringResource(R.string.password_copy))
                        }
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedButton(
                onClick = {
                    showGenerator = !showGenerator
                    if (!showGenerator) generatedPwd = ""
                    else generatedPwd = PasswordGenerator.generate(
                        genLength.toInt(), genUppercase, genNumbers, genSymbols
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.gen_password_title))
            }

            if (showGenerator) {
                PasswordGeneratorPanel(
                    length        = genLength,
                    uppercase     = genUppercase,
                    numbers       = genNumbers,
                    symbols       = genSymbols,
                    generated     = generatedPwd,
                    onLengthChange    = { genLength = it; generatedPwd = PasswordGenerator.generate(it.toInt(), genUppercase, genNumbers, genSymbols) },
                    onUppercaseToggle = { genUppercase = !genUppercase; generatedPwd = PasswordGenerator.generate(genLength.toInt(), genUppercase, genNumbers, genSymbols) },
                    onNumbersToggle   = { genNumbers = !genNumbers; generatedPwd = PasswordGenerator.generate(genLength.toInt(), genUppercase, genNumbers, genSymbols) },
                    onSymbolsToggle   = { genSymbols = !genSymbols; generatedPwd = PasswordGenerator.generate(genLength.toInt(), genUppercase, genNumbers, genSymbols) },
                    onRegenerate  = { generatedPwd = PasswordGenerator.generate(genLength.toInt(), genUppercase, genNumbers, genSymbols) },
                    onUse         = { viewModel.onPasswordChange(generatedPwd); showGenerator = false }
                )
            }

            OutlinedTextField(
                value = state.url,
                onValueChange = viewModel::onUrlChange,
                label = { Text(stringResource(R.string.password_field_url)) },
                leadingIcon = { Icon(Icons.Default.Language, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.password_field_notes)) },
                leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        stringResource(R.string.password_security_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordGeneratorPanel(
    length: Float,
    uppercase: Boolean,
    numbers: Boolean,
    symbols: Boolean,
    generated: String,
    onLengthChange: (Float) -> Unit,
    onUppercaseToggle: () -> Unit,
    onNumbersToggle: () -> Unit,
    onSymbolsToggle: () -> Unit,
    onRegenerate: () -> Unit,
    onUse: () -> Unit
) {
    val strength = PasswordGenerator.strength(generated)
    val (strengthColor, strengthLabel) = when (strength) {
        PasswordStrength.WEAK   -> Color(0xFFE53935) to "Weak"
        PasswordStrength.MEDIUM -> Color(0xFFFB8C00) to "Medium"
        PasswordStrength.STRONG -> Color(0xFF43A047) to "Strong"
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.gen_password_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            if (generated.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(generated, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(strengthLabel, style = MaterialTheme.typography.labelSmall, color = strengthColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(stringResource(R.string.gen_length, length.toInt()), style = MaterialTheme.typography.labelMedium)
            Slider(value = length, onValueChange = onLengthChange, valueRange = 8f..32f, steps = 23)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = uppercase, onClick = onUppercaseToggle, label = { Text("A-Z") }, modifier = Modifier.weight(1f))
                FilterChip(selected = numbers,   onClick = onNumbersToggle,   label = { Text("0-9") }, modifier = Modifier.weight(1f))
                FilterChip(selected = symbols,   onClick = onSymbolsToggle,   label = { Text("!@#") }, modifier = Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRegenerate, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.gen_regenerate))
                }
                Button(onClick = onUse, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.gen_use))
                }
            }
        }
    }
}
