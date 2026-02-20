package com.nexosolar.android.ui.invoices

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.nexosolar.android.domain.models.InvoiceState
import com.nexosolar.android.ui.invoices.components.InvoiceItem
import com.nexosolar.android.ui.invoices.components.InvoiceItemSkeleton
import com.nexosolar.android.ui.invoices.components.NotAvailableDialog
import com.nexosolar.android.ui.invoices.filter.InvoiceFilterScreen
import com.nexosolar.android.ui.smartsolar.components.ErrorView
import com.nexosolar.android.ui.invoices.models.InvoiceListItemUi

// =================================================================
// 1. ROUTE
// =================================================================

@Composable
fun InvoiceRoute(
    viewModel: InvoiceViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

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
// 2. SCREEN (sin estado propio — solo estructura)
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
                        IconButton(
                            onClick = onFilterClick,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
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

            // ✅ Todo el estado local vive aquí, no contamina el Scaffold
            InvoiceContent(
                uiState = uiState,
                onRefresh = onRefresh,
                onRetry = onRetry
            )
        }
    }
}

// =================================================================
// 3. CONTENT (estado local al nivel mínimo necesario)
// =================================================================

@Composable
private fun InvoiceContent(
    uiState: InvoiceUIState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit
) {
    // ✅ Estado local aquí: solo recompone InvoiceContent, no el Scaffold entero
    var showNotAvailableDialog by remember { mutableStateOf(false) }

    // ✅ Lambda estable: misma referencia entre recomposiciones
    val onItemClick = remember { { showNotAvailableDialog = true } }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {

            is InvoiceUIState.Loading -> {
                InvoiceListSkeleton()
            }

            is InvoiceUIState.Error -> {
                ErrorView(
                    message = stringResource(id = uiState.messageRes),
                    errorType = uiState.type,
                    onRetry = onRetry
                )
            }

            is InvoiceUIState.Empty -> {
                InvoicePullToRefresh(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh
                ) {
                    EmptyStateView()
                }
            }

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
                            key = { it.invoiceId }
                        ) { invoice ->
                            InvoiceItem(
                                invoice = invoice,
                                onClick = onItemClick  // ✅ referencia estable
                            )
                        }
                    }
                }
            }
        }

        // ✅ Diálogo dentro del Box, al nivel del contenido, no del Scaffold
        if (showNotAvailableDialog) {
            NotAvailableDialog(
                onDismiss = { showNotAvailableDialog = false }
            )
        }
    }
}

// =================================================================
// 4. COMPONENTES AUXILIARES
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoicePullToRefresh(
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
        content()
    }
}

@Composable
private fun EmptyStateView() {
    // ✅ Box para centrar, Column solo para apilar los elementos internos
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_empty_list),
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(id = R.dimen.empty_state_img_size))
            )
            Text(
                text = stringResource(R.string.sin_resultados),
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.text_size_title).value.sp,
                color = Color.Gray
            )
            Text(
                text = stringResource(R.string.change_filters),
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun InvoiceListSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(count = 12, key = { "skeleton-$it" }) {
            InvoiceItemSkeleton()
        }
    }
}

// =================================================================
// 5. PREVIEWS
// =================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InvoiceScreenSuccessPreview() {
    InvoiceScreen(
        uiState = InvoiceUIState.Success(
            invoices = listOf(
                InvoiceListItemUi(invoiceId = 1, state = InvoiceState.PENDING, formattedDate = "31 Ago 2020", formattedAmount = "54.56 €"),
                InvoiceListItemUi(invoiceId = 2, state = InvoiceState.PAID, formattedDate = "31 Jul 2020", formattedAmount = "67.54 €"),
                InvoiceListItemUi(invoiceId = 3, state = InvoiceState.CANCELLED, formattedDate = "22 Jun 2020", formattedAmount = "56.38 €"),
                InvoiceListItemUi(invoiceId = 4, state = InvoiceState.FIXED_FEE, formattedDate = "12 May 2024", formattedAmount = "150.75 €"),
                InvoiceListItemUi(invoiceId = 5, state = InvoiceState.PAYMENT_PLAN, formattedDate = "08 Jun 2024", formattedAmount = "99.99 €"),
            )
        ),
        onRefresh = {},
        onRetry = {},
        onBackClick = {},
        onFilterClick = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InvoiceScreenEmptyPreview() {
    InvoiceScreen(
        uiState = InvoiceUIState.Empty(),
        onRefresh = {},
        onRetry = {},
        onBackClick = {},
        onFilterClick = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InvoiceScreenLoadingPreview() {
    InvoiceScreen(
        uiState = InvoiceUIState.Loading,
        onRefresh = {},
        onRetry = {},
        onBackClick = {},
        onFilterClick = {}
    )
}
