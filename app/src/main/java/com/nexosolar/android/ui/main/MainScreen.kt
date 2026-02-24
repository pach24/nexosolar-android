package com.nexosolar.android.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.nexosolar.android.ui.theme.NexoSolarTheme

// =================================================================
// 1. ROUTE
// =================================================================

@Composable
fun MainRoute(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToInvoices: () -> Unit,
    onNavigateToSmartSolar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MainScreen(
        uiState = uiState,
        onMockToggled = viewModel::onMockToggled,
        onAltUrlToggled = viewModel::onAltUrlToggled,
        onNavigateToInvoices = onNavigateToInvoices,
        onNavigateToSmartSolar = onNavigateToSmartSolar
    )
}

// =================================================================
// 2. SCREEN (Replicando ConstraintLayout)
// =================================================================

@Composable
fun MainScreen(
    uiState: MainUIState,
    onMockToggled: (Boolean) -> Unit,
    onAltUrlToggled: (Boolean) -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToSmartSolar: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. FONDO ABSTRACTO (Capa base, equivalente a ImageView con constraints)
        Image(
            painter = painterResource(id = R.drawable.bg_header_abstract),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp), // main_header_height
            contentScale = ContentScale.FillBounds // fitXY
        )

        // 2. TODO EL CONTENIDO (Column para apilar verticalmente)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ROW SUPERIOR: Switches + Avatar (alineados con el avatar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 46.dp, end = 30.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label "Servidor"
                Text(
                    text = stringResource(R.string.servidor),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                   )

                // Switch URL Alt
                Switch(
                    checked = uiState.useAltUrl,
                    onCheckedChange = onAltUrlToggled,
                    enabled = !uiState.useMock,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp).height(0.dp).scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.secondary,
                        checkedTrackColor = colorScheme.primaryContainer,
                        uncheckedThumbColor = colorScheme.outline,
                        uncheckedTrackColor = colorScheme.surfaceVariant,
                        disabledCheckedThumbColor = colorScheme.primary.copy(alpha = 0.4f),
                        disabledCheckedTrackColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        disabledUncheckedThumbColor = colorScheme.outline.copy(alpha = 0.5f),
                        disabledUncheckedTrackColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )

                // Label "RetroMock"
                Text(
                    text = stringResource(R.string.retromock),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, end = 6.dp)
                )

                // Switch Mock
                Switch(
                    checked = uiState.useMock,
                    onCheckedChange = onMockToggled,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp).scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.secondary,
                        checkedTrackColor = colorScheme.primaryContainer,
                        uncheckedThumbColor = colorScheme.outline,
                        uncheckedTrackColor = colorScheme.surfaceVariant,
                        disabledCheckedThumbColor = colorScheme.primary.copy(alpha = 0.4f),
                        disabledCheckedTrackColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        disabledUncheckedThumbColor = colorScheme.outline.copy(alpha = 0.5f),
                        disabledUncheckedTrackColor = colorScheme.surfaceVariant.copy(alpha = 0.6f)

                    )
                )

                // Avatar
                Card(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user_outline),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = colorResource(id = R.color.my_theme_green_light)
                        )
                    }
                }
            }

            // SALUDO (debajo del avatar con margin_top)
            Text(
                text = stringResource(R.string.greeting_user, uiState.userName),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )

            // DIRECCIÓN
            Text(
                text = uiState.userAddress,
                fontSize = 14.sp,
                color = colorScheme.onBackground,
                maxLines = 1,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp)
            )

            // TÍTULO SECCIÓN
            Text(
                text = stringResource(R.string.mi_energ_a),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 16.dp)
            )

            // TARJETAS DE NAVEGACIÓN
            // TARJETAS DE NAVEGACIÓN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // equivale al main_action_card_margin_start
            ) {
                // Tarjeta Facturas
                ActionCard(
                    title = stringResource(R.string.ver_nconsumo),
                    subtitle = stringResource(R.string.hist_rico),
                    icon = R.drawable.ic_lightbulb_outline,
                    onClick = onNavigateToInvoices,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.main_action_card_width))   // 160dp
                        .height(dimensionResource(R.dimen.main_action_card_height))  // 190dp
                )

                // Tarjeta SmartSolar
                ActionCard(
                    title = stringResource(R.string.smart_nsolar),
                    subtitle = stringResource(R.string.paneles),
                    icon = R.drawable.twotone_doc_24,
                    onClick = onNavigateToSmartSolar,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.main_action_card_width))   // 160dp
                        .height(dimensionResource(R.dimen.main_action_card_height))  // 190dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =================================================================
// 3. COMPONENTE TARJETA
// =================================================================

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colorResource(id = R.color.card_border)
        )

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color.Unspecified
            )

            Column {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    letterSpacing = (-0.02).sp
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.dark_gray),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// =================================================================
// 4. PREVIEW
// =================================================================
// =================================================================
// 4. PREVIEW
// =================================================================

@Preview(name = "Light Mode", showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    NexoSolarTheme {
        MainScreen(
            uiState = MainUIState(
                userName = "Francisco Pacheco",
                userAddress = "Av. de la Constitución, Sevilla"
            ),
            onMockToggled = {},
            onAltUrlToggled = {},
            onNavigateToInvoices = {},
            onNavigateToSmartSolar = {}
        )
    }
}

@Preview(name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun MainScreenDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        MainScreen(
            uiState = MainUIState(
                userName = "Francisco Pacheco",
                userAddress = "Av. de la Constitución, Sevilla"
            ),
            onMockToggled = {},
            onAltUrlToggled = {},
            onNavigateToInvoices = {},
            onNavigateToSmartSolar = {}
        )
    }
}
