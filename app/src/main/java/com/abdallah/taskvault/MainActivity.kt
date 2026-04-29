package com.abdallah.taskvault

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.abdallah.taskvault.R
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.abdallah.taskvault.data.preferences.UserPreferencesRepository
import com.abdallah.taskvault.ui.AppViewModel
import com.abdallah.taskvault.ui.SyncState
import com.abdallah.taskvault.ui.SyncStep
import com.abdallah.taskvault.ui.applock.AppLockScreen
import com.abdallah.taskvault.ui.auth.LoginScreen
import com.abdallah.taskvault.ui.auth.SyncScreen
import com.abdallah.taskvault.ui.navigation.NavGraph
import com.abdallah.taskvault.ui.navigation.Screen
import com.abdallah.taskvault.ui.theme.TODoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    companion object {
        const val EXTRA_OPEN_ADD_SCREEN     = "open_add_screen"
        const val EXTRA_OPEN_DETAIL_TODO_ID = "open_detail_todo_id"
    }

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val appViewModel: AppViewModel by viewModels()

    // Emits a route to navigate to; null = do nothing
    private val pendingNavRoute = MutableStateFlow<String?>(null)
    private var appliedLanguageCode: String? = null
    private var isAppLocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appliedLanguageCode = runBlocking { userPreferencesRepository.languageCode.first() }
        applyLanguage(appliedLanguageCode)

        val appLockEnabled = runBlocking { userPreferencesRepository.appLockEnabled.first() }
        isAppLocked = appLockEnabled

        // On a fresh launch, pick the start destination from the intent
        val startDestination = resolveRoute(intent) ?: Screen.Home.route

        val darkThemeFlow = userPreferencesRepository.isDarkTheme
            .stateIn(lifecycleScope, SharingStarted.Eagerly, null)
        val languageCodeFlow = userPreferencesRepository.languageCode
            .stateIn(lifecycleScope, SharingStarted.Eagerly, appliedLanguageCode)

        setContent {
            val isDarkPref by darkThemeFlow.collectAsState()
            val languageCode by languageCodeFlow.collectAsState()
            val darkTheme = isDarkPref ?: isSystemInDarkTheme()

            LaunchedEffect(languageCode) {
                if (languageCode != appliedLanguageCode) {
                    appliedLanguageCode = languageCode
                    applyLanguage(languageCode)
                    recreate()
                }
            }

            TODoAppTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val currentUser by appViewModel.currentUser.collectAsState()
                    val syncState  by appViewModel.syncState.collectAsState()
                    val syncStep   by appViewModel.syncStep.collectAsState()
                    val syncMsg = when (syncStep) {
                        SyncStep.RESTORE -> stringResource(R.string.sync_restore)
                        SyncStep.UPLOAD  -> stringResource(R.string.sync_upload)
                        else             -> stringResource(R.string.sync_message)
                    }

                    LaunchedEffect(currentUser?.uid) {
                        if (currentUser != null && syncState == SyncState.IDLE) {
                            appViewModel.onSignedIn()
                        }
                    }

                    if (currentUser == null) {
                        LoginScreen()
                    } else if (syncState == SyncState.SYNCING) {
                        SyncScreen(message = syncMsg)
                    } else if (syncState == SyncState.ERROR) {
                        SyncScreen(
                            message  = stringResource(R.string.sync_error_message),
                            isError  = true,
                            onRetry  = { appViewModel.onSignedIn() }
                        )
                    } else if (isAppLocked) {
                        AppLockScreen(
                            onUnlock = { showBiometricPrompt() }
                        )
                    } else {
                        val navController = rememberNavController()

                        // React to widget / notification intents arriving while app is alive
                        val pending by pendingNavRoute.collectAsState()
                        LaunchedEffect(pending) {
                            pending?.let { route ->
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                                pendingNavRoute.value = null
                            }
                        }

                        NavGraph(
                            navController    = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }

        if (appLockEnabled) {
            showBiometricPrompt()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val enabled = userPreferencesRepository.appLockEnabled.first()
            if (enabled && !isAppLocked) {
                isAppLocked = true
                showBiometricPrompt()
            }
        }
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            isAppLocked = false
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isAppLocked = false
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    finish()
                }
            }
            override fun onAuthenticationFailed() { }
        })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_lock_prompt_title))
            .setSubtitle(getString(R.string.app_lock_prompt_subtitle))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(promptInfo)
    }

    private fun applyLanguage(languageCode: String?) {
        val locale = languageCode
            ?.takeIf { it.isNotBlank() }
            ?.let { Locale.forLanguageTag(it) }
            ?: Resources.getSystem().configuration.locales[0]

        Locale.setDefault(locale)
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveRoute(intent)?.let { pendingNavRoute.value = it }
    }

    private fun resolveRoute(intent: Intent?): String? = when {
        intent?.getBooleanExtra(EXTRA_OPEN_ADD_SCREEN, false) == true ->
            Screen.Add.route
        intent?.hasExtra(EXTRA_OPEN_DETAIL_TODO_ID) == true -> {
            val id = intent.getLongExtra(EXTRA_OPEN_DETAIL_TODO_ID, -1L)
            if (id != -1L) Screen.Detail.createRoute(id) else null
        }
        else -> null
    }
}
