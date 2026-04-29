package com.abdallah.taskvault.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.taskvault.data.preferences.UserPreferencesRepository
import com.abdallah.taskvault.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkTheme: Boolean?   = null,
    val languageCode: String?   = null,
    val currencySymbol: String  = "$",
    val appLockEnabled: Boolean = false,
    val currentUser: FirebaseUser? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val authRepository: AuthRepository
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
                authRepository.currentUser
            ) { dark, lang, currency, lock, user ->
                SettingsUiState(
                    isDarkTheme    = dark,
                    languageCode   = lang,
                    currencySymbol = currency,
                    appLockEnabled = lock,
                    currentUser    = user
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
}
