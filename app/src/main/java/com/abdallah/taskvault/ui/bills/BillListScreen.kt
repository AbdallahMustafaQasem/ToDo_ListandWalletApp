package com.abdallah.taskvault.ui.bills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Bill
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNew: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: BillListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.bills_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNew, containerColor = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Default.Add, stringResource(R.string.bills_fab_new))
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.bills.isEmpty() -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Receipt, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Text(stringResource(R.string.bills_empty_default), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 88.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.bills, key = { it.id }) { bill ->
                        BillCard(
                            bill = bill,
                            isOverdue = viewModel.isOverdue(bill),
                            isDueSoon = viewModel.isDueSoon(bill),
                            onMarkPaid = { viewModel.markAsPaid(bill) },
                            onEdit = { onNavigateToEdit(bill.id) },
                            onDelete = { viewModel.delete(bill) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillCard(
    bill: Bill,
    isOverdue: Boolean,
    isDueSoon: Boolean,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val numFmt = remember { NumberFormat.getCurrencyInstance() }

    val statusColor = when {
        bill.isPaid -> Color(0xFF43A047)
        isOverdue   -> Color(0xFFE53935)
        isDueSoon   -> Color(0xFFFB8C00)
        else        -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusLabel = when {
        bill.isPaid -> stringResource(R.string.bill_paid)
        isOverdue   -> stringResource(R.string.bill_overdue)
        isDueSoon   -> stringResource(R.string.bill_due_soon)
        else        -> stringResource(R.string.bill_schedule_next, fmt.format(Date(bill.nextDueDateMillis)))
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(bill.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(numFmt.format(bill.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = statusColor.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
                        Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.SemiBold)
                    }
                    if (bill.reminderEnabled) {
                        Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            if (!bill.isPaid) {
                IconButton(onClick = onMarkPaid) {
                    Icon(Icons.Default.CheckCircle, stringResource(R.string.bill_mark_paid), tint = Color(0xFF43A047))
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { showMenu = false; onEdit() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}
