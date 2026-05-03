package com.abdallah.taskvault.ui.memoirs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import java.text.SimpleDateFormat
import java.util.*

private val MOOD_OPTIONS = listOf("😊", "😄", "😌", "😢", "😤", "😴", "🤔", "❤️", "🔥", "✨")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoirDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: MemoirDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fmt = remember { SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault()) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.memoir_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveMemoir() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveMemoir() }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save), tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(uiState.mood, style = MaterialTheme.typography.displaySmall)
                Column {
                    Text(
                        text = fmt.format(Date(uiState.dateMillis)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.memoir_mood_question),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(stringResource(R.string.memoir_mood_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MOOD_OPTIONS) { mood ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.mood == mood) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { viewModel.onMoodChanged(mood) }
                    ) {
                        Text(
                            text = mood,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            HorizontalDivider()

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                placeholder = { Text(stringResource(R.string.memoir_field_title_placeholder)) },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChanged,
                placeholder = { Text(stringResource(R.string.memoir_field_content_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                minLines = 10
            )
        }
    }
}
