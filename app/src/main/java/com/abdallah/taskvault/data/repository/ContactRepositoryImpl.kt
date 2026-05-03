package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.ContactDao
import com.abdallah.taskvault.data.local.ContactEntity
import com.abdallah.taskvault.data.local.toEntity
import com.abdallah.taskvault.domain.model.Contact
import com.abdallah.taskvault.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao
) : ContactRepository {

    override fun getAll(): Flow<List<Contact>> =
        contactDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Contact>> =
        contactDao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Contact? =
        contactDao.getById(id)?.toDomain()

    override suspend fun getByUserId(userId: String): Contact? =
        contactDao.getByUserId(userId)?.toDomain()

    override suspend fun insert(contact: Contact): Long =
        contactDao.insert(contact.toEntity())

    override suspend fun update(contact: Contact) =
        contactDao.update(contact.toEntity())

    override suspend fun delete(contact: Contact) =
        contactDao.delete(contact.toEntity())

    override fun getCount(): Flow<Int> =
        contactDao.getCount()
}
