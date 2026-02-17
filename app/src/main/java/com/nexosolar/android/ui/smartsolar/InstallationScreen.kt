package com.nexosolar.android.ui.smartsolar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexosolar.android.R
import com.nexosolar.android.domain.models.Installation

// 1. ROUTE: Conecta ViewModel con UI
@Composable
fun InstallationRoute(
    viewModel: InstallationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InstallationScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh
    )
}

// 2. SCREEN: Gestiona los estados (Loading, Error, Success)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationScreen(
    uiState: InstallationUIState,
    onRefresh: () -> Unit
) {
    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when (uiState) {
                is InstallationUIState.Loading -> {
                    InstallationSkeleton()
                }
                is InstallationUIState.Error -> {
                    Text(
                        text = "Error: ${uiState.message}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is InstallationUIState.Empty -> {
                    PullToRefreshContent(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh
                    ) {
                        Text(
                            text = "No se encontraron datos.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                is InstallationUIState.Success -> {
                    PullToRefreshContent(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh
                    ) {
                        InstallationContent(installation = uiState.installation)
                    }
                }
            }
        }
    }
}

// 3. CONTENT: El diseño visual específico (tu XML migrado)
@Composable
fun InstallationContent(installation: Installation) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(dimensionResource(id = R.dimen.energy_detail_screen_padding))
    ) {
        // 1. TEXTO DESCRIPTIVO
        Text(
            text = stringResource(id = R.string.desc_energy_detail),
            color = colorResource(id = R.color.energy_detail_text_color),
            fontSize = dimensionResource(id = R.dimen.energy_detail_text_size).value.sp,
            lineHeight = (dimensionResource(id = R.dimen.energy_detail_text_size).value * 1.2f).sp
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_autoconsumo_margin_top)))

        // 2. ROW: ETIQUETA + VALOR
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.autoconsumo),
                color = colorResource(id = R.color.energy_detail_label_color),
                fontSize = dimensionResource(id = R.dimen.energy_detail_label_text_size).value.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(id = R.string._92),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.energy_detail_value_text_size).value.sp
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_chart_margin_top)))

        // 3. IMAGEN DEL GRÁFICO
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.grafico1),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.energy_detail_chart_width))
                    .height(dimensionResource(id = R.dimen.energy_detail_chart_height))
            )
        }
    }
}

// 4. HELPER: Pull to Refresh
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}

// 5. PREVIEWS: Para ver el diseño sin ejecutar
@Preview(showBackground = true, name = "Success Preview")
@Composable
private fun InstallationScreenPreview() {
    com.nexosolar.android.ui.theme.NexoSolarTheme {
        InstallationContent(
            installation = Installation()
        )
    }
}
