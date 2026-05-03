package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.PasswordDao
import com.abdallah.taskvault.data.local.PasswordEntity
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.model.Password
import com.abdallah.taskvault.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PasswordRepositoryImpl @Inject constructor(
    private val dao: PasswordDao,
    private val sync: FirebaseSyncRepository
) : PasswordRepository {

    override fun getAll(): Flow<List<Password>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Password>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Password? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(password: Password): Long {
        val id = dao.insert(password.toEntity())
        val saved = password.copy(id = id)
        sync.syncPassword(saved)
        return id
    }

    override suspend fun update(password: Password) {
        dao.update(password.toEntity())
        sync.syncPassword(password)
    }

    override suspend fun delete(password: Password) {
        dao.delete(password.toEntity())
        sync.deletePasswordSync(password.id)
    }

    override fun getCount(): Flow<Int> = dao.count()

    private fun PasswordEntity.toDomain() =
        Password(id, title, username, password, url, notes, createdAt, updatedAt)

    private fun Password.toEntity() =
        PasswordEntity(id, title, username, password, url, notes, createdAt, updatedAt)
}
