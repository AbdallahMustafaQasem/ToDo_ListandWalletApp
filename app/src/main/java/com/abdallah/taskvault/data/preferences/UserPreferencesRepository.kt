package com.abdallah.taskvault.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

enum class SortOrder {
    CREATION_DATE,
    DUE_DATE,
    PRIORITY,
    ALPHABETICAL
}

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_THEME  = booleanPreferencesKey("dark_theme")
        val SORT_ORDER  = stringPreferencesKey("sort_order")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val WALLET_CURRENCY_SYMBOL = stringPreferencesKey("wallet_currency_symbol")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val DAILY_DIGEST_ENABLED = booleanPreferencesKey("daily_digest_enabled")
    }

    val isDarkTheme: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME]
    }

    val sortOrder: Flow<SortOrder> = context.dataStore.data.map { prefs ->
        prefs[Keys.SORT_ORDER]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
            ?: SortOrder.CREATION_DATE
    }

    val languageCode: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE_CODE]
    }

    val walletCurrencySymbol: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.WALLET_CURRENCY_SYMBOL] ?: "$"
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = dark }
    }

    /** Remove the dark-theme key so the app falls back to the system default. */
    suspend fun clearDarkTheme() {
        context.dataStore.edit { it.remove(Keys.DARK_THEME) }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun setLanguageCode(languageCode: String?) {
        context.dataStore.edit {
            if (languageCode.isNullOrBlank()) {
                it.remove(Keys.LANGUAGE_CODE)
            } else {
                it[Keys.LANGUAGE_CODE] = languageCode
            }
        }
    }

    suspend fun setWalletCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[Keys.WALLET_CURRENCY_SYMBOL] = symbol }
    }

    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.APP_LOCK_ENABLED] ?: false
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.APP_LOCK_ENABLED] = enabled }
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    val dailyDigestEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_DIGEST_ENABLED] ?: false
    }

    suspend fun setDailyDigestEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_DIGEST_ENABLED] = enabled }
    }
}
