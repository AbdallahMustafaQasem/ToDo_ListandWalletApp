package com.abdallah.taskvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY next_due_date_millis ASC")
    fun getAll(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity): Long

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT COUNT(*) FROM bills")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM bills WHERE is_paid = 0 AND next_due_date_millis <= :sevenDaysFromNow")
    fun getDueSoonCount(sevenDaysFromNow: Long): Flow<Int>
}
