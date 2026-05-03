package com.abdallah.taskvault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY display_name ASC")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE display_name LIKE '%' || :query || '%' OR user_id LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ContactEntity>>

    @Query("SELECT COUNT(*) FROM contacts")
    fun getCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ContactEntity): Long

    @Update
    suspend fun update(entity: ContactEntity)

    @Delete
    suspend fun delete(entity: ContactEntity)
}
