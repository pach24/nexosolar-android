package com.nexosolar.android.ui.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nexosolar.android.R
import com.nexosolar.android.ui.theme.NexoGreenLight
import com.nexosolar.android.ui.theme.NexoSolarTheme

/**
 * **Diálogo de información**
 *
 * Muestra el estado de la solicitud de autoconsumo.
 * Usa las dimensiones definidas en `dimens.xml` para `dialog_*`.
 */
@Composable
fun InfoDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { /* No se puede cerrar tocando fuera */ }) {
        Card(
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dialog_card_corner_radius)),
            elevation = CardDefaults.cardElevation(
                defaultElevation = dimensionResource(id = R.dimen.dialog_card_elevation)
            ),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.dialog_card_background)
            ),
            modifier = Modifier.wrapContentSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.dialog_content_padding))
                    .width(IntrinsicSize.Min) // Ajusta al contenido
            ) {
                // Título
                Text(
                    text = stringResource(R.string.autoconsumo_status_title),
                    color = colorResource(id = R.color.dialog_title_color),
                    fontSize = dimensionResource(id = R.dimen.dialog_title_text_size).value.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.dialog_title_width))
                        .padding(
                            top = dimensionResource(id = R.dimen.dialog_title_margin_top),
                            bottom = dimensionResource(id = R.dimen.dialog_title_margin_bottom)
                        )
                )

                // Mensaje
                Text(
                    text = stringResource(R.string.autoconsumo_status_message),
                    color = colorResource(id = R.color.dialog_message_color),
                    fontSize = dimensionResource(id = R.dimen.dialog_message_text_size).value.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = (dimensionResource(id = R.dimen.dialog_message_text_size).value +
                            dimensionResource(id = R.dimen.dialog_message_line_spacing_extra).value).sp,
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.dialog_message_width))
                        .padding(bottom = dimensionResource(id = R.dimen.dialog_message_margin_bottom))
                )

                // Botón Aceptar
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dialog_button_corner_radius)),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        containerColor = NexoGreenLight,
                        contentColor = colorResource(id = R.color.dialog_button_text_color)
                    ),
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.dialog_button_width))
                        .height(dimensionResource(id = R.dimen.dialog_button_height))
                ) {
                    Text(
                        text = stringResource(R.string.accept),
                        fontSize = dimensionResource(id = R.dimen.dialog_button_text_size).value.sp
                    )
                }
            }
        }
    }
}

/**
 * **Preview del diálogo**
 */
@Preview(showBackground = true, name = "Info Dialog")
@Composable
private fun InfoDialogPreview() {
    NexoSolarTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            InfoDialog(onDismiss = {})
        }
    }
}
