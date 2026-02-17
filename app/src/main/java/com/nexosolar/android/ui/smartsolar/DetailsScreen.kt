package com.nexosolar.android.ui.smartsolar

// Imports limpios para evitar conflictos
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

// Material 3 explícito
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

// Hilt & Lifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Proyecto
import com.nexosolar.android.R
import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.ui.common.ErrorView
import com.nexosolar.android.ui.theme.NexoSolarTheme

/**
 * **Route del DetailsScreen**
 */
@Composable
fun DetailsRoute(
    viewModel: InstallationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailsScreen(
        uiState = uiState,
        onRefresh = viewModel::onRefresh
    )
}

/**
 * **Screen principal de Detalles**
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    uiState: InstallationUIState,
    onRefresh: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when (uiState) {
                is InstallationUIState.Loading -> {
                    DetailsSkeleton()
                }

                is InstallationUIState.Error -> {
                    ErrorView(
                        message = stringResource(id = uiState.messageRes),
                        errorType = uiState.type,
                        onRetry = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is InstallationUIState.Empty -> {
                    // Usamos el wrapper interno
                    DetailsPullToRefresh(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh
                    ) {
                        ErrorView(
                            // CORREGIDO: ID de string genérico
                            message = stringResource(R.string.error_message_generic),
                            errorType = ErrorClassifier.ErrorType.Unknown(null),
                            onRetry = onRefresh,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                is InstallationUIState.Success -> {
                    // Usamos el wrapper interno
                    DetailsPullToRefresh(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh
                    ) {
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

/**
 * **Contenido visual de Detalles**
 */
@Composable
fun DetailsContent(
    installation: Installation,
    onInfoClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(dimensionResource(id = R.dimen.detail_content_padding))
    ) {
        // 1. CAU
        DetailItem(
            label = stringResource(R.string.cau_label),
            value = installation.selfConsumptionCode
        )

        DetailDivider()

        // 2. ESTADO SOLICITUD
        DetailItemWithInfo(
            label = stringResource(R.string.estado_solicitud_alta_autoconsumidor),
            value = installation.installationStatus,
            onInfoClick = onInfoClick
        )

        DetailDivider()

        // 3. TIPO AUTOCONSUMO
        DetailItem(
            label = stringResource(R.string.tipo_autoconsumo),
            value = installation.installationType
        )

        DetailDivider()

        // 4. COMPENSACIÓN
        DetailItem(
            // CORREGIDO: ID exacto del XML
            label = stringResource(R.string.compensaci_n_de_excedentes),
            value = installation.compensation
        )

        DetailDivider()

        // 5. POTENCIA
        DetailItem(
            // CORREGIDO: ID exacto del XML
            label = stringResource(R.string.potencia_de_instalaci_n),
            value = installation.power
        )

        DetailDivider()
    }
}

/**
 * **Item de detalle simple**
 */
@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            color = colorResource(id = R.color.detail_label_color),
            fontSize = dimensionResource(id = R.dimen.detail_label_text_size).value.sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_value_margin_top)))
        Text(
            text = value,
            color = colorResource(id = R.color.detail_value_color),
            fontSize = dimensionResource(id = R.dimen.detail_value_text_size).value.sp
        )
    }
}

/**
 * **Item de detalle con icono info**
 */
@Composable
private fun DetailItemWithInfo(
    label: String,
    value: String,
    onInfoClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            color = colorResource(id = R.color.detail_label_color),
            fontSize = dimensionResource(id = R.dimen.detail_label_text_size).value.sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.detail_value_margin_top)))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = colorResource(id = R.color.detail_value_color),
                fontSize = dimensionResource(id = R.dimen.detail_value_text_size).value.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.tooltip_azul_estados),
                // CORREGIDO: ID exacto del XML
                contentDescription = stringResource(R.string.m_s_informaci_n),
                tint = colorResource(id = R.color.detail_info_tint),
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
            // Margen pequeño ARRIBA (separación del valor actual)
            top = dimensionResource(id = R.dimen.detail_divider_margin_top),
            // Margen grande ABAJO (separación de la siguiente sección)
            bottom = dimensionResource(id = R.dimen.detail_section_margin_top)
        ),
        thickness = dimensionResource(id = R.dimen.detail_divider_height),
        color = colorResource(id = R.color.detail_divider_color)
    )
}


/**
 * **Pull-to-Refresh Wrapper (RENOMBRADO)**
 * He cambiado el nombre a 'DetailsPullToRefresh' para evitar conflictos de nombres
 * con otras funciones o duplicados en el build.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsPullToRefresh(
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

/**
 * **Preview**
 */
@Preview(showBackground = true, name = "Details - Success")
@Composable
private fun DetailsScreenPreview() {
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
