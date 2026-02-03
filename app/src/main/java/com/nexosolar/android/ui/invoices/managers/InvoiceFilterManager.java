package com.nexosolar.android.ui.invoices.managers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nexosolar.android.core.DateValidator;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.models.InvoiceState;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor especializado en el ESTADO de los filtros.
 *
 * - SRP: Solo gestiona el estado y orquesta el filtrado.
 * - Delegación: Usa DateValidator para validación y InvoiceStatisticsCalculator para valores por defecto.
 */
public class InvoiceFilterManager {

    private final FilterInvoicesUseCase filterUseCase;
    private final InvoiceStatisticsCalculator calculator;

    private final MutableLiveData<InvoiceFilters> _currentFilters = new MutableLiveData<>();
    private final MutableLiveData<String> _validationError = new MutableLiveData<>();

    public InvoiceFilterManager(FilterInvoicesUseCase filterUseCase) {
        this.filterUseCase = filterUseCase;
        this.calculator = new InvoiceStatisticsCalculator(); // Podría inyectarse también
        initializeDefaultFilters();
    }

    // ===== Getters =====

    public LiveData<InvoiceFilters> getCurrentFilters() {
        return _currentFilters;
    }

    public LiveData<String> getValidationError() {
        return _validationError;
    }

    // ===== Gestión de Estado =====

    public void updateFilters(InvoiceFilters filters) {
        if (filters == null) {
            _validationError.setValue("Los filtros no pueden ser nulos");
            return;
        }

        // Auto-corrección defensiva (evita toasts por fechas cruzadas).
        LocalDate start = filters.startDate;
        LocalDate end = filters.endDate;
        if (start != null && end != null && start.isAfter(end)) {
            filters.startDate = end;
            filters.endDate = start;
        }

        // USO DE DATE VALIDATOR: Validación delegada
        if (DateValidator.isValidRange(filters.startDate, filters.endDate)) {
            _currentFilters.setValue(filters);
            _validationError.setValue(null);
        } else {
            // Si aún así fuese inválido, no reventamos la UX: dejamos el error disponible.
            _validationError.setValue("La fecha de inicio no puede ser posterior a la fecha final");
        }
    }

    public void resetFilters(List<Invoice> invoices) {
        InvoiceFilters defaultFilters = new InvoiceFilters();

        defaultFilters.startDate = null;
        defaultFilters.endDate = null;
        defaultFilters.minAmount = 0.0;

        // USO DE CALCULATOR: Delegamos el cálculo del máximo
        float maxAmount = calculator.calculateMaxAmount(invoices);
        defaultFilters.maxAmount = (double) maxAmount;

        // Lista vacía = “sin filtrar por estado” (y en applyCurrentFilters se interpreta como TODOS)
        defaultFilters.setFilteredStates(new ArrayList<>());

        _currentFilters.setValue(defaultFilters);
        _validationError.setValue(null);
    }

    // ===== Ejecución de Filtros =====

    public List<Invoice> applyCurrentFilters(List<Invoice> invoices) {
        InvoiceFilters filters = _currentFilters.getValue();
        if (filters == null || invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> statesToFilter = filters.filteredStates;
        // REGLA: Lista vacía = Todos los estados
        if (statesToFilter == null || statesToFilter.isEmpty()) {
            statesToFilter = new ArrayList<>();
            for (InvoiceState state : InvoiceState.values()) {
                statesToFilter.add(state.serverValue);
            }
        }

        // REGLA: Fechas nulas = Extremos del dataset
        // Calculamos fechas efectivas TEMPORALES solo para esta ejecución
        LocalDate effectiveStart = filters.startDate;
        LocalDate effectiveEnd = filters.endDate;

        if (effectiveStart == null) {
            effectiveStart = calculator.calculateOldestDate(invoices);
        }
        if (effectiveEnd == null) {
            effectiveEnd = calculator.calculateNewestDate(invoices);
            if (effectiveEnd == null) effectiveEnd = LocalDate.now();
        }

        return filterUseCase.invoke(
                invoices,
                statesToFilter,
                effectiveStart, // Usamos las fechas efectivas
                effectiveEnd,   // Usamos las fechas efectivas
                filters.minAmount,
                filters.maxAmount
        );
    }


    // ===== Métodos de Consulta de Estado =====

    public boolean hasActiveFilters() {
        InvoiceFilters filters = _currentFilters.getValue();
        if (filters == null) return false;


        if (filters.filteredStates != null && !filters.filteredStates.isEmpty()) return true;
        if (filters.startDate != null || filters.endDate != null) return true;

        // Comprobamos si el rango es distinto al por defecto (0 - MAX)
        return filters.minAmount > 0 ||
                (filters.maxAmount != null && filters.maxAmount < Double.MAX_VALUE);
    }

    private void initializeDefaultFilters() {
        InvoiceFilters defaultFilters = new InvoiceFilters();
        defaultFilters.minAmount = 0.0;
        defaultFilters.maxAmount = Double.MAX_VALUE;
        defaultFilters.setFilteredStates(new ArrayList<>());
        _currentFilters.setValue(defaultFilters);
    }
}
