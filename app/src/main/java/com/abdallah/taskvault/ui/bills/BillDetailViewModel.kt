package com.abdallah.taskvault.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.analytics.AnalyticsHelper
import com.abdallah.taskvault.domain.model.Bill
import com.abdallah.taskvault.domain.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BillDetailUiState(
    val id: Long = -1L,
    val name: String = "",
    val amount: String = "",
    val dueDay: Int = 1,
    val category: String = "Other",
    val notes: String = "",
    val reminderEnabled: Boolean = false,
    val reminderDaysBefore: Int = 1,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false
)

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BillRepository,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val billId: Long = savedStateHandle.get<Long>("billId") ?: -1L
    private val _uiState = MutableStateFlow(BillDetailUiState())
    val uiState: StateFlow<BillDetailUiState> = _uiState.asStateFlow()

    init {
        if (billId != -1L) {
            viewModelScope.launch {
                repository.getById(billId)?.let { b ->
                    _uiState.value = BillDetailUiState(
                        id = b.id, name = b.name, amount = b.amount.toString(),
                        dueDay = b.dueDay, category = b.category, notes = b.notes,
                        reminderEnabled = b.reminderEnabled, reminderDaysBefore = b.reminderDaysBefore,
                        isLoading = false
                    )
                } ?: run { _uiState.value = _uiState.value.copy(isLoading = false) }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun onNameChange(v: String)       { _uiState.value = _uiState.value.copy(name = v) }
    fun onAmountChange(v: String)     { _uiState.value = _uiState.value.copy(amount = v) }
    fun onDueDayChange(v: Int)        { _uiState.value = _uiState.value.copy(dueDay = v.coerceIn(1, 31)) }
    fun onCategoryChange(v: String)   { _uiState.value = _uiState.value.copy(category = v) }
    fun onNotesChange(v: String)      { _uiState.value = _uiState.value.copy(notes = v) }
    fun onReminderToggle()            { _uiState.value = _uiState.value.copy(reminderEnabled = !_uiState.value.reminderEnabled) }
    fun onReminderDaysChange(v: Int)  { _uiState.value = _uiState.value.copy(reminderDaysBefore = v.coerceIn(1, 14)) }

    fun save() {
        val s = _uiState.value
        if (s.name.isBlank()) return
        val amount = s.amount.toDoubleOrNull() ?: return
        viewModelScope.launch {
            val nextDue = calcNextDue(s.dueDay)
            if (billId == -1L) {
                repository.insert(Bill(name = s.name, amount = amount, dueDay = s.dueDay,
                    category = s.category, notes = s.notes, reminderEnabled = s.reminderEnabled,
                    reminderDaysBefore = s.reminderDaysBefore, nextDueDateMillis = nextDue))
                analytics.logBillCreated()
            } else {
                val existing = repository.getById(billId) ?: return@launch
                repository.update(existing.copy(name = s.name, amount = amount, dueDay = s.dueDay,
                    category = s.category, notes = s.notes, reminderEnabled = s.reminderEnabled,
                    reminderDaysBefore = s.reminderDaysBefore, nextDueDateMillis = calcNextDue(s.dueDay)))
                analytics.logBillUpdated()
            }
            _uiState.value = s.copy(isSaved = true)
        }
    }

    private fun calcNextDue(dueDay: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(dueDay, maxDay))
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        if (target.before(now)) target.add(Calendar.MONTH, 1)
        return target.timeInMillis
    }
}
