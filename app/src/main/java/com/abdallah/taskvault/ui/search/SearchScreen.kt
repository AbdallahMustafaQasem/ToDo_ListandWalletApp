package com.abdallah.taskvault.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTodo: (Long) -> Unit,
    onNavigateToNote: (Long) -> Unit,
    onNavigateToMemoir: (Long) -> Unit,
    onNavigateToPassword: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.large
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.query.isBlank() -> SearchIdle()
                state.isEmpty -> SearchEmpty()
                else -> SearchResults(
                    state = state,
                    onTodoClick = onNavigateToTodo,
                    onNoteClick = onNavigateToNote,
                    onMemoirClick = onNavigateToMemoir,
                    onPasswordClick = onNavigateToPassword
                )
            }
        }
    }
}

@Composable
private fun SearchIdle() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Text(stringResource(R.string.search_empty_default),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SearchEmpty() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Text(stringResource(R.string.search_empty_results),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SearchResults(
    state: SearchUiState,
    onTodoClick: (Long) -> Unit,
    onNoteClick: (Long) -> Unit,
    onMemoirClick: (Long) -> Unit,
    onPasswordClick: (Long) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        if (state.todos.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.search_section_tasks, state.todos.size), Icons.Default.CheckCircle) }
            items(state.todos, key = { "todo_${it.id}" }) { result ->
                ResultRow(result) { onTodoClick(result.id) }
            }
        }
        if (state.notes.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.search_section_notes, state.notes.size), Icons.Default.StickyNote2) }
            items(state.notes, key = { "note_${it.id}" }) { result ->
                ResultRow(result) { onNoteClick(result.id) }
            }
        }
        if (state.memoirs.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.search_section_memoirs, state.memoirs.size), Icons.Default.MenuBook) }
            items(state.memoirs, key = { "memoir_${it.id}" }) { result ->
                ResultRow(result) { onMemoirClick(result.id) }
            }
        }
        if (state.passwords.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.search_section_passwords, state.passwords.size), Icons.Default.Lock) }
            items(state.passwords, key = { "pwd_${it.id}" }) { result ->
                ResultRow(result) { onPasswordClick(result.id) }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(title, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ResultRow(result: SearchResult, onClick: () -> Unit) {
    val icon = when (result.type) {
        SearchResultType.TODO     -> Icons.Default.CheckCircle
        SearchResultType.NOTE     -> Icons.Default.StickyNote2
        SearchResultType.MEMOIR   -> Icons.Default.MenuBook
        SearchResultType.PASSWORD -> Icons.Default.Lock
    }
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(result.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
        },
        supportingContent = if (result.subtitle.isNotBlank()) ({
            Text(result.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }) else null,
        leadingContent = {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}
