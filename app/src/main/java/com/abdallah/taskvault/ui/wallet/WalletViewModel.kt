package com.abdallah.taskvault.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.data.preferences.UserPreferencesRepository
import com.abdallah.taskvault.domain.model.TransactionType
import com.abdallah.taskvault.domain.model.WalletCategory
import com.abdallah.taskvault.domain.model.WalletTransaction
import com.abdallah.taskvault.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

data class WalletUiState(
    val isLoading: Boolean = true,
    val transactions: List<WalletTransaction> = emptyList(),
    val categories: List<WalletCategory> = emptyList(),
    val currencySymbol: String = "$",
    val monthlyBudget: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val currentBalance: Double = 0.0,
    val amountSpentThisMonth: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val isBudgetExceeded: Boolean = false,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val spendingTrend: List<TrendPoint> = emptyList()
)

data class CategoryBreakdown(
    val categoryName: String,
    val categoryIcon: String,
    val amount: Double,
    val fraction: Float
)

data class TrendPoint(
    val label: String,
    val amount: Double
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val filters = MutableStateFlow(WalletUiState())
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            walletRepository.seedDefaultCategories()
        }
        viewModelScope.launch {
            combine(
                walletRepository.getTransactions(),
                walletRepository.getCategories(),
                walletRepository.getBudget(),
                userPreferencesRepository.walletCurrencySymbol,
                filters
            ) { transactions, categories, budget, currencySymbol, filterState ->
                buildUiState(
                    baseState = filterState,
                    transactions = transactions,
                    categories = categories,
                    monthlyBudget = budget?.monthlyBudget ?: 0.0,
                    currencySymbol = currencySymbol
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun saveTransaction(
        id: Long,
        type: TransactionType,
        amount: Double,
        categoryId: Long?,
        dateMillis: Long,
        notes: String
    ) {
        viewModelScope.launch {
            walletRepository.upsertTransaction(
                WalletTransaction(
                    id = id,
                    type = type,
                    amount = amount,
                    categoryId = categoryId,
                    categoryName = "",
                    categoryIcon = "",
                    dateMillis = dateMillis,
                    notes = notes
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            walletRepository.deleteTransaction(id)
        }
    }

    fun saveCategory(id: Long, name: String, icon: String, isDefault: Boolean) {
        viewModelScope.launch {
            walletRepository.upsertCategory(
                WalletCategory(
                    id = id,
                    name = name,
                    icon = icon,
                    isDefault = isDefault
                )
            )
        }
    }

    fun deleteCategory(category: WalletCategory) {
        if (category.isDefault) return
        viewModelScope.launch {
            walletRepository.deleteCategory(category.id)
        }
    }

    fun setMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            walletRepository.setMonthlyBudget(amount)
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            userPreferencesRepository.setWalletCurrencySymbol(symbol)
        }
    }

    private fun buildUiState(
        baseState: WalletUiState,
        transactions: List<WalletTransaction>,
        categories: List<WalletCategory>,
        monthlyBudget: Double,
        currencySymbol: String
    ): WalletUiState {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val currentBalance = totalIncome - totalExpense
        val spentThisMonth = transactions
            .filter { it.type == TransactionType.EXPENSE && matchesMonth(it.dateMillis) }
            .sumOf { it.amount }
        val remainingBudget = monthlyBudget - spentThisMonth
        val categoryExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryName to it.categoryIcon }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        val totalCategoryExpense = categoryExpenses.values.sum().takeIf { it > 0.0 } ?: 1.0
        val categoryBreakdown = categoryExpenses.entries
            .sortedByDescending { it.value }
            .map { entry ->
                CategoryBreakdown(
                    categoryName = entry.key.first,
                    categoryIcon = entry.key.second,
                    amount = entry.value,
                    fraction = (entry.value / totalCategoryExpense).toFloat()
                )
            }
        val spendingTrend = buildTrend(transactions)

        return baseState.copy(
            isLoading = false,
            transactions = transactions.sortedByDescending { it.dateMillis },
            categories = categories,
            currencySymbol = currencySymbol,
            monthlyBudget = monthlyBudget,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            currentBalance = currentBalance,
            amountSpentThisMonth = spentThisMonth,
            remainingBudget = remainingBudget,
            isBudgetExceeded = monthlyBudget > 0.0 && remainingBudget < 0.0,
            categoryBreakdown = categoryBreakdown,
            spendingTrend = spendingTrend
        )
    }

    private fun matchesMonth(dateMillis: Long): Boolean {
        val selected = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val current = Calendar.getInstance()
        return selected.get(Calendar.YEAR) == current.get(Calendar.YEAR) &&
            selected.get(Calendar.MONTH) == current.get(Calendar.MONTH)
    }

    private fun buildTrend(transactions: List<WalletTransaction>): List<TrendPoint> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
        return expenses
            .groupBy { transaction ->
                val calendar = Calendar.getInstance().apply { timeInMillis = transaction.dateMillis }
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            .toSortedMap()
            .map { entry ->
                TrendPoint(
                    label = formatter.format(entry.key),
                    amount = entry.value.sumOf { it.amount }.absoluteValue
                )
            }
            .takeLast(6)
    }
}
