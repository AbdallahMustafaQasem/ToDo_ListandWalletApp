package com.abdallah.taskvault

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.abdallah.taskvault.data.preferences.UserPreferencesRepository
import com.abdallah.taskvault.data.worker.BudgetAlertWorker
import com.abdallah.taskvault.data.worker.TrashPurgeWorker
import com.abdallah.taskvault.notification.DailyDigestWorker
import com.abdallah.taskvault.widget.TodoWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TodoApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        TrashPurgeWorker.schedule(this)
        BudgetAlertWorker.schedule(this)
        TodoWidgetReceiver.ensureScheduled(this)
        CoroutineScope(Dispatchers.IO).launch {
            if (userPreferencesRepository.dailyDigestEnabled.first()) {
                DailyDigestWorker.schedule(this@TodoApplication)
            }
        }
    }
}
