package com.nexosolar.android.ui.smartsolar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexosolar.android.R
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.ui.theme.NexoSolarTheme
import kotlinx.coroutines.launch

// =================================================================
// 1. ROUTE (Hilt Entry Point)
// =================================================================

/**
 * Contenedor principal para la navegación de SmartSolar.
 * Responsable de instanciar las rutas que requieren inyección de dependencias (Hilt).
 */
@Composable
fun SmartSolarRoute(
    onBackClick: () -> Unit
) {
    SmartSolarScreen(
        onBackClick = onBackClick,
        installationContent = { InstallationRoute() },
        energyContent = { EnergyRoute() },
        detailsContent = { DetailsRoute() }
    )
}

// =================================================================
// 2. SCREEN (Pure UI)
// =================================================================

/**
 * UI declarativa de Smart Solar.
 * Utiliza "Slot API" (lambdas para contenido) para desacoplar la vista de la lógica de inyección,
 * permitiendo previsualizaciones y tests aislados.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartSolarScreen(
    onBackClick: () -> Unit,
    installationContent: @Composable () -> Unit,
    energyContent: @Composable () -> Unit,
    detailsContent: @Composable () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val tabTitles = listOf(
        "Mi instalación",
        "Energía",
        "Detalles"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                // Botón Atrás
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.smart_solar_back_margin_start),
                            top = dimensionResource(id = R.dimen.smart_solar_back_margin_top)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back_24),
                        contentDescription = null,
                        tint = colorResource(id = R.color.smart_solar_accent_green),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.atr_s),
                        color = colorResource(id = R.color.smart_solar_accent_green),
                        fontSize = dimensionResource(id = R.dimen.smart_solar_back_text_size).value.sp,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal)
                    )
                }

                // Título
                Text(
                    text = stringResource(id = R.string.smart_solar),
                    color = Color.Black,
                    fontSize = dimensionResource(id = R.dimen.smart_solar_title_text_size).value.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        top = dimensionResource(id = R.dimen.smart_solar_title_margin_top),
                        start = dimensionResource(id = R.dimen.smart_solar_title_margin_horizontal),
                        end = dimensionResource(id = R.dimen.smart_solar_title_margin_horizontal),
                        bottom = 16.dp
                    )
                )

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = dimensionResource(id = R.dimen.smart_solar_tabs_margin_horizontal),
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    divider = {}, // Elimina el divisor por defecto de Material
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            val currentTab = tabPositions[pagerState.currentPage]

                            // Indicador personalizado:
                            // Se aplica padding horizontal para que el indicador visualmente coincida
                            // con el ancho del texto y no con el ancho total de la celda.
                            Box(
                                Modifier
                                    .tabIndicatorOffset(currentTab)
                                    .padding(horizontal = 12.dp)
                                    .height(dimensionResource(id = R.dimen.smart_solar_tab_indicator_height))
                                    .background(
                                        color = Color.Black,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            )
                        }
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val selected = pagerState.currentPage == index

                        Tab(
                            selected = selected,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                            // Sobrescribe el minWidth por defecto de Material (90dp) para ajustar al contenido
                            modifier = Modifier.widthIn(min = 1.dp),
                            text = {
                                Text(
                                    text = title,
                                    color = if (selected) Color.Black else colorResource(id = R.color.dark_gray),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) { page ->
            when (page) {
                0 -> installationContent()
                1 -> energyContent()
                2 -> detailsContent()
            }
        }
    }
}

// =================================================================
// 3. PREVIEW
// =================================================================

@Preview(showBackground = true)
@Composable
fun SmartSolarScreenPreview() {
    NexoSolarTheme {
        val mockInstallation = Installation(
            selfConsumptionCode = "ES0021000000000001JN",
            installationStatus = "Activa",
            installationType = "Residencial",
            compensation = "Con compensación",
            power = "5.5 kW"
        )

        val mockState = InstallationUIState.Success(
            installation = mockInstallation,
            isRefreshing = false
        )

        // Se inyectan componentes 'Screen' puros con datos mockeados
        // para evitar errores de inyección de ViewModels en tiempo de diseño.
        SmartSolarScreen(
            onBackClick = {},
            installationContent = {
                InstallationScreen(uiState = mockState, onRefresh = {})
            },
            energyContent = {
                EnergyScreen()
            },
            detailsContent = {
                DetailsScreen(uiState = mockState, onRefresh = {})
            }
        )
    }
}
