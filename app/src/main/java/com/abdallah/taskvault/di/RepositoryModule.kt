package com.abdallah.taskvault.di

import com.abdallah.taskvault.data.alarm.AlarmSchedulerImpl
import com.abdallah.taskvault.data.repository.FirebaseAuthRepositoryImpl
import com.abdallah.taskvault.data.repository.SubtaskRepositoryImpl
import com.abdallah.taskvault.data.repository.TodoListRepositoryImpl
import com.abdallah.taskvault.data.repository.WalletRepositoryImpl
import com.abdallah.taskvault.domain.alarm.AlarmScheduler
import com.abdallah.taskvault.data.repository.BillRepositoryImpl
import com.abdallah.taskvault.data.repository.ContactRepositoryImpl
import com.abdallah.taskvault.data.repository.HabitRepositoryImpl
import com.abdallah.taskvault.data.repository.MemoirRepositoryImpl
import com.abdallah.taskvault.data.repository.NoteRepositoryImpl
import com.abdallah.taskvault.data.repository.PasswordRepositoryImpl
import com.abdallah.taskvault.data.repository.CommentRepositoryImpl
import com.abdallah.taskvault.data.repository.TagRepositoryImpl
import com.abdallah.taskvault.data.repository.TaskAssignmentRepositoryImpl
import com.abdallah.taskvault.data.repository.TodoRepositoryImpl
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.abdallah.taskvault.domain.repository.CommentRepository
import com.abdallah.taskvault.domain.repository.ContactRepository
import com.abdallah.taskvault.domain.repository.BillRepository
import com.abdallah.taskvault.domain.repository.HabitRepository
import com.abdallah.taskvault.domain.repository.MemoirRepository
import com.abdallah.taskvault.domain.repository.NoteRepository
import com.abdallah.taskvault.domain.repository.PasswordRepository
import com.abdallah.taskvault.domain.repository.SubtaskRepository
import com.abdallah.taskvault.domain.repository.TagRepository
import com.abdallah.taskvault.domain.repository.TodoListRepository
import com.abdallah.taskvault.domain.repository.TaskAssignmentRepository
import com.abdallah.taskvault.domain.repository.TodoRepository
import com.abdallah.taskvault.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindSubtaskRepository(impl: SubtaskRepositoryImpl): SubtaskRepository

    @Binds
    @Singleton
    abstract fun bindTodoListRepository(impl: TodoListRepositoryImpl): TodoListRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindMemoirRepository(impl: MemoirRepositoryImpl): MemoirRepository

    @Binds
    @Singleton
    abstract fun bindPasswordRepository(impl: PasswordRepositoryImpl): PasswordRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindBillRepository(impl: BillRepositoryImpl): BillRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindTaskAssignmentRepository(impl: TaskAssignmentRepositoryImpl): TaskAssignmentRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository
}
