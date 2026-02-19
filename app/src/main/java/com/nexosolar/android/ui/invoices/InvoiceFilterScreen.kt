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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
                            tint = Color.Gray,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
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
                color = Color.Black,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
            )

            // ========================================
            // SECCIÓN FECHAS ✅
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
            // SECCIÓN IMPORTE ✅ (ÚNICA)
            // ========================================
            FilterSectionTitle(stringResource(R.string.por_un_importe))
            Spacer(modifier = Modifier.height(16.dp))

            // Texto rango
            Text(
                text = String.format("%.0f € - %.0f €", safeMin, safeMax),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.my_theme_green_light),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Etiquetas
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "0 €", color = Color.Gray, fontSize = 14.sp)
                Text(text = String.format("%.0f €", actualMaxAmount), color = Color.Gray, fontSize = 14.sp)
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
                modifier = Modifier
                    .fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = colorResource(id = R.color.my_theme_green_light),
                    activeTrackColor = colorResource(id = R.color.my_theme_green_light),
                    inactiveTrackColor = Color.LightGray.copy(alpha = 0.5f)
                ),
                startThumb = {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = colorResource(id = R.color.my_theme_green_light),
                                shape = CircleShape
                            )
                    )
                },
                endThumb = {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                color = colorResource(id = R.color.my_theme_green_light),
                                shape = CircleShape
                            )
                    )
                },
                // ✅ Track personalizado con grosor controlado
                track = { rangeSliderState ->
                    SliderDefaults.Track(
                        rangeSliderState = rangeSliderState,
                        modifier = Modifier.height(2.dp), // ⬅️ AQUÍ controlas el grosor
                        colors = SliderDefaults.colors(
                            activeTrackColor = colorResource(id = R.color.my_theme_green_light),
                            inactiveTrackColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        drawStopIndicator = null // Sin marcadores en los extremos
                    )
                }
            )


            FilterDivider()

            // ========================================
            // SECCIÓN ESTADOS ✅
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
            // BOTONES ✅
            // ========================================
            Button(
                onClick = { onApplyFilters(currentFilters) },
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.my_theme_green_light)),
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text(text = stringResource(R.string.borrar_filtros), color = Color.Gray, fontSize = 16.sp)
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
        color = Color.Black,
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
        color = Color(0xFFEEEEEE) // Gris muy suave
    )
}
@Composable
private fun DateButtonColumn(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Valor por defecto para evitar errores
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Botón gris redondeado de ancho fijo (140dp)
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFEEEEEE),
            modifier = Modifier
                .width(140.dp)
                .height(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date?.let { DateUtils.formatDateShort(it) } ?: stringResource(R.string.dia_mes_ano),
                    color = Color.Black,
                    fontWeight = if (date != null) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}


/**
 * Fila de checkbox donde se puede pulsar en cualquier parte (texto o caja).
 * Usa los colores verdes definidos en el tema.
 */
@Composable
private fun FilterCheckboxRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp) // Altura cómoda para el dedo
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Sin efecto ripple si prefieres limpieza, o quita esta línea
            ) { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null, // Null porque el click lo maneja la Row
            colors = CheckboxDefaults.colors(
                checkedColor = colorResource(id = R.color.my_theme_green_light), // VERDE
                uncheckedColor = Color.LightGray,
                checkmarkColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = Color.Black, // Gris oscuro como en la foto
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
            }) { Text("OK", color = colorResource(R.color.my_theme_green_light)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// ==========================================
// PREVIEW
// ==========================================

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun InvoiceFilterScreenPreview() {
    // 1. Simulamos estadísticas reales (Min 0€ - Max 300€)
    val mockStatistics = InvoiceFilterUIState.FilterStatistics(
        maxAmount = 300f,
        oldestDateMillis = 1672531200000L, // 01/01/2023
        newestDateMillis = 1704067200000L  // 01/01/2024
    )

    // 2. Simulamos filtros aplicados (Rango 50-250€ y estado 'Pendiente' marcado)
    val mockFilters = InvoiceFilters(
        minAmount = 50f,
        maxAmount = 250f,
        filteredStates = setOf(InvoiceState.PENDING.serverValue)
    )

    // 3. Estado de UI completo
    val mockUIState = InvoiceFilterUIState(
        filters = mockFilters,
        statistics = mockStatistics,
        isApplying = false
    )

    // 4. Renderizamos la pantalla
    InvoiceFilterScreen(
        uiState = mockUIState,
        onApplyFilters = {},
        onClose = {}
    )
}

