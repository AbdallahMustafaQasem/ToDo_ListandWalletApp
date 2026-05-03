package com.abdallah.taskvault.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.abdallah.taskvault.data.local.BillDao
import com.abdallah.taskvault.data.local.toEntity
import com.abdallah.taskvault.domain.model.Bill
import com.abdallah.taskvault.domain.repository.BillRepository
import com.abdallah.taskvault.receiver.BillReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepositoryImpl @Inject constructor(
    private val dao: BillDao,
    private val alarmManager: AlarmManager,
    @ApplicationContext private val context: Context
) : BillRepository {

    companion object {
        const val ALARM_ID_OFFSET = 600_000
        private const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000
    }

    override fun getAll(): Flow<List<Bill>> = dao.getAll().map { it.map { e -> e.toDomain() } }
    override suspend fun getById(id: Long): Bill? = dao.getById(id)?.toDomain()
    override fun getCount(): Flow<Int> = dao.count()
    override fun getDueSoonCount(): Flow<Int> =
        dao.getDueSoonCount(System.currentTimeMillis() + SEVEN_DAYS_MS)

    override suspend fun insert(bill: Bill): Long {
        val id = dao.insert(bill.toEntity())
        if (bill.reminderEnabled) scheduleAlarm(bill.copy(id = id))
        return id
    }

    override suspend fun update(bill: Bill) {
        cancelAlarm(bill)
        dao.update(bill.toEntity())
        if (bill.reminderEnabled && !bill.isPaid) scheduleAlarm(bill)
    }

    override suspend fun delete(bill: Bill) {
        cancelAlarm(bill)
        dao.delete(bill.toEntity())
    }

    override suspend fun markAsPaid(bill: Bill) {
        cancelAlarm(bill)
        val nextDue = calculateNextDueMillis(bill.dueDay, bill.nextDueDateMillis)
        val updated = bill.copy(isPaid = true, nextDueDateMillis = nextDue)
        dao.update(updated.toEntity())
        if (bill.reminderEnabled) scheduleAlarm(updated)
    }

    private fun scheduleAlarm(bill: Bill) {
        val triggerAt = bill.nextDueDateMillis - bill.reminderDaysBefore * 24L * 60 * 60 * 1000
        if (triggerAt <= System.currentTimeMillis()) return
        val intent = Intent(context, BillReminderReceiver::class.java).apply {
            putExtra(BillReminderReceiver.EXTRA_BILL_ID, bill.id)
            putExtra(BillReminderReceiver.EXTRA_BILL_NAME, bill.name)
            putExtra(BillReminderReceiver.EXTRA_BILL_AMOUNT, bill.amount)
        }
        val pi = PendingIntent.getBroadcast(
            context, (bill.id + ALARM_ID_OFFSET).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun cancelAlarm(bill: Bill) {
        val intent = Intent(context, BillReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, (bill.id + ALARM_ID_OFFSET).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun calculateNextDueMillis(dueDay: Int, currentDueDateMillis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDueDateMillis }
        cal.add(Calendar.MONTH, 1)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, minOf(dueDay, maxDay))
        return cal.timeInMillis
    }
}
