package com.nexosolar.android.ui.invoices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.nexosolar.android.R
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.databinding.FragmentFilterBinding
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.InvoiceState
import com.nexosolar.android.ui.common.RangeValidator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

/**
 * Fragment para configurar filtros de facturas.
 * MIGRADO A FLOW: Usa StateFlow y repeatOnLifecycle.
 */
@AndroidEntryPoint
class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    // Referencia al ViewModel compartido
    private val viewModel: InvoiceViewModel by activityViewModels()

    /**
     * Estado temporal de filtros editados por el usuario.
     * Solo afecta al ViewModel al presionar "Aplicar".
     */
    private var tempFilters: InvoiceFilters? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    // ========== OBSERVERS (MIGRADO A FLOW) ==========

    private fun setupObservers() {
        // ✅ CAMBIO CLAVE: Usar lifecycleScope + repeatOnLifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observamos el estado de filtros
                viewModel.filterState.collect { uiState ->
                    // Guardamos referencia inicial si es null
                    if (tempFilters == null) {
                        tempFilters = uiState.filters
                    }
                    renderUI(uiState)
                }
            }
        }
    }

    // ========== LISTENERS ==========

    private fun setupListeners() {
        with(binding) {
            btnSelectDate.setOnClickListener { showDatePicker(isStartDate = true) }
            btnSelectDateUntil.setOnClickListener { showDatePicker(isStartDate = false) }

            rangeSlider.addOnChangeListener { slider, _, _ ->
                handleSliderChange(slider.values)
            }

            setupStateCheckboxes()

            btnAplicar.setOnClickListener { applyFilters() }
            btnBorrar.setOnClickListener {
                // 1. Obtenemos el valor máximo real del ViewModel para reiniciar el slider correctamente
                val currentStats = viewModel.filterState.value.statistics

                // 2. Creamos unos filtros "limpios" (sin fechas, sin estados, rango completo)
                val cleanFilters = InvoiceFilters(
                    minAmount = 0f,
                    maxAmount = currentStats.maxAmount, // Importante: volver al tope real
                    startDate = null,
                    endDate = null,
                    filteredStates = emptySet()
                )

                // 3. Actualizamos la variable temporal del fragmento
                tempFilters = cleanFilters

                // 4. Forzamos el repintado de la pantalla con estos datos limpios
                // Truco: Creamos un estado "ficticio" temporal solo para que renderUI sepa qué pintar
                val dummyState = viewModel.filterState.value.copy(filters = cleanFilters)
                renderUI(dummyState)
            }
            btnCerrar.setOnClickListener { closeFragment() }
        }
    }

    private fun setupStateCheckboxes() {
        val checkboxMapping = mapOf(
            binding.checkPagadas to InvoiceState.PAID,
            binding.checkPendientesPago to InvoiceState.PENDING,
            binding.checkAnuladas to InvoiceState.CANCELLED,
            binding.checkCuotaFija to InvoiceState.FIXED_FEE,
            binding.checkPlanPago to InvoiceState.PAYMENT_PLAN
        )

        checkboxMapping.forEach { (checkbox, state) ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                handleStateToggle(state, isChecked)
            }
        }
    }

    // ========== UI RENDERING ==========

    private fun renderUI(uiState: InvoiceFilterUIState) {
        // Usamos tempFilters si existe (edición en curso), sino el del estado
        val filtersToRender = tempFilters ?: uiState.filters

        with(binding) {
            renderDateButtons(filtersToRender)

            if (uiState.statistics.maxAmount > 0) {
                renderSlider(uiState, filtersToRender)
            }

            renderStateCheckboxes(filtersToRender)

            btnAplicar.isEnabled = !uiState.isApplying
        }
    }

    private fun renderDateButtons(filters: InvoiceFilters) {
        binding.btnSelectDate.text = filters.startDate?.let {
            DateUtils.formatDateShort(it)
        } ?: getString(R.string.dia_mes_ano)

        binding.btnSelectDateUntil.text = filters.endDate?.let {
            DateUtils.formatDateShort(it)
        } ?: getString(R.string.dia_mes_ano)
    }

    private fun renderSlider(uiState: InvoiceFilterUIState, currentFilters: InvoiceFilters) {
        val maxAmount = uiState.statistics.maxAmount

        with(binding.rangeSlider) {
            valueFrom = 0f
            valueTo = maxAmount

            val minVal = (currentFilters.minAmount ?: 0f).coerceIn(0f, maxAmount)
            val maxVal = (currentFilters.maxAmount ?: maxAmount).coerceIn(minVal, maxAmount)

            // Evitar crash si min > max por redondeo
            if (minVal <= maxVal) {
                setValues(minVal, maxVal)
            }
        }

        // Actualizar etiquetas
        binding.tvMaxImporte.text = String.format("%.2f €", maxAmount)
        binding.tvMinValue.text = formatSliderValue(currentFilters.minAmount ?: 0f, maxAmount, isMaxValue = false)
        binding.tvMaxValue.text = formatSliderValue(currentFilters.maxAmount ?: maxAmount, maxAmount, isMaxValue = true)
    }

    private fun renderStateCheckboxes(filters: InvoiceFilters) {
        val states = filters.filteredStates

        // Evitamos disparar listeners al setear estado programáticamente
        with(binding) {
            checkPagadas.isChecked = states.contains(InvoiceState.PAID.serverValue)
            checkPendientesPago.isChecked = states.contains(InvoiceState.PENDING.serverValue)
            checkAnuladas.isChecked = states.contains(InvoiceState.CANCELLED.serverValue)
            checkCuotaFija.isChecked = states.contains(InvoiceState.FIXED_FEE.serverValue)
            checkPlanPago.isChecked = states.contains(InvoiceState.PAYMENT_PLAN.serverValue)
        }
    }

    // ========== EVENT HANDLERS ==========

    private fun handleSliderChange(values: List<Float>) {
        if (values.size != 2) return

        val min = values[0]
        val max = values[1]

        tempFilters = tempFilters?.copy(minAmount = min, maxAmount = max) ?: InvoiceFilters(minAmount = min, maxAmount = max)

        // Feedback visual inmediato
        val maxAmount = viewModel.filterState.value.statistics.maxAmount
        binding.tvMinValue.text = formatSliderValue(min, maxAmount, isMaxValue = false)
        binding.tvMaxValue.text = formatSliderValue(max, maxAmount, isMaxValue = true)
    }

    private fun handleStateToggle(state: InvoiceState, isChecked: Boolean) {
        val currentStates = tempFilters?.filteredStates?.toMutableSet() ?: mutableSetOf()

        if (isChecked) {
            currentStates.add(state.serverValue)
        } else {
            currentStates.remove(state.serverValue)
        }

        // Crear filtros vacíos si es null
        val baseFilters = tempFilters ?: InvoiceFilters()
        tempFilters = baseFilters.copy(filteredStates = currentStates)
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val uiState = viewModel.filterState.value
        // Usamos tempFilters si existe, si no los del estado
        val currentFilters = tempFilters ?: uiState.filters

        // --- 1. Definir Límites (Constraints) ---
        val minDateAllowed = uiState.statistics.oldestDateMillis
        val maxDateAllowed = uiState.statistics.newestDateMillis

        // (Opcional) Ajustar límites según la OTRA fecha ya seleccionada
        // Ej: Si elijo Fin, el mínimo no es oldestDate, es startDate (si existe)
        val validationMin = if (!isStartDate && currentFilters.startDate != null) {
            DateUtils.toEpochMilli(currentFilters.startDate).coerceAtLeast(minDateAllowed)
        } else minDateAllowed

        val validationMax = if (isStartDate && currentFilters.endDate != null) {
            DateUtils.toEpochMilli(currentFilters.endDate).coerceAtMost(maxDateAllowed)
        } else maxDateAllowed


        // --- 2. Definir Selección Inicial (Segura) ---
        val rawSelection = if (isStartDate) {
            currentFilters.startDate?.let { DateUtils.toEpochMilli(it) }
        } else {
            currentFilters.endDate?.let { DateUtils.toEpochMilli(it) }
        }

        // CLAVE: Si la fecha seleccionada cae FUERA del rango permitido, hacemos fallback
        // Esto evita el crash y muestra el calendario en una fecha válida
        val safeSelection = if (rawSelection != null && rawSelection in minDateAllowed..maxDateAllowed) {
            rawSelection
        } else {
            // Fallback inteligente:
            // Si es Fecha Inicio -> Pon el foco en el día más antiguo disponible
            // Si es Fecha Fin -> Pon el foco en el día más reciente disponible
            if (isStartDate) minDateAllowed else maxDateAllowed
        }

        // --- 3. Construir Picker ---
        val constraints = CalendarConstraints.Builder()
            .setStart(minDateAllowed)
            .setEnd(maxDateAllowed)
            .setValidator(RangeValidator(validationMin, validationMax)) // Tu validador custom
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(if (isStartDate) "Seleccionar Inicio" else "Seleccionar Fin")
            .setCalendarConstraints(constraints)
            .setSelection(safeSelection) // ¡Usamos la fecha segura!
            .build()

        picker.addOnPositiveButtonClickListener { selectedMillis ->
            handleDateSelected(selectedMillis, isStartDate)
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }


    private fun handleDateSelected(dateMillis: Long, isStartDate: Boolean) {
        val selectedDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        val baseFilters = tempFilters ?: InvoiceFilters()
        tempFilters = if (isStartDate) {
            baseFilters.copy(startDate = selectedDate)
        } else {
            baseFilters.copy(endDate = selectedDate)
        }

        // Forzar renderizado parcial de botones
        tempFilters?.let { renderDateButtons(it) }
    }

    private fun applyFilters() {
        tempFilters?.let { filters ->
            viewModel.updateFilters(filters)
            viewModel.applyFilters()
        }
        closeFragment()
    }

    private fun closeFragment() {
        parentFragmentManager.popBackStack()
    }

    // ========== HELPERS ==========

    private fun formatSliderValue(value: Float, maxValue: Float, isMaxValue: Boolean): String {
        return if (isMaxValue && value >= maxValue - 0.001f) {
            String.format("%.2f €", value)
        } else {
            String.format("%.0f €", value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
