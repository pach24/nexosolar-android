package com.nexosolar.android.ui.smartsolar.installation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.ui.smartsolar.InstallationViewModel
import com.nexosolar.android.ui.smartsolar.components.ErrorView
import com.nexosolar.android.ui.smartsolar.models.InstallationUIState
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun InstallationRoute(viewModel: InstallationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InstallationScreen(uiState = uiState, onRefresh = viewModel::onRefresh)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationScreen(
    uiState: InstallationUIState,
    onRefresh: () -> Unit
) {
    // Fondo global para evitar cortes visuales
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is InstallationUIState.Loading -> InstallationSkeleton()
            is InstallationUIState.Error -> {
                PullToRefreshContent(isRefreshing = false, onRefresh = onRefresh) {
                    ErrorView(
                        message = stringResource(id = uiState.messageRes),
                        errorType = uiState.type,
                        onRetry = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is InstallationUIState.Empty -> {
                PullToRefreshContent(isRefreshing = uiState.isRefreshing, onRefresh = onRefresh) {
                    ErrorView(
                        message = stringResource(R.string.error_message_generic),
                        errorType = ErrorClassifier.ErrorType.Unknown(null),
                        onRetry = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            is InstallationUIState.Success -> {
                PullToRefreshContent(isRefreshing = uiState.isRefreshing, onRefresh = onRefresh) {
                    InstallationContent(installation = uiState.installation)
                }
            }
        }
    }
}

@Composable
fun InstallationContent(
    installation: Installation,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.energy_detail_screen_padding))
    ) {
        Text(
            text = stringResource(id = R.string.desc_energy_detail),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = dimensionResource(id = R.dimen.energy_detail_text_size).value.sp,
            lineHeight = (dimensionResource(id = R.dimen.energy_detail_text_size).value * 1.2f).sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_autoconsumo_margin_top)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = R.string.autoconsumo),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = dimensionResource(id = R.dimen.energy_detail_label_text_size).value.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "92%",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.energy_detail_value_text_size).value.sp
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.energy_detail_chart_margin_top)))
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


@Composable
fun PullToRefreshContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState() // ← hoisted aquí igual que en Invoices

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        modifier = Modifier.fillMaxSize(),
        // 👇 AÑADIMOS EL INDICADOR PERSONALIZADO AQUÍ 👇
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,          // ← misma referencia
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface, // Fondo de la bolita
                color = MaterialTheme.colorScheme.primary           // Color del spinner (Verde NexoSolar)
            )
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = this.maxHeight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // heightIn(min = minHeight) garantiza que el contenido ocupe al menos toda la pantalla
                // para que el fondo blanco no se corte si el contenido es corto.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                ) {
                    content()
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Success (Light)")
@Composable
private fun InstallationScreenSuccessPreview() {
    NexoSolarTheme {
        Box() {
            InstallationContent(installation = Installation())
        }
    }
}
