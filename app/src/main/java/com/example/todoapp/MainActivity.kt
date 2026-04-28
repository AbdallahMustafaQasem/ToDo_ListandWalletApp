package com.example.todoapp

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.data.preferences.UserPreferencesRepository
import com.example.todoapp.ui.navigation.NavGraph
import com.example.todoapp.ui.navigation.Screen
import com.example.todoapp.ui.theme.TODoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_ADD_SCREEN     = "open_add_screen"
        const val EXTRA_OPEN_DETAIL_TODO_ID = "open_detail_todo_id"
    }

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    // Emits a route to navigate to; null = do nothing
    private val pendingNavRoute = MutableStateFlow<String?>(null)
    private var appliedLanguageCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appliedLanguageCode = runBlocking { userPreferencesRepository.languageCode.first() }
        applyLanguage(appliedLanguageCode)

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
