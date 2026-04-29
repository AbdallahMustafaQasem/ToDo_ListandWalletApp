package com.abdallah.taskvault.ui.wallet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.TransactionType
import com.abdallah.taskvault.domain.model.WalletCategory
import com.abdallah.taskvault.domain.model.WalletTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private data class CurrencyOption(
    val symbol: String,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTransactionSheet by rememberSaveable { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<WalletTransaction?>(null) }
    var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    val currencyOptions = listOf(
        CurrencyOption(symbol = "", label = stringResource(R.string.wallet_currency_no_symbol)),
        CurrencyOption(symbol = "$", label = "$"),
        CurrencyOption(symbol = "€", label = "€"),
        CurrencyOption(symbol = "£", label = "£"),
        CurrencyOption(symbol = "¥", label = "¥"),
        CurrencyOption(symbol = "₹", label = "₹"),
        CurrencyOption(symbol = "₽", label = "₽"),
        CurrencyOption(symbol = "₺", label = "₺"),
        CurrencyOption(symbol = "₩", label = "₩"),
        CurrencyOption(symbol = "₱", label = "₱"),
        CurrencyOption(symbol = "₫", label = "₫")
    )

    if (showTransactionSheet) {
        TransactionEditorSheet(
            categories = uiState.categories,
            initialTransaction = editingTransaction,
            onDismiss = {
                showTransactionSheet = false
                editingTransaction = null
            },
            onSave = { id, type, amount, categoryId, dateMillis, notes ->
                viewModel.saveTransaction(id, type, amount, categoryId, dateMillis, notes)
                showTransactionSheet = false
                editingTransaction = null
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencyDialog(
            currentSymbol = uiState.currencySymbol,
            options = currencyOptions,
            onDismiss = { showCurrencyDialog = false },
            onSave = { option ->
                viewModel.setCurrencySymbol(option.symbol)
                showCurrencyDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = uiState.monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onSave = { amount ->
                viewModel.setMonthlyBudget(amount)
                showBudgetDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wallet_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.wallet_manage_categories))
                    }
                    IconButton(onClick = { showCurrencyDialog = true }) {
                        Icon(Icons.Default.AttachMoney, contentDescription = stringResource(R.string.wallet_change_currency))
                    }
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = stringResource(R.string.wallet_set_budget))
                    }
                    IconButton(onClick = {
                        editingTransaction = null
                        showTransactionSheet = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.wallet_add_transaction))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OverviewSection(uiState = uiState, currencySymbol = uiState.currencySymbol)
            }
            item {
                BudgetSection(
                    spent = uiState.amountSpentThisMonth,
                    budget = uiState.monthlyBudget,
                    remaining = uiState.remainingBudget,
                    exceeded = uiState.isBudgetExceeded,
                    currencySymbol = uiState.currencySymbol,
                    onSetBudget = { showBudgetDialog = true }
                )
            }
            item {
                AnalyticsSection(uiState = uiState, currencySymbol = uiState.currencySymbol)
            }
            item {
                Text(
                    text = stringResource(R.string.wallet_transactions_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (uiState.transactions.isEmpty()) {
                item {
                    EmptyWalletState(onAddTransaction = {
                        editingTransaction = null
                        showTransactionSheet = true
                    })
                }
            } else {
                items(uiState.transactions, key = { it.id }) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        currencySymbol = uiState.currencySymbol,
                        onEdit = {
                            editingTransaction = transaction
                            showTransactionSheet = true
                        },
                        onDelete = { viewModel.deleteTransaction(transaction.id) }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OverviewSection(uiState: WalletUiState, currencySymbol: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.wallet_overview),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = currency(uiState.currentBalance, currencySymbol),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.wallet_current_balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = stringResource(R.string.wallet_income),
                amount = uiState.totalIncome,
                currencySymbol = currencySymbol,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = stringResource(R.string.wallet_expenses),
                amount = uiState.totalExpense,
                currencySymbol = currencySymbol,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(title: String, amount: Double, currencySymbol: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(currency(amount, currencySymbol), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BudgetSection(
    spent: Double,
    budget: Double,
    remaining: Double,
    exceeded: Boolean,
    currencySymbol: String,
    onSetBudget: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.wallet_budgeting), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onSetBudget) { Text(stringResource(R.string.wallet_set_monthly_budget)) }
            }
            Text(stringResource(R.string.wallet_amount_spent, currency(spent, currencySymbol)), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.wallet_remaining_budget, currency(remaining, currencySymbol)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (budget > 0.0) {
                val progress = (spent / budget).coerceIn(0.0, 1.0)
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = if (exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            if (exceeded) {
                Text(stringResource(R.string.wallet_budget_exceeded), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AnalyticsSection(uiState: WalletUiState, currencySymbol: String) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.wallet_reports_analytics), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (uiState.categoryBreakdown.isEmpty()) {
                Text(stringResource(R.string.wallet_no_expense_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(stringResource(R.string.wallet_expenses_by_category), color = MaterialTheme.colorScheme.onSurfaceVariant)
                PieChart(uiState.categoryBreakdown)
                uiState.categoryBreakdown.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.categoryIcon} ${item.categoryName}", fontWeight = FontWeight.Medium)
                        Text(currency(item.amount, currencySymbol))
                    }
                }
            }
            HorizontalDivider()
            Text(stringResource(R.string.wallet_spending_over_time), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            TrendChart(points = uiState.spendingTrend, currencySymbol = currencySymbol)
        }
    }
}

@Composable
private fun PieChart(items: List<CategoryBreakdown>) {
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF2196F3),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFFFF5722)
    )
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(180.dp)) {
            var startAngle = -90f
            items.forEachIndexed { index, item ->
                val sweep = item.fraction * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 44f, cap = StrokeCap.Butt),
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun TrendChart(points: List<TrendPoint>, currencySymbol: String) {
    if (points.isEmpty()) {
        Text(stringResource(R.string.wallet_no_spending_trend))
        return
    }
    val maxValue = max(points.maxOf { it.amount }, 1.0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        points.forEach { point ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(point.label)
                    Text(currency(point.amount, currencySymbol))
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((point.amount / maxValue).toFloat())
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: WalletTransaction,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${transaction.categoryIcon} ${transaction.categoryName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(formatDate(transaction.dateMillis), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (transaction.notes.isNotBlank()) {
                        Text(transaction.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(transaction.type.labelRes())) }
                    )
                    Text(
                        text = currency(transaction.amount, currencySymbol),
                        color = if (transaction.type == TransactionType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.wallet_edit_transaction))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.wallet_delete_transaction))
                }
            }
        }
    }

}

@Composable
private fun EmptyWalletState(onAddTransaction: () -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp).size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(stringResource(R.string.wallet_no_transactions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.wallet_no_transactions_message), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddTransaction) { Text(stringResource(R.string.wallet_add_transaction)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionEditorSheet(
    categories: List<WalletCategory>,
    initialTransaction: WalletTransaction?,
    onDismiss: () -> Unit,
    onSave: (Long, TransactionType, Double, Long?, Long, String) -> Unit
) {
    var type by remember(initialTransaction) { mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE) }
    var amountText by remember(initialTransaction) { mutableStateOf(initialTransaction?.amount?.toString().orEmpty()) }
    var categoryId by remember(initialTransaction) { mutableStateOf(initialTransaction?.categoryId) }
    var dateText by remember(initialTransaction) { mutableStateOf(formatDate(initialTransaction?.dateMillis ?: System.currentTimeMillis())) }
    var dateMillis by remember(initialTransaction) { mutableStateOf(initialTransaction?.dateMillis ?: System.currentTimeMillis()) }
    var notes by remember(initialTransaction) { mutableStateOf(initialTransaction?.notes.orEmpty()) }
    var expanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (initialTransaction == null) stringResource(R.string.wallet_add_transaction_title) else stringResource(R.string.wallet_edit_transaction_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TransactionType.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = type == option,
                        onClick = { type = option },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TransactionType.entries.size)
                    ) {
                        Text(stringResource(option.labelRes()))
                    }
                }
            }
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(R.string.wallet_amount)) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = categories.firstOrNull { it.id == categoryId }?.let { "${it.icon} ${it.name}" } ?: stringResource(R.string.wallet_uncategorized),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.wallet_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.wallet_uncategorized)) }, onClick = {
                        categoryId = null
                        expanded = false
                    })
                    categories.forEach { category ->
                        DropdownMenuItem(text = { Text("${category.icon} ${category.name}") }, onClick = {
                            categoryId = category.id
                            expanded = false
                        })
                    }
                }
            }
            OutlinedTextField(
                value = dateText,
                onValueChange = {
                    dateText = it
                    parseDate(it)?.let { parsed -> dateMillis = parsed }
                },
                label = { Text(stringResource(R.string.wallet_date_format_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.wallet_notes)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0) {
                            onSave(initialTransaction?.id ?: 0L, type, amount, categoryId, dateMillis, notes)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BudgetDialog(
    currentBudget: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember(currentBudget) { mutableStateOf(if (currentBudget == 0.0) "" else currentBudget.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wallet_monthly_budget)) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.wallet_budget_amount)) }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val parsed = amount.toDoubleOrNull() ?: 0.0
                onSave(parsed)
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun CurrencyDialog(
    currentSymbol: String,
    options: List<CurrencyOption>,
    onDismiss: () -> Unit,
    onSave: (CurrencyOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wallet_currency_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = currentSymbol == option.symbol,
                            onClick = { onSave(option) }
                        )
                        Text(text = option.label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

private fun currency(amount: Double, currencySymbol: String): String {
    val formatter = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val formatted = formatter.format(amount)
    return if (currencySymbol.isBlank()) formatted else "$currencySymbol$formatted"
}

private fun formatDate(millis: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))

private fun parseDate(value: String): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)?.time
}.getOrNull()

private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.INCOME -> R.string.wallet_income
    TransactionType.EXPENSE -> R.string.wallet_expense
}
