package com.nexosolar.android.ui.invoices.components



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexosolar.android.R
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceState
import com.nexosolar.android.ui.theme.NexoSolarTheme
import java.time.LocalDate
import java.util.Locale



@Composable
fun InvoiceItem(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val isPaid = invoice.estadoEnum == InvoiceState.PAID
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dimensionResource(id = R.dimen.invoice_item_padding_horizontal))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isPaid) 22.dp else 16.dp),
            // Alineamos al Top para que la Fecha siempre empiece en la misma coordenada Y
            verticalAlignment = Alignment.Top
        ) {
            // -- COLUMNA IZQUIERDA (Fecha y Estado) --
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = DateUtils.formatDate(invoice.invoiceDate).ifEmpty { stringResource(R.string.sin_fecha) },
                    color = Color.Black,
                    fontSize = dimensionResource(id = R.dimen.invoice_item_date_text_size).value.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(
                        start = dimensionResource(id = R.dimen.invoice_item_date_margin_start),
                        // El margen superior es SIEMPRE el mismo para que no salte
                        top = 0.dp
                    )
                )

                if (invoice.estadoEnum != InvoiceState.PAID) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = getStatusTextRes(invoice.estadoEnum)),
                        color = getStatusColor(invoice.estadoEnum),
                        fontSize = dimensionResource(id = R.dimen.invoice_item_state_text_size).value.sp,
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.invoice_item_state_margin_start),

                        )
                    )
                } else {
                    // Si está pagada, añadimos un espacio equivalente al estado para que
                    // el ítem mantenga una altura similar y no se vea "flaco"
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }

            // -- COLUMNA DERECHA (Importe y Flecha) --
            // Usamos un Modifier.align(Alignment.CenterVertically) solo aquí
            // para que el importe y la flecha sí estén centrados respecto al bloque izquierdo
            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%.2f €", invoice.invoiceAmount),
                    color = Color.Black,
                    fontSize = dimensionResource(id = R.dimen.invoice_item_amount_text_size).value.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(end = 15.dp)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back_24),
                    contentDescription = stringResource(R.string.icono_flecha_item_factura),
                    tint = colorResource(id = R.color.dark_gray),
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(scaleX = -1f)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = dimensionResource(id = R.dimen.invoice_item_divider_height),
            color = colorResource(id = R.color.invoice_item_divider_color)
        )
    }
}

// Helpers privados para mantener limpio el composable principal
@Composable
private fun getStatusTextRes(state: InvoiceState): Int {
    return when (state) {
        InvoiceState.PENDING -> R.string.estado_pendiente
        InvoiceState.CANCELLED -> R.string.estado_anulada
        InvoiceState.FIXED_FEE -> R.string.estado_cuota_fija
        InvoiceState.PAYMENT_PLAN -> R.string.estado_plan_pago
        else -> R.string.estado_desconocido
    }
}

@Composable
private fun getStatusColor(state: InvoiceState): Color {
    return when (state) {
        InvoiceState.PENDING, InvoiceState.CANCELLED -> colorResource(id = R.color.texto_alerta) // Rojo/Naranja
        else -> Color.Black
    }
}

// =================================================================
// PREVIEWS
// =================================================================

@Preview(showBackground = true, name = "Factura Pendiente")
@Composable
private fun InvoiceItemPendingPreview() {
    NexoSolarTheme {
        InvoiceItem(
            invoice = Invoice(
                invoiceAmount = 150.50f,
                invoiceDate = LocalDate.of(2023, 9, 10),
                invoiceStatus = "Pendiente de pago" // String raw, pero el enum lo pilla
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Factura Pagada (Sin estado)")
@Composable
private fun InvoiceItemPaidPreview() {
    NexoSolarTheme {
        InvoiceItem(
            invoice = Invoice(
                invoiceAmount = 89.99f,
                invoiceDate = LocalDate.of(2023, 10, 25),
                invoiceStatus = "Pagada" // Debería mapear a PAID
            ),
            onClick = {}
        )
    }
}

