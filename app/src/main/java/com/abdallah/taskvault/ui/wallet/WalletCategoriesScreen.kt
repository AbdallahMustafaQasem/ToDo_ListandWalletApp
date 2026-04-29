package com.abdallah.taskvault.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.WalletCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletCategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingCategory by remember { mutableStateOf<WalletCategory?>(null) }
    var name by remember(editingCategory) { mutableStateOf(editingCategory?.name.orEmpty()) }
    var icon by remember(editingCategory) { mutableStateOf(editingCategory?.icon ?: "💼") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wallet_manage_categories)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingCategory = null
                        name = ""
                        icon = "💼"
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.wallet_add_category))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (editingCategory == null) {
                            stringResource(R.string.wallet_add_category)
                        } else {
                            stringResource(R.string.wallet_edit_category_title)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.wallet_category_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text(stringResource(R.string.wallet_emoji)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (editingCategory != null) {
                            TextButton(onClick = {
                                editingCategory = null
                                name = ""
                                icon = "💼"
                            }) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                        TextButton(onClick = {
                            if (name.isNotBlank()) {
                                viewModel.saveCategory(
                                    id = editingCategory?.id ?: 0L,
                                    name = name,
                                    icon = icon.ifBlank { "💼" },
                                    isDefault = editingCategory?.isDefault ?: false
                                )
                                editingCategory = null
                                name = ""
                                icon = "💼"
                            }
                        }) {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }

            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.wallet_categories_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    uiState.categories.forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${category.icon} ${category.name}")
                            Row {
                                IconButton(onClick = {
                                    editingCategory = category
                                    name = category.name
                                    icon = category.icon
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.wallet_edit_category))
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.deleteCategory(category)
                                        if (editingCategory?.id == category.id) {
                                            editingCategory = null
                                            name = ""
                                            icon = "💼"
                                        }
                                    },
                                    enabled = !category.isDefault
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.wallet_delete_category))
                                }
                            }
                        }
                    }
                    if (uiState.categories.isEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
