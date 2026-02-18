package com.nexosolar.android.ui.invoices




import com.nexosolar.android.ui.invoices.components.InvoiceItemSkeleton

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexosolar.android.R
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.ui.invoices.components.InvoiceItem
import com.nexosolar.android.ui.invoices.components.NotAvailableDialog
import com.nexosolar.android.ui.invoices.filter.InvoiceFilterScreen
import com.nexosolar.android.ui.smartsolar.components.ErrorView
import java.time.LocalDate


// =================================================================
// 1. ROUTE (Punto de entrada para Navegación/Activity)
// =================================================================
@Composable
fun InvoiceRoute(
    viewModel: InvoiceViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // 1. Recogemos el State directamente (sin 'by')
    val uiStateState = viewModel.uiState.collectAsStateWithLifecycle()
    val filterStateState = viewModel.filterState.collectAsStateWithLifecycle()

    // 2. Extraemos el valor manualmente
    val uiState = uiStateState.value
    val filterState = filterStateState.value

    // Estado local para controlar si mostramos la pantalla de filtros
    var showFilters by remember { mutableStateOf(false) }

    if (showFilters) {
        InvoiceFilterScreen(
            uiState = filterState,
            onApplyFilters = { filters ->
                viewModel.updateFilters(filters)
                viewModel.applyFilters()
                showFilters = false
            },
            onClose = { showFilters = false }
        )
    } else {
        InvoiceScreen(
            uiState = uiState,
            onRefresh = viewModel::onSwipeRefresh,
            onRetry = viewModel::onSwipeRefresh,
            onBackClick = onBackClick,
            onFilterClick = { showFilters = true }
        )
    }
}


// =================================================================
// 2. SCREEN (UI Pura)
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    uiState: InvoiceUIState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    var showNotAvailableDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Spacer(modifier = Modifier) },
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { onBackClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.back_button),
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(id = R.color.my_theme_green_dark)
                        )
                        Text(
                            text = stringResource(R.string.back_button),
                            color = colorResource(id = R.color.my_theme_green_dark),
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    if (uiState is InvoiceUIState.Success || uiState is InvoiceUIState.Empty) {
                        IconButton(onClick = onFilterClick,
                        modifier = Modifier.padding(end = 12.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.filtericon_3x),
                                contentDescription = stringResource(R.string.filtrar_facturas),
                                modifier = Modifier.size(32.dp),
                                tint = Color.Black
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = stringResource(R.string.facturas),
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.text_size_hero_title).value.sp,
                color = Color.Black,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.Start)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    // --- LOADING ---
                    is InvoiceUIState.Loading -> {
                        InvoiceListSkeleton()
                    }

                    // --- ERROR ---
                    is InvoiceUIState.Error -> {
                        ErrorView(
                            message = stringResource(id = uiState.messageRes),
                            errorType = uiState.type,
                            onRetry = onRetry
                        )
                    }

                    // --- EMPTY ---
                    is InvoiceUIState.Empty -> {
                        InvoicePullToRefresh(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = onRefresh
                        ) {
                            EmptyStateView()
                        }
                    }

                    // --- SUCCESS ---
                    is InvoiceUIState.Success -> {
                        InvoicePullToRefresh(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = onRefresh
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(
                                    items = uiState.invoices,
                                    key = { it.invoiceID }
                                ) { invoice ->
                                    InvoiceItem(
                                        invoice = invoice,
                                        onClick = { showNotAvailableDialog = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // ✅ Diálogo condicional
            if (showNotAvailableDialog) {
                NotAvailableDialog(
                    onDismiss = { showNotAvailableDialog = false }
                )
            }
        }
    }
}

// =================================================================
// 3. COMPONENTES AUXILIARES
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoicePullToRefresh(
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

@Composable
private fun EmptyStateView() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_empty_list),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(id = R.dimen.empty_state_img_size))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.sin_resultados),
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(id = R.dimen.text_size_title).value.sp,
            color = Color.Gray
        )
        Text(
            text = stringResource(R.string.change_filters),
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
@Composable
private fun InvoiceListSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(
            count = 12,
            key = { "skeleton-$it" }
        ) {
            InvoiceItemSkeleton()  // ✅ LLAMAR AQUÍ
        }
    }
}


// =================================================================
// PREVIEWS
// =================================================================
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InvoiceScreenPreview() {
    InvoiceScreen(
        uiState = InvoiceUIState.Success(
            invoices = listOf(
                Invoice(
                    invoiceID = 1,
                    invoiceAmount = 54.56f,
                    invoiceDate = LocalDate.of(2020, 8, 31),
                    invoiceStatus = "Pendiente de pago"
                ),
                Invoice(
                    invoiceID = 2,
                    invoiceAmount = 67.54f,
                    invoiceDate = LocalDate.of(2020, 7, 31),
                    invoiceStatus = "Pendiente de pago"
                ),
                Invoice(
                    invoiceID = 3,
                    invoiceAmount = 56.38f,
                    invoiceDate = LocalDate.of(2020, 6, 22),
                    invoiceStatus = "Pendiente de pago"
                ),
                Invoice(
                    invoiceID = 4,
                    invoiceAmount = 57.38f,
                    invoiceDate = LocalDate.of(2020, 5, 31),
                    invoiceStatus = "Pagada"
                ),
                Invoice(
                    invoiceID = 5,
                    invoiceAmount = 150.75f,
                    invoiceDate = LocalDate.of(2024, 5, 12),
                    invoiceStatus = "Cuota fija"
                ),
                Invoice(
                    invoiceID = 6,
                    invoiceAmount = 99.99f,
                    invoiceDate = LocalDate.of(2024, 6, 8),
                    invoiceStatus = "Plan de pago"
                ),
                Invoice(
                    invoiceID = 7,
                    invoiceAmount = 175.25f,
                    invoiceDate = LocalDate.of(2024, 7, 15),
                    invoiceStatus = "Pagada"
                ),
                Invoice(
                    invoiceID = 8,
                    invoiceAmount = 62.40f,
                    invoiceDate = LocalDate.of(2024, 8, 3),
                    invoiceStatus = "Pendiente de pago"
                ),
                Invoice(
                    invoiceID = 9,
                    invoiceAmount = 300.00f,
                    invoiceDate = LocalDate.of(2024, 9, 22),
                    invoiceStatus = "Pagada"
                ),
                Invoice(
                    invoiceID = 10,
                    invoiceAmount = 110.80f,
                    invoiceDate = LocalDate.of(2024, 10, 18),
                    invoiceStatus = "Pendiente de pago"
                ),
                Invoice(
                    invoiceID = 11,
                    invoiceAmount = 95.50f,
                    invoiceDate = LocalDate.of(2024, 11, 7),
                    invoiceStatus = "Pagada"
                ),
                Invoice(
                    invoiceID = 12,
                    invoiceAmount = 220.00f,
                    invoiceDate = LocalDate.of(2024, 12, 30),
                    invoiceStatus = "Anulada"
                )
            )
        ),
        onRefresh = {},
        onRetry = {},
        onBackClick = {},
        onFilterClick = {}
    )
}