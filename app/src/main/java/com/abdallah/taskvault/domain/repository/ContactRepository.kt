package com.abdallah.taskvault.domain.repository

import com.abdallah.taskvault.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getAll(): Flow<List<Contact>>
    fun search(query: String): Flow<List<Contact>>
    suspend fun getById(id: Long): Contact?
    suspend fun getByUserId(userId: String): Contact?
    suspend fun insert(contact: Contact): Long
    suspend fun update(contact: Contact)
    suspend fun delete(contact: Contact)
    fun getCount(): Flow<Int>
}
