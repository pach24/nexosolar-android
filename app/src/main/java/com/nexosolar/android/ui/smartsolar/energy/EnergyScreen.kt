package com.nexosolar.android.ui.smartsolar.energy

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.nexosolar.android.R
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun EnergyRoute() {
    EnergyScreen()
}

@Composable
fun EnergyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                top = dimensionResource(R.dimen.empty_energy_padding_top),
                start = dimensionResource(R.dimen.empty_energy_padding_horizontal),
                end = dimensionResource(R.dimen.empty_energy_padding_horizontal)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.plan_gestiones),
            contentDescription = stringResource(R.string.trabajo_en_progreso),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(dimensionResource(R.dimen.empty_energy_image_size))
        )
        Spacer(
            modifier = Modifier.height(dimensionResource(R.dimen.empty_energy_space_between))
        )
        Text(
            text = stringResource(R.string.desc_energy_fragment),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = dimensionResource(R.dimen.empty_energy_text_size).value.sp,
            textAlign = TextAlign.Center,
            lineHeight = (dimensionResource(R.dimen.empty_energy_text_size).value * 1.2f).sp,
            letterSpacing = 0.02.sp,
            modifier = Modifier.width(dimensionResource(R.dimen.empty_energy_text_width))
        )
    }
}

@Preview(showBackground = true, name = "Energy - Light Mode")
@Composable
private fun EnergyScreenPreview() {
    NexoSolarTheme {
        EnergyScreen()
    }
}

@Preview(
    showBackground = true,
    name = "Energy - Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EnergyScreenDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        EnergyScreen()
    }
}
