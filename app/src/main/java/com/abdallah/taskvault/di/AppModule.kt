package com.abdallah.taskvault.di

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.abdallah.taskvault.data.local.MemoirDao
import com.abdallah.taskvault.data.local.NoteDao
import com.abdallah.taskvault.data.local.BillDao
import com.abdallah.taskvault.data.local.ContactDao
import com.abdallah.taskvault.data.local.HabitDao
import com.abdallah.taskvault.data.local.PasswordDao
import com.abdallah.taskvault.data.local.SubtaskDao
import com.abdallah.taskvault.data.local.TodoDao
import com.abdallah.taskvault.data.local.TodoDatabase
import com.abdallah.taskvault.data.local.TodoListDao
import com.abdallah.taskvault.data.local.WalletDao
import com.google.firebase.analytics.FirebaseAnalytics
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
        .addMigrations(TodoDatabase.MIGRATION_4_5)
        .addMigrations(TodoDatabase.MIGRATION_5_6)
        .addMigrations(TodoDatabase.MIGRATION_6_7)
        .addMigrations(TodoDatabase.MIGRATION_7_8)
        .addMigrations(TodoDatabase.MIGRATION_8_9)
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
    fun provideNoteDao(database: TodoDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideMemoirDao(database: TodoDatabase): MemoirDao = database.memoirDao()

    @Provides
    @Singleton
    fun providePasswordDao(database: TodoDatabase): PasswordDao = database.passwordDao()

    @Provides
    @Singleton
    fun provideHabitDao(database: TodoDatabase): HabitDao = database.habitDao()

    @Provides
    @Singleton
    fun provideBillDao(database: TodoDatabase): BillDao = database.billDao()

    @Provides
    @Singleton
    fun provideContactDao(database: TodoDatabase): ContactDao = database.contactDao()

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

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
            val remindersChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Todo Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for todo reminders"
                enableVibration(true)
            }
            val assignmentsChannel = NotificationChannel(
                ASSIGNMENTS_CHANNEL_ID,
                "Task Assignments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task assignments and status updates"
                enableVibration(true)
            }
            notificationManager.createNotificationChannels(listOf(remindersChannel, assignmentsChannel))
        }
        return notificationManager
    }

    const val NOTIFICATION_CHANNEL_ID  = "todo_reminders"
    const val ASSIGNMENTS_CHANNEL_ID   = "task_assignments"
}
