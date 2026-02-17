package com.nexosolar.android.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexosolar.android.R
import com.nexosolar.android.core.ErrorClassifier

/**
 * Vista genérica de error reutilizable.
 * Muestra un icono, título, mensaje y botón de reintentar.
 */
@Composable
fun ErrorView(
    message: String,
    errorType: ErrorClassifier.ErrorType?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Obtenemos la configuración visual (Icono, Título, Tamaño)
    // Si errorType es null, asumimos Unknown
    val uiConfig = (errorType ?: ErrorClassifier.ErrorType.Unknown(null)).toUiConfig()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // 2. Icono dinámico
        Image(
            painter = painterResource(id = uiConfig.iconRes),
            contentDescription = stringResource(R.string.error_icon_desc),
            modifier = Modifier
                .size(uiConfig.iconSize) // Tamaño dinámico
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        // 3. Título dinámico
        Text(
            text = stringResource(id = uiConfig.titleRes),
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 4. Mensaje (viene del ViewModel)
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 5. Botón de Reintentar
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.my_theme_green_light),
                contentColor = Color.White
            ),
            modifier = Modifier.widthIn(min = 140.dp)
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

// =================================================================
// EXTENSIONES & CONFIG
// =================================================================

/**
 * Modelo de configuración visual para errores.
 */
private data class ErrorUiConfig(
    val titleRes: Int,
    val iconRes: Int,
    val iconSize: Dp
)

/**
 * Mapea el tipo de error de dominio a recursos visuales de UI.
 */
private fun ErrorClassifier.ErrorType.toUiConfig(): ErrorUiConfig {
    return when (this) {
        // ERROR DE RED: Icono WiFi pequeño + Título "Sin Conexión"
        is ErrorClassifier.ErrorType.Network -> ErrorUiConfig(
            titleRes = R.string.error_conexion,
            iconRes = R.drawable.ic_wifi_off_24,
            iconSize = 100.dp
        )

        // ERROR DE SERVIDOR: Icono Genérico grande + Título Genérico
        is ErrorClassifier.ErrorType.Server -> ErrorUiConfig(
            titleRes = R.string.error_generic_title, // O "Error de Servidor" si tienes
            iconRes = R.drawable.ic_error_installation,
            iconSize = 200.dp
        )

        // DESCONOCIDO: Igual que servidor
        is ErrorClassifier.ErrorType.Unknown -> ErrorUiConfig(
            titleRes = R.string.error_generic_title,
            iconRes = R.drawable.ic_error_installation,
            iconSize = 200.dp
        )
    }
}

// =================================================================
// PREVIEWS
// =================================================================

@Preview(showBackground = true, name = "Error - Network (Small Icon)")
@Composable
fun ErrorViewNetworkPreview() {
    ErrorView(
        message = stringResource(R.string.error_conexion_description_message),
        errorType = ErrorClassifier.ErrorType.Network("No internet"),
        onRetry = {}
    )
}

@Preview(showBackground = true, name = "Error - Generic (Big Icon)")
@Composable
fun ErrorViewGenericPreview() {
    ErrorView(
        message = stringResource(R.string.error_message_generic),
        errorType = ErrorClassifier.ErrorType.Unknown(null),
        onRetry = {}
    )
}
