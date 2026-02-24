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
import com.nexosolar.android.ui.theme.NexoSolarTheme

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
    var showNotAvailableDialog by remember { mutableStateOf(false) }
    var isNavigatingBack by remember { mutableStateOf(false) }

    val handleBackClick = remember(onBackClick, isNavigatingBack) {
        {
            if (!isNavigatingBack) {
                isNavigatingBack = true
                showFilters = false
                showNotAvailableDialog = false
                onBackClick()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            showFilters = false
            showNotAvailableDialog = false
            isNavigatingBack = false
        }
    }

    BackHandler(enabled = true) {
        if (showFilters) {
            showFilters = false
        } else {
            handleBackClick()
        }
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
            onBackClick = handleBackClick,
            onFilterClick = { if (!isNavigatingBack) showFilters = true },
            isNavigatingBack = isNavigatingBack,
            showNotAvailableDialog = showNotAvailableDialog,
            onInvoiceClick = { if (!isNavigatingBack) showNotAvailableDialog = true },
            onDialogDismiss = { showNotAvailableDialog = false }
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
    onFilterClick: () -> Unit,
    isNavigatingBack: Boolean,
    showNotAvailableDialog: Boolean,
    onInvoiceClick: () -> Unit,
    onDialogDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Spacer(modifier = Modifier) },
                navigationIcon = {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable(enabled = !isNavigatingBack) { onBackClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.back_button),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.back_button),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    if (uiState is InvoiceUIState.Success || uiState is InvoiceUIState.Empty) {
                        IconButton(
                            onClick = onFilterClick,
                            enabled = !isNavigatingBack,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.filtericon_3x),
                                contentDescription = stringResource(R.string.filtrar_facturas),
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor =  MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = stringResource(R.string.facturas),
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.text_size_hero_title).value.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.Start)
            )

            // ✅ Todo el estado local vive aquí, no contamina el Scaffold
            InvoiceContent(
                uiState = uiState,
                onRefresh = onRefresh,
                onRetry = onRetry,
                isNavigatingBack = isNavigatingBack,
                showNotAvailableDialog = showNotAvailableDialog,
                onInvoiceClick = onInvoiceClick,
                onDialogDismiss = onDialogDismiss
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
    onRetry: () -> Unit,
    isNavigatingBack: Boolean,
    showNotAvailableDialog: Boolean,
    onInvoiceClick: () -> Unit,
    onDialogDismiss: () -> Unit
) {
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
                                onClick = { if (!isNavigatingBack) onInvoiceClick() }
                            )
                        }
                    }
                }
            }
        }

        // ✅ Diálogo dentro del Box, al nivel del contenido, no del Scaffold
        if (showNotAvailableDialog) {
            NotAvailableDialog(
                onDismiss = onDialogDismiss
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
        modifier = Modifier.fillMaxSize().padding(bottom = 150.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
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

@Composable
@Preview(showBackground = true, showSystemUi = true, name = "Success Light")
private fun InvoiceScreenSuccessPreview() {
    NexoSolarTheme {
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
            onFilterClick = {},
            isNavigatingBack = false,
            showNotAvailableDialog = false,
            onInvoiceClick = {},
            onDialogDismiss = {}
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true, name = "Success Dark")
private fun InvoiceScreenSuccessDarkPreview() {
    NexoSolarTheme(darkTheme = true) { // ← darkTheme = true
        InvoiceScreen(
            uiState = InvoiceUIState.Success(
                invoices = listOf(
                    InvoiceListItemUi(id = 1, dateText = "31 ago 2020", amountText = "54,56 €", state = com.nexosolar.android.domain.models.InvoiceState.PENDING),
                    InvoiceListItemUi(id = 2, dateText = "31 jul 2020", amountText = "67,54 €", state = com.nexosolar.android.domain.models.InvoiceState.PAID),
                )
            ),
            onRefresh = {},
            onRetry = {},
            onBackClick = {},
            onFilterClick = {},
            isNavigatingBack = false,
            showNotAvailableDialog = false,
            onInvoiceClick = {},
            onDialogDismiss = {}
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true, name = "Empty Light")
private fun InvoiceScreenEmptyPreview() {
    NexoSolarTheme {
        InvoiceScreen(
            uiState = InvoiceUIState.Empty(),
            onRefresh = {},
            onRetry = {},
            onBackClick = {},
            onFilterClick = {},
            isNavigatingBack = false,
            showNotAvailableDialog = false,
            onInvoiceClick = {},
            onDialogDismiss = {}
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true, name = "Empty Dark")
private fun InvoiceScreenEmptyDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        InvoiceScreen(
            uiState = InvoiceUIState.Empty(),
            onRefresh = {},
            onRetry = {},
            onBackClick = {},
            onFilterClick = {},
            isNavigatingBack = false,
            showNotAvailableDialog = false,
            onInvoiceClick = {},
            onDialogDismiss = {}
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true, name = "Loading Light")
private fun InvoiceScreenLoadingPreview() {
    NexoSolarTheme {
        InvoiceScreen(
            uiState = InvoiceUIState.Loading,
            onRefresh = {},
            onRetry = {},
            onBackClick = {},
            onFilterClick = {},
            isNavigatingBack = false,
            showNotAvailableDialog = false,
            onInvoiceClick = {},
            onDialogDismiss = {}
        )
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true, name = "Loading Dark")
private fun InvoiceScreenLoadingDarkPreview() {
    NexoSolarTheme(darkTheme = true) {
        InvoiceScreen(
            uiState = InvoiceUIState.Loading,
            onRefresh = {},
            onRetry = {},
            onBackClick = {},
            onFilterClick = {},
            isNavigatingBack = false,
            showNotAvailableDialog = false,
            onInvoiceClick = {},
            onDialogDismiss = {}
        )
    }
}
