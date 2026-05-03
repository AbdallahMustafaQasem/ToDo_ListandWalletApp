package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Memoir
import kotlinx.coroutines.flow.Flow

interface MemoirRepository {
    fun getAllMemoirs(): Flow<List<Memoir>>
    fun searchMemoirs(query: String): Flow<List<Memoir>>
    suspend fun getMemoirById(id: Long): Memoir?
    suspend fun insertMemoir(memoir: Memoir): Long
    suspend fun updateMemoir(memoir: Memoir)
    suspend fun deleteMemoir(memoir: Memoir)
    fun getMemoirCount(): Flow<Int>
}
