package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Bill
import kotlinx.coroutines.flow.Flow

interface BillRepository {
    fun getAll(): Flow<List<Bill>>
    suspend fun getById(id: Long): Bill?
    suspend fun insert(bill: Bill): Long
    suspend fun update(bill: Bill)
    suspend fun delete(bill: Bill)
    fun getCount(): Flow<Int>
    fun getDueSoonCount(): Flow<Int>
    suspend fun markAsPaid(bill: Bill)
}
