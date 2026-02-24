package com.nexosolar.android.ui.smartsolar.details

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nexosolar.android.R
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun DetailsSkeleton() {
    val isDark = isSystemInDarkTheme()
    val baseColor = if (isDark) Color(0xFF424242) else Color(0xFFE0E0E0)
    val highlightColor = if (isDark) Color(0xFF616161) else Color(0xFFF5F5F5)
    val shimmerColors = listOf(baseColor, highlightColor, baseColor)

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

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 200f, y = translateAnim - 200f),
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensionResource(id = R.dimen.detail_content_padding))
    ) {
        repeat(5) { index ->
            SkeletonDetailItem(
                brush = brush,
                showInfoPlaceholder = (index == 1) // Solo el 2º item tiene icono info
            )
            if (index < 4) {
                SkeletonDivider()
            }
        }
    }
}

@Composable
private fun SkeletonDetailItem(
    brush: Brush,
    showInfoPlaceholder: Boolean
) {
    val shape = RoundedCornerShape(4.dp)
    Column {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(14.dp)
                .background(brush, shape)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_value_margin_top)))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .background(brush, shape)
            )
            if (showInfoPlaceholder) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.detail_info_icon_size))
                        .background(brush, shape)
                )
            }
        }
    }
}

@Composable
private fun SkeletonDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(
            top = dimensionResource(id = R.dimen.detail_divider_margin_top),
            bottom = dimensionResource(id = R.dimen.detail_section_margin_top)
        ),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
@Preview(showBackground = true, name = "Skeleton Animated (Light)")
@Composable
private fun DetailsSkeletonLightPreview() {
    NexoSolarTheme(darkTheme = false) {
        DetailsSkeleton()
    }
}

@Preview(showBackground = true, name = "Skeleton Animated (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailsSkeletonDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        DetailsSkeleton()
    }
}
