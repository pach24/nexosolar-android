package com.nexosolar.android.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ================= PALETA DE MARCA =================
val NexoGreenLight = Color(0xFF95BB4E)
val NexoGreenDark = Color(0xFF669900)
val SmartSolarGreen = Color(0xFF7CB342)

// ================= MAIN HEADER (Gradiente) =================
val HeaderBack = Color(0xFF7DA935)
val HeaderMid = Color(0xFF99BB62)
val HeaderFront = Color(0xFFBAD296)

// ================= ESTADOS =================
val TextAlert = Color(0xFFF46565)
val TextNormal = Color(0xFF000000)

// ================= UI GENÉRICO =================
val InactiveThumb = Color(0xFFBDBDBD)
val ActiveThumb = Color(0xFF03DAC5)
val CardBorder = Color(0xFFF0F0F0)
val MainBackground = Color(0xFFF2F2F7)
val myGray = Color(0xFFE9E7EC)
val LightGray = Color(0xFFE8E8E8)
val DarkGray = Color(0xFF78787A)
val DarkerGray = Color(0xFF333333)

// ================= SKELETONS / DIVIDERS =================
val SkeletonDivider = Color(0xFFEEEEEE)
val InvoiceDivider = Color(0xFFE0E0E0)

// ================= COLORES ADICIONALES =================
// Basados en los R.color.*
val DetailLabelColor = Color(0xFF78787A)        // detail_label_color
val DetailValueColor = Color(0xFF333333)        // detail_value_color
val DetailInfoTint = Color(0xFF7CB342)          // detail_info_tint (verde acento)
val DetailDividerColor = Color(0xFFE0E0E0)      // detail_divider_color
val EmptyEnergyTextColor = Color(0xFF78787A)    // empty_energy_text_color
val DialogTitleColor = Color(0xFF333333)        // dialog_title_color
val DialogMessageColor = Color(0xFF78787A)      // dialog_message_color
val DialogButtonTextColor = Color(0xFFFFFFFF)   // dialog_button_text_color
val ErrorIconDescColor = Color(0xFF78787A)      // Genérico para descripciones
val InfoBlue = Color(0xFF2196F3)

// ================= MATERIAL3 COLOR SCHEMES =================

/**
 * Esquema de colores CLARO (Light Mode)
 * Mapea la paleta de NexoSolar a roles semánticos de Material3
 */
val NexoLightColorScheme = lightColorScheme(
    // PRIMARIOS (Botones principales, acciones destacadas)
    primary = NexoGreenLight,              // Verde marca (botones, FABs)
    onPrimary = Color.White,               // Texto sobre primary
    primaryContainer = Color(0xFFE8F5D7), // Fondo suave para chips/tags verdes
    onPrimaryContainer = NexoGreenDark,    // Texto sobre primaryContainer

    // SECUNDARIOS (Accents, elementos de apoyo)
    secondary = SmartSolarGreen,           // Verde SmartSolar (tabs, iconos)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEDC8),
    onSecondaryContainer = Color(0xFF33691E),

    // SUPERFICIES (Fondos de cards, dialogs, sheets)
    surface = myGray,                 // Fondo de cards, dialogs
    onSurface = DarkerGray,                // Texto principal sobre surface (negro)
    surfaceVariant = MainBackground,       // Fondo de pantallas (gris muy claro)
    onSurfaceVariant = DarkGray,           // Texto secundario (gris medio)

    // FONDO GENERAL
    background = MainBackground,           // Fondo de la app
    onBackground = DarkerGray,             // Texto sobre background

    // ERRORES
    error = TextAlert,                     // Rojo para errores/pendientes
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // BORDES Y DIVISORES
    outline = InvoiceDivider,              // Dividers, bordes de campos
    outlineVariant = SkeletonDivider,      // Dividers más sutiles (skeletons)

    // SCRIM (overlay oscuro para modals)
    scrim = Color.Black.copy(alpha = 0.32f)
)

/**
 * Esquema de colores OSCURO (Dark Mode)
 * Invierte luminosidad y adapta contrastes
 */
val NexoDarkColorScheme = darkColorScheme(
    // PRIMARIOS
    primary = NexoGreenLight,              // Verde más brillante para destacar
    onPrimary = Color(0xFF1A3300),         // Texto oscuro sobre verde
    primaryContainer = NexoGreenDark,      // Contenedor más oscuro
    onPrimaryContainer = Color(0xFFD4E8BC),

    // SECUNDARIOS
    secondary = SmartSolarGreen,
    onSecondary = Color(0xFF1B3A00),
    secondaryContainer = Color(0xFF2E5A00),
    onSecondaryContainer = Color(0xFFDCEDC8),

    // SUPERFICIES (fondos oscuros)
    surface = Color(0xFF1C1B1F),           // Gris muy oscuro para cards
    onSurface = Color(0xFFE6E1E5),         // Texto claro
    surfaceVariant = Color(0xFF121212),    // Fondo de pantallas (negro suave)
    onSurfaceVariant = Color(0xFFCAC4D0), // Texto secundario

    // FONDO GENERAL
    background = Color(0xFF121212),        // Negro puro suavizado
    onBackground = Color(0xFFE6E1E5),

    // ERRORES
    error = Color(0xFFFFB4AB),             // Rojo más suave
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // BORDES Y DIVISORES
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),

    scrim = Color.Black.copy(alpha = 0.6f)
)
