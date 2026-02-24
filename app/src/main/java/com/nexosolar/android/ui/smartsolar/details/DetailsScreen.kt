package com.nexosolar.android.ui.smartsolar.details

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.nexosolar.android.ui.smartsolar.components.InfoDialog
import com.nexosolar.android.ui.smartsolar.models.InstallationUIState
import com.nexosolar.android.ui.theme.InfoBlue
import com.nexosolar.android.ui.theme.NexoSolarTheme

@Composable
fun DetailsRoute(viewModel: InstallationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailsScreen(uiState = uiState, onRefresh = viewModel::onRefresh)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    uiState: InstallationUIState,
    onRefresh: () -> Unit
) {
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState) {
                is InstallationUIState.Loading -> DetailsSkeleton()
                is InstallationUIState.Error -> {
                    DetailsPullToRefresh(isRefreshing = false, onRefresh = onRefresh) {
                        ErrorView(
                            message = stringResource(id = uiState.messageRes),
                            errorType = uiState.type,
                            onRetry = onRefresh,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is InstallationUIState.Empty -> {
                    DetailsPullToRefresh(isRefreshing = uiState.isRefreshing, onRefresh = onRefresh) {
                        ErrorView(
                            message = stringResource(R.string.error_message_generic),
                            errorType = ErrorClassifier.ErrorType.Unknown(null),
                            onRetry = onRefresh,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is InstallationUIState.Success -> {
                    DetailsPullToRefresh(isRefreshing = uiState.isRefreshing, onRefresh = onRefresh) {
                        DetailsContent(
                            installation = uiState.installation,
                            onInfoClick = { showInfoDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        InfoDialog(onDismiss = { showInfoDialog = false })
    }
}

@Composable
fun DetailsContent(
    installation: Installation,
    onInfoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
            .padding(dimensionResource(id = R.dimen.detail_content_padding))
    ) {
        DetailItem(stringResource(R.string.cau_label), installation.selfConsumptionCode)
        DetailDivider()
        DetailItemWithInfo(
            label = stringResource(R.string.estado_solicitud_alta_autoconsumidor),
            value = installation.installationStatus,
            onInfoClick = onInfoClick
        )
        DetailDivider()
        DetailItem(stringResource(R.string.tipo_autoconsumo), installation.installationType)
        DetailDivider()
        DetailItem(stringResource(R.string.compensaci_n_de_excedentes), installation.compensation)
        DetailDivider()
        DetailItem(stringResource(R.string.potencia_de_instalaci_n), installation.power)
        DetailDivider()
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = dimensionResource(id = R.dimen.detail_label_text_size).value.sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_value_margin_top)))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = dimensionResource(id = R.dimen.detail_value_text_size).value.sp
        )
    }
}

@Composable
private fun DetailItemWithInfo(label: String, value: String, onInfoClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = dimensionResource(id = R.dimen.detail_label_text_size).value.sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_value_margin_top)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = dimensionResource(id = R.dimen.detail_value_text_size).value.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.tooltip_azul_estados),
                contentDescription = stringResource(R.string.m_s_informaci_n),
                tint = InfoBlue,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.detail_info_icon_size))
                    .clickable(onClick = onInfoClick)
            )
        }
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(
            top = dimensionResource(id = R.dimen.detail_divider_margin_top),
            bottom = dimensionResource(id = R.dimen.detail_section_margin_top)
        ),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = rememberPullToRefreshState(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Preview(showBackground = true, name = "Success (Light)")
@Composable
private fun DetailsSuccessLightPreview() {
    NexoSolarTheme {
        DetailsContent(
            installation = Installation(
                selfConsumptionCode = "ES0021000000000001JN",
                installationStatus = "Activa",
                installationType = "Residencial",
                compensation = "Con compensación",
                power = "5.5 kW"
            ),
            onInfoClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Success (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailsSuccessDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        DetailsContent(
            installation = Installation(
                selfConsumptionCode = "ES0021000000000001JN",
                installationStatus = "Activa",
                installationType = "Residencial",
                compensation = "Con compensación",
                power = "5.5 kW"
            ),
            onInfoClick = {}
        )
    }
}
