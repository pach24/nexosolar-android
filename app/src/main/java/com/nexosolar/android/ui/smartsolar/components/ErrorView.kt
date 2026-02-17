package com.nexosolar.android.ui.smartsolar.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
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
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun ErrorView(
    message: String,
    errorType: ErrorClassifier.ErrorType?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val type = errorType ?: ErrorClassifier.ErrorType.Unknown(null)
    val uiConfig = type.toUiConfig()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = uiConfig.iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(uiConfig.iconSize)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit,
            colorFilter = if (type is ErrorClassifier.ErrorType.Network) {
                ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            } else null
        )

        Text(
            text = stringResource(id = uiConfig.titleRes),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.widthIn(min = 140.dp)
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

private data class ErrorUiConfig(
    val titleRes: Int,
    val iconRes: Int,
    val iconSize: Dp
)

private fun ErrorClassifier.ErrorType.toUiConfig(): ErrorUiConfig {
    return when (this) {
        is ErrorClassifier.ErrorType.Network -> ErrorUiConfig(
            titleRes = R.string.error_conexion,
            iconRes = R.drawable.ic_wifi_off_24,
            iconSize = 100.dp
        )
        else -> ErrorUiConfig(
            titleRes = R.string.error_generic_title,
            iconRes = R.drawable.ic_error_installation,
            iconSize = 200.dp
        )
    }
}

@Preview(showBackground = true, name = "Network Error (Light)")
@Composable
private fun ErrorViewNetworkLightPreview() {
    NexoSolarTheme(darkTheme = false) {
        ErrorView(
            message = stringResource(R.string.error_conexion_description_message),
            errorType = ErrorClassifier.ErrorType.Network("No internet"),
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Network Error (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorViewNetworkDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        ErrorView(
            message = stringResource(R.string.error_conexion_description_message),
            errorType = ErrorClassifier.ErrorType.Network("No internet"),
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Generic Error")
@Composable
private fun ErrorViewGenericPreview() {
    NexoSolarTheme {
        ErrorView(
            message = stringResource(R.string.error_message_generic),
            errorType = ErrorClassifier.ErrorType.Unknown(null),
            onRetry = {}
        )
    }
}
@Preview(
    showBackground = true,
    name = "Generic Error (Dark)",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ErrorViewGenericDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        ErrorView(
            message = stringResource(R.string.error_message_generic),
            errorType = ErrorClassifier.ErrorType.Unknown(null),
            onRetry = {}
        )
    }
}

