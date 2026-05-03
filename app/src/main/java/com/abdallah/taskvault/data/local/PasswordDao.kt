package com.abdallah.taskvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM passwords ORDER BY updated_at_millis DESC")
    fun getAll(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getById(id: Long): PasswordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(password: PasswordEntity): Long

    @Update
    suspend fun update(password: PasswordEntity)

    @Delete
    suspend fun delete(password: PasswordEntity)

    @Query("SELECT COUNT(*) FROM passwords")
    fun count(): Flow<Int>

    @Query(
        """SELECT * FROM passwords
           WHERE title LIKE '%' || :query || '%'
              OR username LIKE '%' || :query || '%'
              OR url LIKE '%' || :query || '%'
           ORDER BY updated_at_millis DESC"""
    )
    fun search(query: String): Flow<List<PasswordEntity>>
}
