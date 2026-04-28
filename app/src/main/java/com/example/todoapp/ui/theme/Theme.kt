package com.example.todoapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

internal val LightColorScheme = lightColorScheme(
    primary            = Purple40,
    onPrimary          = Color.White,
    primaryContainer   = Purple90,
    onPrimaryContainer = Purple10,
    secondary          = Teal40,
    onSecondary        = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    tertiary           = Pink40,
    onTertiary         = Color.White,
    error              = Color(0xFFB00020),
    onError            = Color.White,
    errorContainer     = Color(0xFFFFDAD6),
    onErrorContainer   = Color(0xFF410002),
    background         = Color(0xFFF7F4FB),
    onBackground       = Color(0xFF1C1B1F),
    surface            = Color(0xFFFFFBFE),
    onSurface          = Color(0xFF1C1B1F),
    surfaceVariant     = Color(0xFFE7E0EC),
    onSurfaceVariant   = Color(0xFF49454F),
    outline            = Color(0xFF79747E),
    outlineVariant     = Color(0xFFD9D1E3)
)

internal val DarkColorScheme = darkColorScheme(
    primary            = Purple80,
    onPrimary          = Purple20,
    primaryContainer   = Purple30,
    onPrimaryContainer = Purple90,
    secondary          = Teal80,
    onSecondary        = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    tertiary           = Pink80,
    onTertiary         = Color(0xFF492532),
    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF690005),
    errorContainer     = Color(0xFF93000A),
    onErrorContainer   = Color(0xFFFFDAD6),
    background         = Color(0xFF141218),
    onBackground       = Color(0xFFE6E1E5),
    surface            = Color(0xFF1C1B1F),
    onSurface          = Color(0xFFE6E1E5),
    surfaceVariant     = Color(0xFF49454F),
    onSurfaceVariant   = Color(0xFFCAC4D0),
    outline            = Color(0xFF938F99),
    outlineVariant     = Color(0xFF5B5562)
)

@Composable
fun TODoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // disabled so our rich palette always shows
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
    ) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(colorScheme.background)
        ) {
            content()
        }
    }
}
