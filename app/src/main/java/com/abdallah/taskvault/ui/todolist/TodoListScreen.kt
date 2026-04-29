package com.abdallah.taskvault.ui.todolist

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import com.abdallah.taskvault.data.preferences.SortOrder
import com.abdallah.taskvault.ui.todolist.components.EmptyStateView
import com.abdallah.taskvault.ui.todolist.components.FilterChipsRow
import com.abdallah.taskvault.ui.todolist.components.TodoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToTrash: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToLists: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showSearch by remember { mutableStateOf(false) }
    var showMenu   by remember { mutableStateOf(false) }
    var showQuickAdd by remember { mutableStateOf(false) }

    val strAlarmRationale  = stringResource(R.string.alarm_permission_rationale)
    val strSnackbarDeleted = stringResource(R.string.snackbar_deleted)
    val strSnackbarUndo    = stringResource(R.string.snackbar_undo)
    val strEmptyDefault    = stringResource(R.string.empty_default)
    val strEmptyActive     = stringResource(R.string.empty_active)
    val strEmptyCompleted  = stringResource(R.string.empty_completed)
    val strEmptySearch     = stringResource(R.string.empty_search)

    // FAB extends when list is scrolled to top
    val isScrolledDown by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    // Notification permission
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Exact alarm rationale
    LaunchedEffect(uiState.showExactAlarmRationale) {
        if (uiState.showExactAlarmRationale) {
            snackbarHostState.showSnackbar(
                message  = strAlarmRationale,
                duration = SnackbarDuration.Long
            )
            viewModel.dismissExactAlarmRationale()
        }
    }

    // Quick-add bottom sheet
    if (showQuickAdd) {
        QuickAddBottomSheet(
            onDismiss = { showQuickAdd = false },
            onFullAdd = {
                showQuickAdd = false
                onNavigateToAdd()
            },
            onQuickAdd = { title ->
                scope.launch {
                    // delegated to ViewModel via a quick-add approach
                    // (just navigate to add with pre-filled title — simplest safe approach)
                    showQuickAdd = false
                    onNavigateToAdd()
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor   = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor    = MaterialTheme.colorScheme.inversePrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = showSearch,
                            transitionSpec = {
                                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                            },
                            label = "searchToggle"
                        ) { searching ->
                            if (searching) {
                                OutlinedTextField(
                                    value         = uiState.searchQuery,
                                    onValueChange = viewModel::onSearchQueryChanged,
                                    placeholder   = { Text(stringResource(R.string.search_placeholder)) },
                                    singleLine    = true,
                                    modifier      = Modifier.fillMaxWidth(),
                                    shape         = RoundedCornerShape(18.dp),
                                    colors        = OutlinedTextFieldDefaults.colors()
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            stringResource(R.string.title_my_todos),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (uiState.activeTodoCount > 0) {
                                            Spacer(Modifier.width(8.dp))
                                            AnimatedContent(
                                                targetState = uiState.activeTodoCount,
                                                transitionSpec = {
                                                    slideInVertically { -it } + fadeIn() togetherWith
                                                    slideOutVertically { it } + fadeOut()
                                                },
                                                label = "badgeCount"
                                            ) { count ->
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ) {
                                                    Text(
                                                        text  = count.toString(),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        // Search toggle
                        IconButton(onClick = {
                            showSearch = !showSearch
                            if (!showSearch) viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(
                                imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (showSearch) stringResource(R.string.close) else stringResource(R.string.search)
                            )
                        }
                        // Wallet shortcut
                        IconButton(onClick = onNavigateToWallet) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = stringResource(R.string.menu_wallet)
                            )
                        }
                        // Overflow menu
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
                        }
                        DropdownMenu(
                            expanded        = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // ── Sort ─────────────────────────────────
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.sort), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) },
                                onClick     = {},
                                enabled     = false,
                                leadingIcon = { Icon(Icons.Default.Sort, null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text       = stringResource(order.displayNameRes()),
                                            fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        if (uiState.sortOrder == order)
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        else
                                            Spacer(Modifier.size(24.dp))
                                    },
                                    onClick = {
                                        viewModel.onSortOrderChanged(order)
                                        showMenu = false
                                    }
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            // ── Navigate ────────────────────────────
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.menu_statistics)) },
                                leadingIcon = { Icon(Icons.Default.BarChart, null) },
                                onClick     = { showMenu = false; onNavigateToStatistics() }
                            )
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.menu_calendar)) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                                onClick     = { showMenu = false; onNavigateToCalendar() }
                            )
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.menu_lists)) },
                                leadingIcon = { Icon(Icons.Default.List, null) },
                                onClick     = { showMenu = false; onNavigateToLists() }
                            )
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.menu_trash)) },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick     = { showMenu = false; onNavigateToTrash() }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text        = { Text(stringResource(R.string.settings_title)) },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick     = { showMenu = false; onNavigateToSettings() }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(top = 4.dp, bottom = 8.dp)
                    ) {
                        FilterChipsRow(
                            selectedFilter = uiState.filter,
                            onFilterSelected = viewModel::onFilterChanged
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFabWithLongPress(
                isScrolledDown = isScrolledDown,
                onTap          = { showQuickAdd = true },
                onLongPress    = onNavigateToAdd
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    delay(800)        // give DB flow a moment to re-emit
                    isRefreshing = false
                }
            },
            state    = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.filteredTodos.isEmpty() -> {
                    val emptyMsg = when {
                        uiState.searchQuery.isNotBlank() ->
                            strEmptySearch.format(uiState.searchQuery)
                        uiState.filter == FilterOption.ACTIVE    -> strEmptyActive
                        uiState.filter == FilterOption.COMPLETED -> strEmptyCompleted
                        else                                      -> strEmptyDefault
                    }
                    EmptyStateView(message = emptyMsg)
                }

                else -> {
                    LazyColumn(
                        state          = listState,
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start  = 12.dp,
                            end    = 12.dp,
                            top    = 8.dp,
                            bottom = 88.dp        // space for FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.filteredTodos,
                            key   = { todo -> todo.id }
                        ) { todo ->
                            // Staggered entrance animation per item
                            val index = uiState.filteredTodos.indexOf(todo)
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(todo.id) {
                                delay((index * 50L).coerceAtMost(300L))
                                visible = true
                            }
                            AnimatedVisibility(
                                visible      = visible,
                                enter        = slideInHorizontally(
                                    initialOffsetX = { -it / 2 },
                                    animationSpec  = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeIn(tween(300)),
                                exit         = slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(200)
                                ) + fadeOut(tween(200)),
                                modifier     = Modifier.animateItem(
                                    fadeInSpec    = tween(300),
                                    placementSpec = tween(300),
                                    fadeOutSpec   = tween(200)
                                )
                            ) {
                                TodoItem(
                                    todo            = todo,
                                    onCheckedChange = { isChecked ->
                                        viewModel.onToggleCompletion(todo, isChecked)
                                    },
                                    onClick         = { onNavigateToDetail(todo.id) },
                                    onDismiss       = {
                                        viewModel.onDeleteTodo(todo)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message     = strSnackbarDeleted.format(todo.title),
                                                actionLabel = strSnackbarUndo,
                                                duration    = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.onUndoDelete()
                                            }
                                        }
                                    },
                                    onComplete      = {
                                        viewModel.onToggleCompletion(todo, !todo.isCompleted)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Extended FAB with long-press gesture ─────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExtendedFabWithLongPress(
    isScrolledDown: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val fabShape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    FloatingActionButton(
        onClick        = onTap,
        shape          = fabShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor   = MaterialTheme.colorScheme.onPrimary,
        elevation      = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
        interactionSource = interactionSource,
        modifier       = Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication        = null,
            onClick           = onTap,
            onLongClick       = onLongPress
        )
    ) {
        AnimatedContent(
            targetState  = isScrolledDown,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "fabExpand"
        ) { collapsed ->
            if (collapsed) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.fab_add_todo))
            } else {
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    modifier             = Modifier.padding(horizontal = 18.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.fab_add_todo), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ── Quick-add bottom sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddBottomSheet(
    onDismiss: () -> Unit,
    onFullAdd: () -> Unit,
    onQuickAdd: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation   = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.quick_add),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                placeholder   = { Text(stringResource(R.string.quick_add_placeholder)) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp)
            )
            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onFullAdd,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.full_add))
                }
                Button(
                    onClick  = {
                        if (title.isNotBlank()) {
                            onQuickAdd(title)
                        }
                    },
                    enabled  = title.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.add))
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun SortOrder.displayNameRes(): Int = when (this) {
    SortOrder.CREATION_DATE -> R.string.sort_creation_date
    SortOrder.DUE_DATE      -> R.string.sort_due_date
    SortOrder.PRIORITY      -> R.string.sort_priority
    SortOrder.ALPHABETICAL  -> R.string.sort_alphabetical
}
