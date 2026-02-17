package com.nexosolar.android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Tema principal de NexoSolar
 *
 * Soporta:
 * - Light/Dark mode automático
 * - Dynamic colors en Android 12+ (opcional)
 */
@Composable
fun NexoSolarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Activa colores dinámicos del sistema (Android 12+)
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic colors (Material You) - solo Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Tema oscuro custom
        darkTheme -> NexoDarkColorScheme
        // Tema claro custom (por defecto)
        else -> NexoLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
