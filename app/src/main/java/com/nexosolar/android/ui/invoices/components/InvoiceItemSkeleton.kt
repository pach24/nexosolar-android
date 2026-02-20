package com.nexosolar.android.ui.invoices.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    shimmerTranslate: Float,
    modifier: Modifier = Modifier
) {
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
                ShimmerBox(
                    shimmerTranslate = shimmerTranslate,
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
                        shimmerTranslate = shimmerTranslate,
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
                    shimmerTranslate = shimmerTranslate,
                    width = 70.dp,
                    height = dimensionResource(id = R.dimen.invoice_item_amount_text_size),
                    modifier = Modifier.padding(end = 15.dp)
                )

                ShimmerBox(
                    shimmerTranslate = shimmerTranslate,
                    width = 30.dp,
                    height = 30.dp
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

@Composable
private fun ShimmerBox(
    shimmerTranslate: Float,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0E0E0),
                        Color(0xFFF5F5F5),
                        Color(0xFFE2E2E2)
                    ),
                    start = Offset(x = shimmerTranslate - 200f, y = 0f),
                    end = Offset(x = shimmerTranslate + 200f, y = 0f)
                ),
                shape = RoundedCornerShape(8.dp)
            )
    )
}

@Preview(showBackground = true, name = "Skeleton")
@Composable
private fun InvoiceItemSkeletonPreview() {
    NexoSolarTheme {
        InvoiceItemSkeleton(shimmerTranslate = 400f)
    }
}
