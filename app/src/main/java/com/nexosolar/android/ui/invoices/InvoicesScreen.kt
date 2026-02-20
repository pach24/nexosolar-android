package com.nexosolar.android.ui.invoices

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
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
import com.nexosolar.android.ui.invoices.components.InvoiceItem
import com.nexosolar.android.ui.invoices.components.InvoiceItemSkeleton
import com.nexosolar.android.ui.invoices.components.NotAvailableDialog
import com.nexosolar.android.ui.invoices.filter.InvoiceFilterScreen
import com.nexosolar.android.ui.smartsolar.components.ErrorView

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

    BackHandler(enabled = showFilters) {
        showFilters = false
    }

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
                            key = { it.id }
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


@Composable
private fun InvoicePullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState() // ← hoisted aquí

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,          // ← misma referencia
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary
            )
        }
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
    val shimmerTransition = rememberInfiniteTransition(label = "InvoiceSkeletonShimmer")
    val shimmerTranslate by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "InvoiceSkeletonTranslate"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(count = 8, key = { "skeleton-$it" }) {
            InvoiceItemSkeleton(shimmerTranslate = shimmerTranslate)
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
                InvoiceListItemUi(id = 1, dateText = "31 ago 2020", amountText = "54,56 €", state = com.nexosolar.android.domain.models.InvoiceState.PENDING),
                InvoiceListItemUi(id = 2, dateText = "31 jul 2020", amountText = "67,54 €", state = com.nexosolar.android.domain.models.InvoiceState.PAID),
                InvoiceListItemUi(id = 3, dateText = "22 jun 2020", amountText = "56,38 €", state = com.nexosolar.android.domain.models.InvoiceState.CANCELLED),
                InvoiceListItemUi(id = 4, dateText = "12 may 2024", amountText = "150,75 €", state = com.nexosolar.android.domain.models.InvoiceState.FIXED_FEE),
                InvoiceListItemUi(id = 5, dateText = "8 jun 2024", amountText = "99,99 €", state = com.nexosolar.android.domain.models.InvoiceState.PAYMENT_PLAN),
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
