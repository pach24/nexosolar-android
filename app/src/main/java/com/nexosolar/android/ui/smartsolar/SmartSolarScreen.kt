package com.nexosolar.android.ui.smartsolar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nexosolar.android.R
import com.nexosolar.android.domain.models.Installation
import com.nexosolar.android.ui.smartsolar.details.DetailsRoute
import com.nexosolar.android.ui.smartsolar.details.DetailsScreen
import com.nexosolar.android.ui.smartsolar.energy.EnergyRoute
import com.nexosolar.android.ui.smartsolar.energy.EnergyScreen
import com.nexosolar.android.ui.smartsolar.installation.InstallationRoute
import com.nexosolar.android.ui.smartsolar.installation.InstallationScreen
import com.nexosolar.android.ui.smartsolar.models.InstallationUIState
import com.nexosolar.android.ui.theme.NexoSolarTheme
import kotlinx.coroutines.launch

@Composable
fun SmartSolarRoute(onBackClick: () -> Unit) {
    SmartSolarScreen(
        onBackClick = onBackClick,
        installationContent = { InstallationRoute() },
        energyContent = { EnergyRoute() },
        detailsContent = { DetailsRoute() }
    )
}

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
    // Idealmente mover estos literales a strings.xml
    val titles = listOf("Mi instalación", "Energía", "Detalles")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
            ) {
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.atr_s),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Text(
                    text = stringResource(id = R.string.smart_solar),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    divider = {},
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.onSurface,
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    titles.forEachIndexed { index, title ->
                        val isSelected = pagerState.currentPage == index
                        Tab(
                            selected = isSelected,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                Text(
                                    text = title,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                .background(MaterialTheme.colorScheme.background)
        ) { page ->
            when (page) {
                0 -> installationContent()
                1 -> energyContent()
                2 -> detailsContent()
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun SmartSolarScreenPreview() {
    NexoSolarTheme {
        val mockInstallation = Installation(
            selfConsumptionCode = "ES0021000000000001JN",
            installationStatus = "Activa",
            installationType = "Residencial",
            compensation = "Con compensación",
            power = "5.5 kW"
        )
        val mockState = InstallationUIState.Success(mockInstallation, false)
        SmartSolarScreen(
            onBackClick = {},
            installationContent = { InstallationScreen(uiState = mockState, onRefresh = {}) },
            energyContent = { EnergyScreen() },
            detailsContent = { DetailsScreen(uiState = mockState, onRefresh = {}) }
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SmartSolarScreenDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        val mockInstallation = Installation(
            selfConsumptionCode = "ES0021000000000001JN",
            installationStatus = "Activa",
            installationType = "Residencial",
            compensation = "Con compensación",
            power = "5.5 kW"
        )
        val mockState = InstallationUIState.Success(mockInstallation, false)
        SmartSolarScreen(
            onBackClick = {},
            installationContent = { InstallationScreen(uiState = mockState, onRefresh = {}) },
            energyContent = { EnergyScreen() },
            detailsContent = { DetailsScreen(uiState = mockState, onRefresh = {}) }
        )
    }
}
