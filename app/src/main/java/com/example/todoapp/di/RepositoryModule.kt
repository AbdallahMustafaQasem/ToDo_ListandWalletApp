package com.example.todoapp.di

import com.example.todoapp.data.alarm.AlarmSchedulerImpl
import com.example.todoapp.data.repository.WalletRepositoryImpl
import com.example.todoapp.domain.alarm.AlarmScheduler
import com.example.todoapp.data.repository.TodoRepositoryImpl
import com.example.todoapp.domain.repository.TodoRepository
import com.example.todoapp.domain.repository.WalletRepository
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
}
