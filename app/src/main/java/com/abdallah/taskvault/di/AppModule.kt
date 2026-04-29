package com.abdallah.taskvault.di

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.abdallah.taskvault.data.local.SubtaskDao
import com.abdallah.taskvault.data.local.TodoDao
import com.abdallah.taskvault.data.local.TodoDatabase
import com.abdallah.taskvault.data.local.TodoListDao
import com.abdallah.taskvault.data.local.WalletDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase =
        Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            TodoDatabase.DATABASE_NAME
        )
        .addMigrations(TodoDatabase.MIGRATION_1_2)
        .addMigrations(TodoDatabase.MIGRATION_2_3)
        .addMigrations(TodoDatabase.MIGRATION_3_4)
        .build()

    @Provides
    @Singleton
    fun provideTodoDao(database: TodoDatabase): TodoDao = database.todoDao()

    @Provides
    @Singleton
    fun provideWalletDao(database: TodoDatabase): WalletDao = database.walletDao()

    @Provides
    @Singleton
    fun provideSubtaskDao(database: TodoDatabase): SubtaskDao = database.subtaskDao()

    @Provides
    @Singleton
    fun provideTodoListDao(database: TodoDatabase): TodoListDao = database.todoListDao()

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Todo Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for todo reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }

    const val NOTIFICATION_CHANNEL_ID = "todo_reminders"
}
