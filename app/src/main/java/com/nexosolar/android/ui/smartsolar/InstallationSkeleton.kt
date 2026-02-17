package com.nexosolar.android.ui.smartsolar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.nexosolar.android.R

import com.nexosolar.android.ui.common.shimmerEffect // Importa tu nuevo modifier

@Preview
@Composable
fun InstallationSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(dimensionResource(id = R.dimen.energy_detail_screen_padding))
    ) {
        // 1. SKELETON DESCRIPCIÓN (Simulamos 2 líneas de texto)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f) // 80% del ancho
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()

        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f) // 60% del ancho (segunda línea)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_autoconsumo_margin_top)))

        // 2. SKELETON ETIQUETA + VALOR
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(30.dp) // Un poco más alto para simular el número grande
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_chart_margin_top)))

        // 3. SKELETON GRÁFICO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.energy_detail_chart_height))
                .clip(RoundedCornerShape(8.dp))
                .shimmerEffect()


        )
    }
}
