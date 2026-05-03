package com.abdallah.taskvault.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    private val analytics: FirebaseAnalytics
) {

    fun logScreenView(screenName: String, screenClass: String = "NavGraph") {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

    fun logEvent(name: String, params: Bundle? = null) {
        analytics.logEvent(name, params)
    }

    fun logNoteCreated()   = logEvent("note_created")
    fun logNoteUpdated()   = logEvent("note_updated")
    fun logNoteDeleted()   = logEvent("note_deleted")

    fun logMemoirCreated() = logEvent("memoir_created")
    fun logMemoirUpdated() = logEvent("memoir_updated")
    fun logMemoirDeleted() = logEvent("memoir_deleted")

    fun logPasswordCreated() = logEvent("password_created")
    fun logPasswordUpdated() = logEvent("password_updated")
    fun logPasswordDeleted() = logEvent("password_deleted")

    fun logHabitCreated()   = logEvent("habit_created")
    fun logHabitUpdated()   = logEvent("habit_updated")
    fun logHabitDeleted()   = logEvent("habit_deleted")
    fun logHabitCompleted() = logEvent("habit_completed")

    fun logBillCreated()    = logEvent("bill_created")
    fun logBillUpdated()    = logEvent("bill_updated")
    fun logBillDeleted()    = logEvent("bill_deleted")
    fun logBillPaid()       = logEvent("bill_paid")

    fun logUserSignedIn()   = logEvent("user_signed_in")
    fun logUserSignedOut()  = logEvent("user_signed_out")
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AnalyticsEntryPoint {
    fun analyticsHelper(): AnalyticsHelper
}
