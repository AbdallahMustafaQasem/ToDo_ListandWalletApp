package com.abdallah.taskvault.ui.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R

private val EMOJI_CHOICES = listOf("⭐","🏃","💪","📚","🧘","🥗","💧","🎵","✏️","🌿","😴","🧹","💊","🛡️","🎯")
private val COLOR_CHOICES = listOf("#6750A4","#E53935","#43A047","#FB8C00","#1E88E5","#8E24AA","#00897B","#F4511E")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id == -1L) stringResource(R.string.habit_detail_new)
                        else stringResource(R.string.habit_detail_edit),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = viewModel::save, enabled = state.name.isNotBlank()) {
                        Icon(Icons.Default.Check, stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.habit_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.habit_field_description)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            Text(stringResource(R.string.habit_field_emoji), style = MaterialTheme.typography.labelLarge)
            EmojiGrid(selected = state.emoji, onSelect = viewModel::onEmojiChange)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiGrid(selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EMOJI_CHOICES.forEach { emoji ->
            val isSelected = emoji == selected
            Surface(
                onClick = { onSelect(emoji) },
                shape = MaterialTheme.shapes.small,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Text(emoji, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
