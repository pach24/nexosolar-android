package com.nexosolar.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definimos el esquema CLARO (Light Theme)
private val LightColorScheme = lightColorScheme(
    primary = NexoGreenLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = NexoGreenDark,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,

    secondary = ActiveThumb,
    onSecondary = androidx.compose.ui.graphics.Color.Black,

    background = MainBackground,
    surface = androidx.compose.ui.graphics.Color.White,

    error = TextAlert
)


private val DarkColorScheme = darkColorScheme(
    primary = NexoGreenLight,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    background = DarkerGray
)

@Composable
fun NexoSolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // SOLUCIÓN: Buscar la Activity de forma recursiva (segura para Hilt)
            val window = (view.context.findActivity()).window

            window.statusBarColor = androidx.compose.ui.graphics.Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Función helper para desenrollar el ContextWrapper de Hilt
private fun android.content.Context.findActivity(): Activity {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("No activity found")
}
