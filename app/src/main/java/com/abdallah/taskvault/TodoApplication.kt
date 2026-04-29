package com.abdallah.taskvault

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.abdallah.taskvault.data.worker.BudgetAlertWorker
import com.abdallah.taskvault.data.worker.TrashPurgeWorker
import com.abdallah.taskvault.widget.TodoWidgetReceiver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TodoApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        TrashPurgeWorker.schedule(this)
        BudgetAlertWorker.schedule(this)
        TodoWidgetReceiver.ensureScheduled(this)
    }
}
