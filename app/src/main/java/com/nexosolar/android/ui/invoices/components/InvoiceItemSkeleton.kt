package com.nexosolar.android.ui.invoices.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexosolar.android.R
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun InvoiceItemSkeleton(
    modifier: Modifier = Modifier
) {
    // ✅ Animación que traslada el gradiente continuamente
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Distancia del desplazamiento
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val isPaid = false

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.invoice_item_padding_horizontal))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isPaid) 22.dp else 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Fecha shimmer
                ShimmerBox(
                    translateAnim = translateAnim,
                    width = 150.dp,
                    height = dimensionResource(id = R.dimen.invoice_item_date_text_size),
                    modifier = Modifier.padding(
                        start = dimensionResource(id = R.dimen.invoice_item_date_margin_start),
                        top = 0.dp
                    )
                )

                if (!isPaid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        translateAnim = translateAnim,
                        width = 120.dp,
                        height = dimensionResource(id = R.dimen.invoice_item_state_text_size),
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.invoice_item_state_margin_start)
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                ShimmerBox(
                    translateAnim = translateAnim,
                    width = 90.dp,
                    height = dimensionResource(id = R.dimen.invoice_item_amount_text_size),
                    modifier = Modifier.padding(end = 15.dp)
                )

                ShimmerBox(
                    translateAnim = translateAnim,
                    width = 20.dp,
                    height = 20.dp
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

/**
 * Box con efecto shimmer deslizante.
 *
 * @param translateAnim Valor de traslación horizontal (0f → 1000f continuamente).
 * @param width Ancho del box.
 * @param height Alto del box.
 */
@Composable
private fun ShimmerBox(
    translateAnim: Float,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0E0E0), // Gris claro (base)
                        Color(0xFFF5F5F5), // Blanco (brillo)
                        Color(0xFFE0E0E0)  // Gris claro (base)
                    ),
                    // ✅ La clave: start y end se mueven con translateAnim
                    start = Offset(x = translateAnim - 200f, y = 0f),
                    end = Offset(x = translateAnim + 200f, y = 0f)
                )
            )
    )
}

// =================================================================
// PREVIEWS SKELETON
// =================================================================

@Preview(showBackground = true, name = "Skeleton No Pagada")
@Composable
private fun InvoiceItemSkeletonPreview() {
    NexoSolarTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            InvoiceItemSkeleton()
        }
    }
}

@Preview(showBackground = true, name = "Skeleton Pagada (Sin estado)")
@Composable
private fun InvoiceItemSkeletonPagadaPreview() {
    NexoSolarTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            InvoiceItemSkeletonPagada()
        }
    }
}
/*
@Preview(showBackground = true, name = "Lista Skeleton (12 items)")
@Composable
private fun InvoiceListSkeletonPreview() {
    NexoSolarTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(count = 12, key = { it }) {
                    InvoiceItemSkeleton()
                }
            }
        }
    }
}
*/


// Helper para preview pagada
@Composable
private fun InvoiceItemSkeletonPagada() {
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val isPaid = true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.invoice_item_padding_horizontal))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isPaid) 22.dp else 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    translateAnim = translateAnim,
                    width = 90.dp,
                    height = dimensionResource(id = R.dimen.invoice_item_date_text_size),
                    modifier = Modifier.padding(
                        start = dimensionResource(id = R.dimen.invoice_item_date_margin_start),
                        top = 0.dp
                    )
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                ShimmerBox(
                    translateAnim = translateAnim,
                    width = 80.dp,
                    height = dimensionResource(id = R.dimen.invoice_item_amount_text_size),
                    modifier = Modifier.padding(end = 15.dp)
                )
                ShimmerBox(
                    translateAnim = translateAnim,
                    width = 20.dp,
                    height = 20.dp
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
