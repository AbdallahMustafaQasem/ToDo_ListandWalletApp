package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Password
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {
    fun getAll(): Flow<List<Password>>
    fun search(query: String): Flow<List<Password>>
    suspend fun getById(id: Long): Password?
    suspend fun insert(password: Password): Long
    suspend fun update(password: Password)
    suspend fun delete(password: Password)
    fun getCount(): Flow<Int>
}
