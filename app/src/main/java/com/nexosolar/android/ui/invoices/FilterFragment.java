package com.nexosolar.android.ui.invoices;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.nexosolar.android.R;
import com.nexosolar.android.core.DateUtils;
import com.nexosolar.android.databinding.FragmentFilterBinding;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.models.InvoiceState;
// AJUSTA ESTE IMPORT según dónde hayas puesto el archivo RangeValidator.java
import com.nexosolar.android.ui.common.RangeValidator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * FilterFragment
 *
 * Fragmento modal que permite aplicar filtros avanzados sobre las facturas.
 * Ofrece filtrado por:
 * - Rango de fechas (inicio y fin)
 * - Rango de importes (slider)
 * - Estados de factura (checkboxes múltiples)
 *
 * Comportamiento de fechas:
 * - UI: Muestra "día/mes/año" cuando no hay fecha explícitamente seleccionada.
 * - Lógica interna: Usa las fechas extremas reales (más antigua/más nueva)
 *   para evitar filtros vacíos innecesarios.
 *
 * Sigue arquitectura MVVM: solo maneja UI y observa el ViewModel compartido.
 */
public class FilterFragment extends Fragment {

    private static final String DATE_PICKER_TAG = "DATE_PICKER";

    // ===== Variables de instancia =====
    private FragmentFilterBinding binding;
    private InvoiceViewModel viewModel;

    // ===== Ciclo de vida =====
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(InvoiceViewModel.class);

        setupObservers();
        setupListeners();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ===== Configuración de observadores =====

    private void setupObservers() {
        viewModel.getFiltrosActuales().observe(getViewLifecycleOwner(), this::actualizarUI);
        viewModel.getErrorValidacion().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== Configuración de listeners =====

    @SuppressLint("DefaultLocale")
    private void setupListeners() {
        binding.btnSelectDate.setOnClickListener(v -> abrirDatePicker(true));
        binding.btnSelectDateUntil.setOnClickListener(v -> abrirDatePicker(false));

        binding.rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() == 2) {
                float min = values.get(0);
                float max = values.get(1);
                binding.tvMinValue.setText(String.format("%.0f €", min));

                float maxImporte = viewModel.getMaxImporte();
                if (max >= maxImporte - 0.001f) {
                    binding.tvMaxValue.setText(String.format("%.2f €", max));
                } else {
                    binding.tvMaxValue.setText(String.format("%.0f €", max));
                }
            }
        });

        binding.checkPagadas.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox(InvoiceState.PAID.serverValue, isChecked));
        binding.checkPendientesPago.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox(InvoiceState.PENDING.serverValue, isChecked));
        binding.checkAnuladas.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox(InvoiceState.CANCELLED.serverValue, isChecked));
        binding.checkCuotaFija.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox(InvoiceState.FIXED_FEE.serverValue, isChecked));
        binding.checkPlanPago.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox(InvoiceState.PAYMENT_PLAN.serverValue, isChecked));

        binding.btnAplicar.setOnClickListener(v -> aplicarFiltros());
        binding.btnBorrar.setOnClickListener(v -> viewModel.resetearFiltros());
        binding.btnCerrar.setOnClickListener(v -> cerrarFragmento());
    }

    // ===== Actualización de UI =====

    @SuppressLint("DefaultLocale")
    private void actualizarUI(InvoiceFilters filtros) {
        if (filtros == null) return;

        // Fechas
        if (filtros.startDate != null) {
            binding.btnSelectDate.setText(DateUtils.formatDateShort(filtros.startDate));
        } else {
            binding.btnSelectDate.setText(R.string.dia_mes_ano);
        }

        if (filtros.endDate != null) {
            binding.btnSelectDateUntil.setText(DateUtils.formatDateShort(filtros.endDate));
        } else {
            binding.btnSelectDateUntil.setText(R.string.dia_mes_ano);
        }

        // Slider
        float maxImporte = viewModel.getMaxImporte();
        if (maxImporte > 0) {
            binding.rangeSlider.setValueFrom(0f);
            binding.rangeSlider.setValueTo(maxImporte);

            float minVal = Math.max(0f, filtros.minAmount.floatValue());
            float maxVal = Math.min(maxImporte, filtros.maxAmount.floatValue());
            if (minVal > maxVal) minVal = maxVal;

            binding.rangeSlider.setValues(minVal, maxVal);
            binding.tvMaxImporte.setText(String.format("%.2f €", maxImporte));
            binding.tvMinValue.setText(String.format("%.0f €", minVal));
            binding.tvMaxValue.setText(String.format("%.0f €", maxVal));
        }

        // Checkboxes
        List<String> estados = filtros.filteredStates;
        if (estados == null) estados = new ArrayList<>();
        binding.checkPagadas.setChecked(estados.contains(InvoiceState.PAID.serverValue));
        binding.checkPendientesPago.setChecked(estados.contains(InvoiceState.PENDING.serverValue));
        binding.checkAnuladas.setChecked(estados.contains(InvoiceState.CANCELLED.serverValue));
        binding.checkCuotaFija.setChecked(estados.contains(InvoiceState.FIXED_FEE.serverValue));
        binding.checkPlanPago.setChecked(estados.contains(InvoiceState.PAYMENT_PLAN.serverValue));
    }

    // ===== Gestión de filtros =====

    private void aplicarFiltros() {
        InvoiceFilters nuevosFiltros = new InvoiceFilters();

        // 1. Estados
        List<String> estados = new ArrayList<>();
        if (binding.checkPagadas.isChecked()) estados.add(InvoiceState.PAID.serverValue);
        if (binding.checkPendientesPago.isChecked()) estados.add(InvoiceState.PENDING.serverValue);
        if (binding.checkAnuladas.isChecked()) estados.add(InvoiceState.CANCELLED.serverValue);
        if (binding.checkCuotaFija.isChecked()) estados.add(InvoiceState.FIXED_FEE.serverValue);
        if (binding.checkPlanPago.isChecked()) estados.add(InvoiceState.PAYMENT_PLAN.serverValue);
        nuevosFiltros.setFilteredStates(estados);

        // 2. Importes
        List<Float> valores = binding.rangeSlider.getValues();
        nuevosFiltros.minAmount = (double) valores.get(0);
        nuevosFiltros.maxAmount = (double) valores.get(1);

        // 3. Fechas
        // IMPORTANTE: Aquí recuperamos las fechas que el usuario HA ELEGIDO explícitamente.
        // Si no ha tocado el calendario, seguirán siendo null (lo cual es correcto).
        InvoiceFilters current = viewModel.getFiltrosActuales().getValue();
        LocalDate start = (current != null) ? current.startDate : null;
        LocalDate end = (current != null) ? current.endDate : null;

        // Solo corregimos si AMBAS existen y están cruzadas
        if (start != null && end != null && start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        nuevosFiltros.startDate = start;
        nuevosFiltros.endDate = end;

        viewModel.actualizarFiltros(nuevosFiltros);
        cerrarFragmento();
    }


    private void actualizarEstadoCheckbox(String estado, boolean isChecked) {
        InvoiceFilters filtros = viewModel.getFiltrosActuales().getValue();
        if (filtros == null) return;

        List<String> estadosActuales = filtros.filteredStates;
        if (estadosActuales == null) estadosActuales = new ArrayList<>();
        else estadosActuales = new ArrayList<>(estadosActuales);

        if (isChecked) {
            if (!estadosActuales.contains(estado)) estadosActuales.add(estado);
        } else {
            estadosActuales.remove(estado);
        }
        filtros.setFilteredStates(estadosActuales);

        // Preservar valores del slider
        List<Float> currentSliderValues = binding.rangeSlider.getValues();
        if (currentSliderValues.size() >= 2) {
            filtros.minAmount = (double) currentSliderValues.get(0);
            filtros.maxAmount = (double) currentSliderValues.get(1);
        }
        viewModel.actualizarEstadoFiltros(filtros);
    }

    // ===== DatePicker =====

    private void abrirDatePicker(boolean esInicio) {
        InvoiceFilters filtrosActuales = viewModel.getFiltrosActuales().getValue();
        if (filtrosActuales == null) return;

        // 1. Límites Globales (el universo de fechas posibles según los datos)
        LocalDate globalMin = viewModel.getOldestDate();
        LocalDate globalMax = viewModel.getNewestDate();
        long today = MaterialDatePicker.todayInUtcMilliseconds();

        long globalMinMillis = (globalMin != null) ? globalMin.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() : today;
        long globalMaxMillis = (globalMax != null) ? globalMax.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() : today;

        // 2. Límites Restringidos (según la selección actual del OTRO campo)
        long constraintMin;
        long constraintMax;

        if (esInicio) {
            // Si elijo inicio, no puedo ir más allá del final seleccionado (si lo hay)
            constraintMin = globalMinMillis;
            if (filtrosActuales.endDate != null) {
                constraintMax = filtrosActuales.endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            } else {
                constraintMax = globalMaxMillis;
            }
        } else {
            // Si elijo fin, no puedo ir antes del inicio seleccionado (si lo hay)
            if (filtrosActuales.startDate != null) {
                constraintMin = filtrosActuales.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            } else {
                constraintMin = globalMinMillis;
            }
            constraintMax = globalMaxMillis;
        }

        // Defensa contra inconsistencias (min > max)
        if (constraintMin > constraintMax) {
            constraintMin = globalMinMillis;
            constraintMax = globalMaxMillis;
        }

        // 3. CONSTRUCCIÓN DEL VALIDADOR PROPIO
        // Aquí usamos tu RangeValidator que compara LocalDate vs LocalDate
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(globalMinMillis) // Esto permite hacer scroll visual hasta el mínimo global
                .setEnd(globalMaxMillis)   // Esto permite hacer scroll visual hasta el máximo global
                .setValidator(new RangeValidator(constraintMin, constraintMax)); // <--- VALIDACIÓN REAL

        // 4. Selección inicial del calendario
        LocalDate fechaActual = esInicio ? filtrosActuales.startDate : filtrosActuales.endDate;
        long selection;
        if (fechaActual != null) {
            selection = fechaActual.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } else {
            // Si no hay selección, sugerimos una fecha inteligente
            LocalDate smartDate = esInicio ? globalMin : globalMax;
            if (smartDate == null) smartDate = LocalDate.now();
            selection = smartDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        }

        // Aseguramos que la selección inicial caiga dentro del rango permitido
        if (selection < constraintMin) selection = constraintMin;
        if (selection > constraintMax) selection = constraintMax;

        constraintsBuilder.setOpenAt(selection);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(esInicio ? "Seleccionar Inicio" : "Seleccionar Fin")
                .setSelection(selection)
                .setCalendarConstraints(constraintsBuilder.build())
                .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selectedMillis -> {
            LocalDate nuevaFecha = Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate();
            InvoiceFilters filtros = viewModel.getFiltrosActuales().getValue();

            if (filtros != null) {
                if (esInicio) {
                    filtros.startDate = nuevaFecha;
                    // Si la nueva fecha de inicio supera al fin actual, empujamos el fin
                    if (filtros.endDate != null && nuevaFecha.isAfter(filtros.endDate)) {
                        filtros.endDate = nuevaFecha;
                    }
                } else {
                    filtros.endDate = nuevaFecha;
                    // Si la nueva fecha de fin es anterior al inicio actual, empujamos el inicio
                    if (filtros.startDate != null && filtros.startDate.isAfter(nuevaFecha)) {
                        filtros.startDate = nuevaFecha;
                    }
                }
                viewModel.actualizarEstadoFiltros(filtros);
            }
        });

        datePicker.show(getParentFragmentManager(), DATE_PICKER_TAG);
    }

    private void cerrarFragmento() {
        if (getActivity() instanceof InvoiceListActivity) {
            ((InvoiceListActivity) getActivity()).restoreMainView();
        }
        getParentFragmentManager().popBackStack();
    }
}
