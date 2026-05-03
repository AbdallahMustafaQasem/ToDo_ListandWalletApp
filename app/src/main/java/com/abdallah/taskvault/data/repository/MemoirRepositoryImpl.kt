package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.MemoirDao
import com.abdallah.taskvault.data.local.MemoirEntity
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.model.Memoir
import com.abdallah.taskvault.domain.repository.MemoirRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MemoirRepositoryImpl @Inject constructor(
    private val dao: MemoirDao,
    private val sync: FirebaseSyncRepository
) : MemoirRepository {

    override fun getAllMemoirs(): Flow<List<Memoir>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun searchMemoirs(query: String): Flow<List<Memoir>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getMemoirById(id: Long): Memoir? =
        dao.getById(id)?.toDomain()

    override suspend fun insertMemoir(memoir: Memoir): Long {
        val id = dao.insert(memoir.toEntity())
        sync.syncMemoir(memoir.copy(id = id))
        return id
    }

    override suspend fun updateMemoir(memoir: Memoir) {
        dao.update(memoir.toEntity())
        sync.syncMemoir(memoir)
    }

    override suspend fun deleteMemoir(memoir: Memoir) {
        dao.delete(memoir.toEntity())
        sync.deleteMemoirSync(memoir.id)
    }

    override fun getMemoirCount(): Flow<Int> = dao.count()

    private fun MemoirEntity.toDomain() =
        Memoir(id, title, content, mood, dateMillis, createdAt)

    private fun Memoir.toEntity() =
        MemoirEntity(id, title, content, mood, dateMillis, createdAt)
}
