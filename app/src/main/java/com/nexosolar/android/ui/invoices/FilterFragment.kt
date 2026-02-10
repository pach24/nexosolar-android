package com.nexosolar.android.ui.invoices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.nexosolar.android.R
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.databinding.FragmentFilterBinding
import com.nexosolar.android.domain.models.InvoiceFilters
import com.nexosolar.android.domain.models.InvoiceState
import com.nexosolar.android.ui.common.RangeValidator
import java.time.Instant
import java.time.ZoneOffset

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InvoiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.filtrosActuales.observe(viewLifecycleOwner) { actualizarUI(it) }
        viewModel.errorValidacion.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnSelectDate.setOnClickListener { abrirDatePicker(true) }
            btnSelectDateUntil.setOnClickListener { abrirDatePicker(false) }

            rangeSlider.addOnChangeListener { slider, _, _ ->
                val values = slider.values
                if (values.size == 2) {
                    val min = values[0]
                    val max = values[1]
                    tvMinValue.text = String.format("%.0f €", min)

                    val maxTotal = viewModel.getMaxImporte()
                    tvMaxValue.text = if (max >= maxTotal - 0.001f) {
                        String.format("%.2f €", max)
                    } else {
                        String.format("%.0f €", max)
                    }
                }
            }

            // Mapeo de Checkboxes con Enum
            val checks = listOf(
                checkPagadas to InvoiceState.PAID,
                checkPendientesPago to InvoiceState.PENDING,
                checkAnuladas to InvoiceState.CANCELLED,
                checkCuotaFija to InvoiceState.FIXED_FEE,
                checkPlanPago to InvoiceState.PAYMENT_PLAN
            )

            checks.forEach { (view, state) ->
                view.setOnCheckedChangeListener { _, isChecked ->
                    // Pasamos el estado al método de actualización
                    actualizarEstadoCheckbox(state.serverValue, isChecked)
                }
            }

            btnAplicar.setOnClickListener { aplicarFiltros() }
            btnBorrar.setOnClickListener { viewModel.resetearFiltros() }
            btnCerrar.setOnClickListener { cerrarFragmento() }
        }
    }

    private fun actualizarUI(filtros: InvoiceFilters?) {
        filtros ?: return

        with(binding) {
            // --- FECHAS ---
            btnSelectDate.text = filtros.startDate?.let {
                DateUtils.formatDateShort(it)
            } ?: getString(R.string.dia_mes_ano)

            btnSelectDateUntil.text = filtros.endDate?.let {
                DateUtils.formatDateShort(it)
            } ?: getString(R.string.dia_mes_ano)

            // --- SLIDER ---
            val maxImporteData = viewModel.getMaxImporte()

            if (maxImporteData > 0) {
                rangeSlider.valueFrom = 0f
                rangeSlider.valueTo = maxImporteData

                // Convertimos Float? -> Float con seguridad y control de límites
                val minVal = (filtros.minAmount ?: 0f).coerceIn(0f, maxImporteData)
                // Capturamos el valor en una variable local inmutable (val)
                // Al ser local, Kotlin sí puede hacer smart cast
                val currentMax = filtros.maxAmount
                val maxVal = if (currentMax == null || currentMax == 0f) {
                    maxImporteData
                } else {
                    currentMax.coerceIn(minVal, maxImporteData)
                }


                // Importante: setValues espera Floats
                rangeSlider.setValues(minVal, maxVal)

                tvMaxImporte.text = String.format("%.2f €", maxImporteData)
                tvMinValue.text = String.format("%.0f €", minVal)
                tvMaxValue.text = String.format("%.0f €", maxVal)
            }

            // --- CHECKBOXES ---
            val estados = filtros.filteredStates
            checkPagadas.isChecked = estados.contains(InvoiceState.PAID.serverValue)
            checkPendientesPago.isChecked = estados.contains(InvoiceState.PENDING.serverValue)
            checkAnuladas.isChecked = estados.contains(InvoiceState.CANCELLED.serverValue)
            checkCuotaFija.isChecked = estados.contains(InvoiceState.FIXED_FEE.serverValue)
            checkPlanPago.isChecked = estados.contains(InvoiceState.PAYMENT_PLAN.serverValue)
        }
    }

    private fun aplicarFiltros() {
        val filtros = viewModel.filtrosActuales.value ?: return

        // Obtenemos valores UI del slider
        val min = binding.rangeSlider.values[0] // Ya es float
        val max = binding.rangeSlider.values[1]

        // Creamos una COPIA del filtro actual actualizando solo el rango
        val nuevosFiltros = filtros.copy(
            minAmount = min,
            maxAmount = max
        )

        // Enviamos el objeto completo (inmutable) al ViewModel
        viewModel.actualizarFiltros(nuevosFiltros)

        cerrarFragmento()
    }

    private fun actualizarEstadoCheckbox(estado: String, isChecked: Boolean) {
        val filtros = viewModel.filtrosActuales.value ?: return

        // 1. Convertimos Set inmutable a MutableSet para operar
        val nuevosEstados = filtros.filteredStates.toMutableSet()

        if (isChecked) {
            nuevosEstados.add(estado)
        } else {
            nuevosEstados.remove(estado)
        }

        // 2. Creamos copia del filtro con el nuevo Set y valores actuales del slider
        val nuevosFiltros = filtros.copy(
            filteredStates = nuevosEstados, // Se vuelve Set automáticamente
            minAmount = binding.rangeSlider.values[0],
            maxAmount = binding.rangeSlider.values[1]
        )

        viewModel.actualizarEstadoFiltros(nuevosFiltros)
    }

    private fun abrirDatePicker(esInicio: Boolean) {
        val current = viewModel.filtrosActuales.value
        val minGlobal = viewModel.getOldestDateMillis()
        val maxGlobal = viewModel.getNewestDateMillis()

        // Lógica de validación (se mantiene igual, solo cambia el acceso a propiedades)
        val validacionMin = if (esInicio) {
            minGlobal
        } else {
            DateUtils.toEpochMilli(current?.startDate).takeIf { it != 0L } ?: minGlobal
        }

        val validacionMax = if (!esInicio) {
            maxGlobal
        } else {
            DateUtils.toEpochMilli(current?.endDate).takeIf { it != 0L } ?: maxGlobal
        }

        val constraints = CalendarConstraints.Builder()
            .setStart(minGlobal)
            .setEnd(maxGlobal)
            .setValidator(RangeValidator(validacionMin, validacionMax))
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(if (esInicio) "Seleccionar Inicio" else "Seleccionar Fin")
            .setCalendarConstraints(constraints)
            .setSelection(if (esInicio) validacionMin else validacionMax)
            .build()

        picker.addOnPositiveButtonClickListener { selectedMillis ->
            val nuevaFecha = Instant.ofEpochMilli(selectedMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()

            // Creamos COPIA con copy() en lugar de modificar campos
            val nuevosFiltros = (current ?: InvoiceFilters()).copy(
                startDate = if (esInicio) nuevaFecha else current?.startDate,
                endDate = if (!esInicio) nuevaFecha else current?.endDate,
                // Mantenemos importes actuales para no resetear slider si el usuario lo movió
                minAmount = binding.rangeSlider.values[0],
                maxAmount = binding.rangeSlider.values[1]
            )

            viewModel.actualizarEstadoFiltros(nuevosFiltros)
        }

        picker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun cerrarFragmento() {
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
