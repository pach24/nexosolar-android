package com.nexosolar.android.ui.invoices.components

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexosolar.android.R
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun InvoiceItemSkeleton(
    shimmerTranslate: Float,
    modifier: Modifier = Modifier
) {
    val isPaid = false
    val isDark = isSystemInDarkTheme()


    val baseColor = if (isDark) Color(0xFF424242) else Color(0xFFE0E0E0)
    val highlightColor = if (isDark) Color(0xFF616161) else Color(0xFFF5F5F5)
    val shimmerColors = listOf(baseColor, highlightColor, baseColor)

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = shimmerTranslate - 200f, y = shimmerTranslate - 200f),
        end = Offset(x = shimmerTranslate, y = shimmerTranslate)
    )

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
                ShimmerBox(
                    brush = brush,
                    width = 160.dp,
                    height = dimensionResource(id = R.dimen.invoice_item_date_text_size),
                    modifier = Modifier.padding(
                        start = dimensionResource(id = R.dimen.invoice_item_date_margin_start),
                        top = 0.dp
                    )
                )

                if (!isPaid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        brush = brush,
                        width = 100.dp,
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
                    brush = brush,
                    width = 70.dp,
                    height = dimensionResource(id = R.dimen.invoice_item_amount_text_size),
                    modifier = Modifier.padding(end = 15.dp)
                )

                ShimmerBox(
                    brush = brush,
                    width = 30.dp,
                    height = 30.dp
                )
            }
        }

        // 3. Adaptamos el divisor dinámicamente.
        val dividerColor = if (isDark) Color(0xFF424242) else Color(0xFFEEEEEE)

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = dimensionResource(id = R.dimen.invoice_item_divider_height),
            color = dividerColor
        )
    }
}

/**
 * Componente UI tonto (Dumb Component).
 * Solo recibe las dimensiones y el Brush ya calculado.
 */
@Composable
private fun ShimmerBox(
    brush: Brush,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .background(
                brush = brush,
                shape = RoundedCornerShape(8.dp)
            )
    )
}

// ================= PREVIEWS =================

@Preview(showBackground = true, name = "Skeleton Animated (Light)")
@Composable
private fun InvoiceItemSkeletonLightPreview() {
    NexoSolarTheme(darkTheme = false) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )
        // Usamos Surface para que el color de fondo simule la app real y el contraste sea visible
        Surface(color = MaterialTheme.colorScheme.surface) {
            InvoiceItemSkeleton(shimmerTranslate = translateAnim)
        }
    }
}

@Preview(showBackground = true, name = "Skeleton Animated (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InvoiceItemSkeletonDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )
        Surface(color = MaterialTheme.colorScheme.surface) {
            InvoiceItemSkeleton(shimmerTranslate = translateAnim)
        }
    }
}
