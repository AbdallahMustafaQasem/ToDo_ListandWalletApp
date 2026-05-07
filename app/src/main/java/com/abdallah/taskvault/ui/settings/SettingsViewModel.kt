package com.abdallah.taskvault.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.data.preferences.UserPreferencesRepository
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.abdallah.taskvault.notification.DailyDigestWorker
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkTheme: Boolean?      = null,
    val languageCode: String?      = null,
    val currencySymbol: String     = "$",
    val appLockEnabled: Boolean    = false,
    val dailyDigestEnabled: Boolean = false,
    val currentUser: FirebaseUser? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.isDarkTheme,
                prefs.languageCode,
                prefs.walletCurrencySymbol,
                prefs.appLockEnabled,
                combine(prefs.dailyDigestEnabled, authRepository.currentUser) { digest, user -> digest to user }
            ) { dark, lang, currency, lock, (digest, user) ->
                SettingsUiState(
                    isDarkTheme         = dark,
                    languageCode        = lang,
                    currencySymbol      = currency,
                    appLockEnabled      = lock,
                    dailyDigestEnabled  = digest,
                    currentUser         = user
                )
            }.collect { _uiState.value = it }
        }
    }

    fun setTheme(dark: Boolean?) {
        viewModelScope.launch {
            if (dark == null) prefs.clearDarkTheme() else prefs.setDarkTheme(dark)
        }
    }

    fun setLanguage(code: String?) {
        viewModelScope.launch { prefs.setLanguageCode(code) }
    }

    fun setCurrency(symbol: String) {
        viewModelScope.launch { prefs.setWalletCurrencySymbol(symbol) }
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch { prefs.setAppLockEnabled(enabled) }
    }

    fun setDailyDigest(enabled: Boolean) {
        viewModelScope.launch { prefs.setDailyDigestEnabled(enabled) }
        if (enabled) DailyDigestWorker.schedule(context)
        else DailyDigestWorker.cancel(context)
    }
}
