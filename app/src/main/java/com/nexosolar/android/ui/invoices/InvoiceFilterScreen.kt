package com.nexosolar.android.ui.invoices.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexosolar.android.R
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.InvoiceState
import com.nexosolar.android.ui.invoices.InvoiceFilterUIState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import com.nexosolar.android.ui.theme.NexoDarkColorScheme
import com.nexosolar.android.ui.theme.NexoLightColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceFilterScreen(
    uiState: InvoiceFilterUIState,
    onApplyFilters: (InvoiceFilters) -> Unit,
    onClose: () -> Unit
) {
    var currentFilters by remember { mutableStateOf(uiState.filters) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val actualMaxAmount = uiState.statistics.maxAmount.coerceAtLeast(1f)
    val safeMin = (currentFilters.minAmount ?: 0f).coerceIn(0f, actualMaxAmount)
    val safeMax = (currentFilters.maxAmount ?: actualMaxAmount).coerceIn(safeMin, actualMaxAmount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onClose, modifier = Modifier.padding(top = 22.dp, end = 12.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.close_icon),
                            contentDescription = stringResource(R.string.cerrar),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // TÍTULO
            Text(
                text = stringResource(R.string.filtrar_facturas),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 22.dp)
            )

            // ========================================
            // SECCIÓN FECHAS
            // ========================================
            FilterSectionTitle(stringResource(R.string.con_fecha_de_emision))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                DateButtonColumn(
                    label = stringResource(R.string.desde),
                    date = currentFilters.startDate,
                    onClick = { showStartDatePicker = true }
                )
                Spacer(modifier = Modifier.width(14.dp))
                DateButtonColumn(
                    label = stringResource(R.string.hasta),
                    date = currentFilters.endDate,
                    onClick = { showEndDatePicker = true }
                )
            }
            FilterDivider()

            // ========================================
            // SECCIÓN IMPORTE
            // ========================================
            FilterSectionTitle(stringResource(R.string.por_un_importe))
            Spacer(modifier = Modifier.height(16.dp))

            // Texto rango
            Text(
                text = String.format("%.0f € - %.0f €", safeMin, safeMax),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Etiquetas
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "0 €", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(text = String.format("%.0f €", actualMaxAmount), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }

            // RangeSlider
            RangeSlider(
                value = safeMin..safeMax,
                onValueChange = { range ->
                    currentFilters = currentFilters.copy(
                        minAmount = range.start,
                        maxAmount = range.endInclusive
                    )
                },
                valueRange = 0f..actualMaxAmount,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline
                ),
                startThumb = {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                },
                endThumb = {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                },
                track = { rangeSliderState ->
                    SliderDefaults.Track(
                        rangeSliderState = rangeSliderState,
                        modifier = Modifier.height(2.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline
                        ),
                        drawStopIndicator = null
                    )
                }
            )

            FilterDivider()

            // ========================================
            // SECCIÓN ESTADOS
            // ========================================
            FilterSectionTitle(stringResource(R.string.por_estado))
            Spacer(modifier = Modifier.height(8.dp))

            val states = listOf(
                InvoiceState.PAID to R.string.pagadas,
                InvoiceState.PENDING to R.string.pendientes_de_pago,
                InvoiceState.CANCELLED to R.string.anuladas,
                InvoiceState.FIXED_FEE to R.string.cuota_fija,
                InvoiceState.PAYMENT_PLAN to R.string.plan_de_pago
            )

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                states.forEach { (state, labelRes) ->
                    val isChecked = currentFilters.filteredStates.contains(state.serverValue)
                    FilterCheckboxRow(
                        label = stringResource(labelRes),
                        isChecked = isChecked,
                        onCheckedChange = { checked ->
                            val newStates = currentFilters.filteredStates.toMutableSet()
                            if (checked) newStates.add(state.serverValue) else newStates.remove(state.serverValue)
                            currentFilters = currentFilters.copy(filteredStates = newStates)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ========================================
            // BOTONES
            // ========================================
            Button(
                onClick = { onApplyFilters(currentFilters) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // <-- Tema M3
                    contentColor = MaterialTheme.colorScheme.onPrimary  // <-- Tema M3
                ),
                enabled = !uiState.isApplying
            ) {
                Text(text = stringResource(R.string.aplicar_filtros), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    currentFilters = InvoiceFilters(
                        minAmount = 0f,
                        maxAmount = actualMaxAmount,
                        startDate = null,
                        endDate = null,
                        filteredStates = emptySet()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.borrar_filtros),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // <-- Tema M3
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // DIÁLOGOS DE FECHA
    if (showStartDatePicker) {
        SafeDatePickerDialog(
            initialDate = currentFilters.startDate,
            statistics = uiState.statistics,
            otherDate = currentFilters.endDate,
            isStartDate = true,
            onDateSelected = { date ->
                currentFilters = currentFilters.copy(startDate = date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        SafeDatePickerDialog(
            initialDate = currentFilters.endDate,
            statistics = uiState.statistics,
            otherDate = currentFilters.startDate,
            isStartDate = false,
            onDateSelected = { date ->
                currentFilters = currentFilters.copy(endDate = date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

// ==========================================
// COMPONENTES AUXILIARES ESTILIZADOS
// ==========================================

@Composable
private fun FilterSectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground, // <-- Tema M3
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FilterDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 32.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant // <-- Reemplaza el Color(0xFFEEEEEE) estático
    )
}

@Composable
private fun DateButtonColumn(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface, // <-- Tema M3 (Gris claro/oscuro)
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface, // <-- Reemplaza Color(0xFFEEEEEE)
            modifier = Modifier
                .width(140.dp)
                .height(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date?.let { DateUtils.formatDateShort(it) } ?: stringResource(R.string.dia_mes_ano),
                    color = MaterialTheme.colorScheme.onSurface, // <-- Reemplaza Color.Black
                    fontWeight = if (date != null) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun FilterCheckboxRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,       // <-- Tema M3
                uncheckedColor = MaterialTheme.colorScheme.outline,     // <-- Reemplaza Color.LightGray
                checkmarkColor = MaterialTheme.colorScheme.onPrimary    // <-- Color del tick dependiente del fondo
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface, // <-- Reemplaza Color.Black
            fontSize = 16.sp
        )
    }
}

// ==========================================
// DATE PICKER SEGURO
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafeDatePickerDialog(
    initialDate: LocalDate?,
    statistics: InvoiceFilterUIState.FilterStatistics,
    otherDate: LocalDate?,
    isStartDate: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val minDateAllowed = statistics.oldestDateMillis
    val maxDateAllowed = statistics.newestDateMillis

    val validationMin = if (!isStartDate && otherDate != null) {
        DateUtils.toEpochMilli(otherDate).coerceAtLeast(minDateAllowed)
    } else minDateAllowed

    val validationMax = if (isStartDate && otherDate != null) {
        DateUtils.toEpochMilli(otherDate).coerceAtMost(maxDateAllowed)
    } else maxDateAllowed

    val rawSelection = initialDate?.let { DateUtils.toEpochMilli(it) }
    val safeSelection = if (rawSelection != null && rawSelection in minDateAllowed..maxDateAllowed) {
        rawSelection
    } else {
        if (isStartDate) minDateAllowed else maxDateAllowed
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = safeSelection,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis in validationMin..validationMax
            }
            override fun isSelectableYear(year: Int): Boolean {
                val yearStart = LocalDate.of(year, 1, 1)
                val yearEnd = LocalDate.of(year, 12, 31)
                val yearStartMillis = DateUtils.toEpochMilli(yearStart)
                val yearEndMillis = DateUtils.toEpochMilli(yearEnd)
                return !(yearEndMillis < validationMin || yearStartMillis > validationMax)
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    if (millis in validationMin..validationMax) {
                        onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            }) { Text("OK", color = MaterialTheme.colorScheme.primary) } // <-- Tema M3
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) // <-- Tema M3
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}



// ==========================================
// PREVIEWS MODO CLARO Y MODO OSCURO
// ==========================================

@Preview(
    name = "Light Mode",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun InvoiceFilterScreenLightPreview() {
    MaterialTheme(colorScheme = NexoLightColorScheme) {
        InvoiceFilterScreen(
            uiState = mockUIState(),
            onApplyFilters = {},
            onClose = {}
        )
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InvoiceFilterScreenDarkPreview() {
    MaterialTheme(colorScheme = NexoDarkColorScheme) {
        InvoiceFilterScreen(
            uiState = mockUIState(),
            onApplyFilters = {},
            onClose = {}
        )
    }
}

// Función auxiliar privada para no repetir el mock
private fun mockUIState() = InvoiceFilterUIState(
    filters = InvoiceFilters(
        minAmount = 50f,
        maxAmount = 250f,
        filteredStates = setOf(InvoiceState.PENDING.serverValue)
    ),
    statistics = InvoiceFilterUIState.FilterStatistics(
        maxAmount = 300f,
        oldestDateMillis = 1672531200000L,
        newestDateMillis = 1704067200000L
    ),
    isApplying = false
)
