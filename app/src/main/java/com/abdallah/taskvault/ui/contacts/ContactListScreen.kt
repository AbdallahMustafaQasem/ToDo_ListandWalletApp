package com.abdallah.taskvault.ui.contacts

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    onNavigateBack: () -> Unit,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.contacts_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.contacts_add))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.contacts_search)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.contacts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.contacts_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.contacts_empty_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.contacts, key = { it.id }) { contact ->
                        ContactCard(
                            contact = contact,
                            onEdit = { viewModel.showEditDialog(contact) },
                            onDelete = { viewModel.deleteContact(contact) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // Add dialog
        if (state.showAddDialog) {
            ContactFormDialog(
                title = stringResource(R.string.contacts_add),
                userId = state.addUserId,
                displayName = state.addDisplayName,
                role = state.addRole,
                onUserIdChange = viewModel::onUserIdChange,
                onDisplayNameChange = viewModel::onDisplayNameChange,
                onRoleChange = viewModel::onRoleChange,
                onConfirm = viewModel::addContact,
                onDismiss = viewModel::hideAddDialog,
                userIdEnabled = true
            )
        }

        // Edit dialog
        if (state.showEditDialog) {
            ContactFormDialog(
                title = stringResource(R.string.contacts_edit),
                userId = state.addUserId,
                displayName = state.addDisplayName,
                role = state.addRole,
                onUserIdChange = {},
                onDisplayNameChange = viewModel::onDisplayNameChange,
                onRoleChange = viewModel::onRoleChange,
                onConfirm = viewModel::updateContact,
                onDismiss = viewModel::hideEditDialog,
                userIdEnabled = false
            )
        }
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        try { Color(contact.avatarColor.toColorInt()) }
                        catch (_: Exception) { MaterialTheme.colorScheme.primary }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    contact.displayName.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contact.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "ID: ${contact.userId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (contact.role.isNotBlank()) {
                    Text(
                        contact.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactFormDialog(
    title: String,
    userId: String,
    displayName: String,
    role: String,
    onUserIdChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    userIdEnabled: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = onUserIdChange,
                    label = { Text(stringResource(R.string.contacts_field_user_id)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = userIdEnabled,
                    leadingIcon = { Icon(Icons.Default.Fingerprint, null) }
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text(stringResource(R.string.contacts_field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = onRoleChange,
                    label = { Text(stringResource(R.string.contacts_field_role)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Work, null) }
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
