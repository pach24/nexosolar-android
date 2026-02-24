package com.nexosolar.android.ui.smartsolar.installation

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nexosolar.android.R
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun InstallationSkeleton() {
    val isDark = isSystemInDarkTheme()
    val baseColor = if (isDark) Color(0xFF424242) else Color(0xFFE0E0E0)
    val highlightColor = if (isDark) Color(0xFF616161) else Color(0xFFF5F5F5)
    val shimmerColors = listOf(baseColor, highlightColor, baseColor)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
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
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensionResource(id = R.dimen.energy_detail_screen_padding))
    ) {
        // Descripción (2 líneas simuladas)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_autoconsumo_margin_top)))

        // Etiqueta + Valor
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_chart_margin_top)))

        // Gráfico principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.energy_detail_chart_height))
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun InstallationSkeletonLightPreview() {
    NexoSolarTheme(darkTheme = false) {
        InstallationSkeleton()
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InstallationSkeletonDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        InstallationSkeleton()
    }
}
