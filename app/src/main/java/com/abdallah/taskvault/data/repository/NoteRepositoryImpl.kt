package com.abdallah.taskvault.data.repository

import com.abdallah.taskvault.data.local.NoteDao
import com.abdallah.taskvault.data.local.NoteEntity
import com.abdallah.taskvault.data.sync.FirebaseSyncRepository
import com.abdallah.taskvault.domain.model.Note
import com.abdallah.taskvault.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val sync: FirebaseSyncRepository
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun getNoteById(id: Long): Note? =
        dao.getById(id)?.toDomain()

    override suspend fun insertNote(note: Note): Long {
        val id = dao.insert(note.toEntity())
        sync.syncNote(note.copy(id = id))
        return id
    }

    override suspend fun updateNote(note: Note) {
        dao.update(note.toEntity())
        sync.syncNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        dao.delete(note.toEntity())
        sync.deleteNoteSync(note.id)
    }

    override fun getNoteCount(): Flow<Int> = dao.count()

    private fun NoteEntity.toDomain() =
        Note(id, title, content, colorHex, isPinned, createdAt, updatedAt)

    private fun Note.toEntity() =
        NoteEntity(id, title, content, colorHex, isPinned, createdAt, updatedAt)
}
