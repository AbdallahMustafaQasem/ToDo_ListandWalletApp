package com.abdallah.taskvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoirDao {

    @Query("SELECT * FROM memoirs ORDER BY date_millis DESC")
    fun getAll(): Flow<List<MemoirEntity>>

    @Query("SELECT * FROM memoirs WHERE id = :id")
    suspend fun getById(id: Long): MemoirEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memoir: MemoirEntity): Long

    @Update
    suspend fun update(memoir: MemoirEntity)

    @Delete
    suspend fun delete(memoir: MemoirEntity)

    @Query("SELECT COUNT(*) FROM memoirs")
    fun count(): Flow<Int>

    @Query("SELECT * FROM memoirs WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY date_millis DESC")
    fun search(query: String): Flow<List<MemoirEntity>>
}
