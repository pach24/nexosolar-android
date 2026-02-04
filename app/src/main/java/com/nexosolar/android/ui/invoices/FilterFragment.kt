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
    import java.time.LocalDate
    import java.time.ZoneOffset

    class FilterFragment : Fragment() {

        private var _binding: FragmentFilterBinding? = null
        private val binding get() = _binding!!

        // Usamos el delegado activityViewModels para compartir el VM con InvoiceListActivity
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

                // Mapeo de Checkboxes
                val checks = listOf(
                    checkPagadas to InvoiceState.PAID,
                    checkPendientesPago to InvoiceState.PENDING,
                    checkAnuladas to InvoiceState.CANCELLED,
                    checkCuotaFija to InvoiceState.FIXED_FEE,
                    checkPlanPago to InvoiceState.PAYMENT_PLAN
                )

                checks.forEach { (view, state) ->
                    view.setOnCheckedChangeListener { _, isChecked ->
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
                // Si es null, ponemos el string "día mes año"
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

                    // Si el filtro no tiene valor (es 0 o null), usamos los extremos
                    val minVal = filtros.minAmount?.toFloat()?.coerceIn(0f, maxImporteData) ?: 0f

                    // Si maxAmount es 0 o null en el filtro, usamos el máximo posible
                    val maxVal = if (filtros.maxAmount == null || filtros.maxAmount == 0.0) {
                        maxImporteData
                    } else {
                        filtros.maxAmount.toFloat().coerceIn(minVal, maxImporteData)
                    }

                    // Aplicamos los valores al slider
                    rangeSlider.setValues(minVal, maxVal)

                    tvMaxImporte.text = String.format("%.2f €", maxImporteData)
                    tvMinValue.text = String.format("%.0f €", minVal)
                    tvMaxValue.text = String.format("%.0f €", maxVal)
                }

                // --- CHECKBOXES ---

                val estados = filtros.filteredStates ?: emptyList()
                checkPagadas.isChecked = estados.contains(InvoiceState.PAID.serverValue)
                checkPendientesPago.isChecked = estados.contains(InvoiceState.PENDING.serverValue)
                checkAnuladas.isChecked = estados.contains(InvoiceState.CANCELLED.serverValue)
                checkCuotaFija.isChecked = estados.contains(InvoiceState.FIXED_FEE.serverValue)
                checkPlanPago.isChecked = estados.contains(InvoiceState.PAYMENT_PLAN.serverValue)
            }
        }

        // En FilterFragment.kt

        private fun aplicarFiltros() {
            val filtros = viewModel.filtrosActuales.value ?: return
            val estadosActuales = filtros.filteredStates?.toMutableList() ?: mutableListOf()

            val (min, max) = binding.rangeSlider.values[0].toDouble() to binding.rangeSlider.values[1].toDouble()

            // 2. Enviar al ViewModel (no hay lógica aquí, ni IFs de fechas)
            viewModel.aplicarFiltrosSeleccionados(estadosActuales, min, max)

            cerrarFragmento()
        }

        private fun actualizarEstadoCheckbox(estado: String, isChecked: Boolean) {
            val filtros = viewModel.filtrosActuales.value ?: return
            val estadosActuales = filtros.filteredStates?.toMutableList() ?: mutableListOf()

            if (isChecked) {
                if (!estadosActuales.contains(estado)) estadosActuales.add(estado)
            } else {
                estadosActuales.remove(estado)
            }

            filtros.filteredStates = estadosActuales
            val sliderValues = binding.rangeSlider.values
            if (sliderValues.size >= 2) {
                filtros.minAmount = sliderValues[0].toDouble()
                filtros.maxAmount = sliderValues[1].toDouble()
            }
            viewModel.actualizarEstadoFiltros(filtros)
        }

        private fun abrirDatePicker(esInicio: Boolean) {
            val current = viewModel.filtrosActuales.value

            // 1. Límites ABSOLUTOS (Sacados de las facturas reales a través del ViewModel)
            // Estos bloquean que el calendario haga scroll a meses donde no hay datos
            val minGlobal = viewModel.getOldestDateMillis()
            val maxGlobal = viewModel.getNewestDateMillis()

            // 2. Límites de VALIDACIÓN (Sacados de DateUtils)
            // Si elijo Inicio: el tope es la fecha de Fin ya seleccionada (o el tope global)
            // Si elijo Fin: el suelo es la fecha de Inicio ya seleccionada (o el suelo global)
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

            // 3. Configuración de Constraints
            val constraints = CalendarConstraints.Builder()
                .setStart(minGlobal) // Bloquea scroll hacia atrás
                .setEnd(maxGlobal)   // Bloquea scroll hacia adelante
                .setValidator(RangeValidator(validacionMin, validacionMax)) // Pone días en gris
                .build()

            // 4. Construcción del Picker
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(if (esInicio) "Seleccionar Inicio" else "Seleccionar Fin")
                .setCalendarConstraints(constraints)
                // La selección inicial será la fecha que ya tenga el filtro o el límite
                .setSelection(if (esInicio) {
                    DateUtils.toEpochMilli(current?.startDate).takeIf { it != 0L } ?: minGlobal
                } else {
                    DateUtils.toEpochMilli(current?.endDate).takeIf { it != 0L } ?: maxGlobal
                })
                .build()

            // 5. Listener de respuesta
            picker.addOnPositiveButtonClickListener { selectedMillis ->
                val nuevaFecha = Instant.ofEpochMilli(selectedMillis)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()

                // Creamos una copia inmutable del filtro actual con la nueva fecha
                val nuevosFiltros = InvoiceFilters().apply {
                    this.startDate = if (esInicio) nuevaFecha else current?.startDate
                    this.endDate = if (!esInicio) nuevaFecha else current?.endDate

                    // Mantenemos el resto de valores que están en la UI
                    this.minAmount = binding.rangeSlider.values[0].toDouble()
                    this.maxAmount = binding.rangeSlider.values[1].toDouble()
                    this.filteredStates = current?.filteredStates ?: ArrayList()
                }

                viewModel.actualizarEstadoFiltros(nuevosFiltros)
            }

            picker.show(parentFragmentManager, "DATE_PICKER")
        }

        private fun cerrarFragmento() {
            (activity as? InvoiceListActivity)?.restoreMainView()
            parentFragmentManager.popBackStack()
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }