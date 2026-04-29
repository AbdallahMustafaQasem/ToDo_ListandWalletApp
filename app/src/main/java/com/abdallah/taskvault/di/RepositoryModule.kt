package com.abdallah.taskvault.di

import com.abdallah.taskvault.data.alarm.AlarmSchedulerImpl
import com.abdallah.taskvault.data.repository.FirebaseAuthRepositoryImpl
import com.abdallah.taskvault.data.repository.SubtaskRepositoryImpl
import com.abdallah.taskvault.data.repository.TodoListRepositoryImpl
import com.abdallah.taskvault.data.repository.WalletRepositoryImpl
import com.abdallah.taskvault.domain.alarm.AlarmScheduler
import com.abdallah.taskvault.data.repository.TodoRepositoryImpl
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.abdallah.taskvault.domain.repository.SubtaskRepository
import com.abdallah.taskvault.domain.repository.TodoListRepository
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
}
