package com.abdallah.taskvault.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Bill
import com.abdallah.taskvault.domain.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillListUiState(
    val bills: List<Bill> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BillListViewModel @Inject constructor(
    private val repository: BillRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    val uiState: StateFlow<BillListUiState> = repository.getAll()
        .map { BillListUiState(bills = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BillListUiState())

    fun markAsPaid(bill: Bill) {
        viewModelScope.launch {
            repository.markAsPaid(bill)
            analytics.logBillPaid()
        }
    }

    fun delete(bill: Bill) {
        viewModelScope.launch {
            repository.delete(bill)
            analytics.logBillDeleted()
        }
    }

    fun isOverdue(bill: Bill): Boolean =
        !bill.isPaid && bill.nextDueDateMillis < System.currentTimeMillis()

    fun isDueSoon(bill: Bill): Boolean {
        val sevenDays = 7L * 24 * 60 * 60 * 1000
        return !bill.isPaid && bill.nextDueDateMillis in System.currentTimeMillis()..System.currentTimeMillis() + sevenDays
    }
}
