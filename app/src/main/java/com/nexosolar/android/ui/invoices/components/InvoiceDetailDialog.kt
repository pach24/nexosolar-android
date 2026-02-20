package com.nexosolar.android.ui.invoices.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nexosolar.android.R
import androidx.compose.ui.window.DialogProperties


/**
 * Diálogo simple que informa que la funcionalidad no está disponible.
 */
@Composable
fun NotAvailableDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = { Text(text = stringResource(R.string.informacion)) },
        text = { Text(text = stringResource(R.string.mensaje_funcionalidad_no_disponible)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cerrar), color= colorResource(id = R.color.my_theme_green_dark))
            }
        }
    )
}

// =================================================================
// PREVIEW
// =================================================================

@Preview(showBackground = true)
@Composable
private fun NotAvailableDialogPreview() {
    NotAvailableDialog(onDismiss = {})
}
