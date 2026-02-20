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
import com.nexosolar.android.ui.invoices.InvoiceListItemUi
import com.nexosolar.android.domain.models.InvoiceState
import com.nexosolar.android.ui.theme.NexoSolarTheme



@Composable
fun InvoiceItem(
    invoice: InvoiceListItemUi,
    onClick: () -> Unit
) {
    val isPaid = invoice.state == InvoiceState.PAID

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = dimensionResource(R.dimen.invoice_item_padding_horizontal))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isPaid) 22.dp else 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // -- COLUMNA IZQUIERDA --
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.dateText.ifEmpty { stringResource(R.string.sin_fecha) },
                    color = MaterialTheme.colorScheme.onSurface,          // ← fix
                    fontSize = dimensionResource(R.dimen.invoice_item_date_text_size).value.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(
                        start = dimensionResource(R.dimen.invoice_item_date_margin_start)
                    )
                )

                if (invoice.state != InvoiceState.PAID) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = getStatusTextRes(invoice.state)),
                        color = getStatusColor(invoice.state),             // ← fix interno
                        fontSize = dimensionResource(R.dimen.invoice_item_state_text_size).value.sp,
                        modifier = Modifier.padding(
                            start = dimensionResource(R.dimen.invoice_item_state_margin_start)
                        )
                    )
                }
            }

            // -- COLUMNA DERECHA --
            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = invoice.amountText,
                    color = MaterialTheme.colorScheme.onSurface,          // ← fix
                    fontSize = dimensionResource(R.dimen.invoice_item_amount_text_size).value.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(end = 15.dp)
                )

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24),
                    contentDescription = stringResource(R.string.icono_flecha_item_factura),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,    // ← fix
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(scaleX = -1f)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = dimensionResource(R.dimen.invoice_item_divider_height),
            color = MaterialTheme.colorScheme.outline             // ← fix
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
        InvoiceState.PENDING,
        InvoiceState.CANCELLED -> MaterialTheme.colorScheme.error         // ← fix (rojo semántico)
        else -> MaterialTheme.colorScheme.onSurface                       // ← fix
    }
}


// =================================================================
// PREVIEWS
// =================================================================

@Preview(showBackground = true, name = "Pendiente · Light")
@Composable
private fun InvoiceItemPendingLightPreview() {
    NexoSolarTheme {
        InvoiceItem(
            invoice = InvoiceListItemUi(id = 1, amountText = "150,50 €", dateText = "10 sep 2023", state = InvoiceState.PENDING),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Pendiente · Dark")
@Composable
private fun InvoiceItemPendingDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        InvoiceItem(
            invoice = InvoiceListItemUi(id = 1, amountText = "150,50 €", dateText = "10 sep 2023", state = InvoiceState.PENDING),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Pagada · Light")
@Composable
private fun InvoiceItemPaidLightPreview() {
    NexoSolarTheme {
        InvoiceItem(
            invoice = InvoiceListItemUi(id = 2, amountText = "89,99 €", dateText = "25 oct 2023", state = InvoiceState.PAID),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Pagada · Dark")
@Composable
private fun InvoiceItemPaidDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        InvoiceItem(
            invoice = InvoiceListItemUi(id = 2, amountText = "89,99 €", dateText = "25 oct 2023", state = InvoiceState.PAID),
            onClick = {}
        )
    }
}



