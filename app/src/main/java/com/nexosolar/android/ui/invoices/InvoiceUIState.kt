package com.nexosolar.android.ui.invoices

import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceFilters

/**
 * Representa los estados de la pantalla de lista de facturas.
 */
sealed interface InvoiceUIState {

    /**
     * Estado inicial de carga (Shimmer).
     */
    data object Loading : InvoiceUIState

    /**
     * Estado de éxito con datos.
     * @param invoices Lista de facturas a mostrar (ya filtradas).
     * @param isRefreshing Indica si hay una recarga en segundo plano (spinner pequeño).
     */
    data class Success(
        val invoices: List<Invoice>,
        val isRefreshing: Boolean = false
    ) : InvoiceUIState

    /**
     * Estado sin resultados (lista vacía).
     * Puede ocurrir por no tener facturas o porque los filtros son muy restrictivos.
     */
    data class Empty(
        val isRefreshing: Boolean = false
    ) : InvoiceUIState

    /**
     * Estado de error.
     * @param type Tipo de error para decidir qué icono mostrar (Red vs Servidor).
     */
    data class Error(
        val messageRes: Int, // Usamos Int (R.string) para facilitar la localización en Compose
        val type: ErrorClassifier.ErrorType
    ) : InvoiceUIState
}

/**
 * Estado independiente para la configuración de filtros.
 * Se separa del estado de la lista para no bloquear la UI principal mientras se editan filtros.
 */
data class InvoiceFilterUIState(


    val filters: InvoiceFilters = InvoiceFilters(),
    val statistics: FilterStatistics = FilterStatistics(),
    val isApplying: Boolean = false
) {
    /**
     * Estadísticas calculadas de las facturas actuales.
     * Usadas para configurar los límites de los sliders y calendarios.
     */
    data class FilterStatistics(
        val maxAmount: Float = 0f,
        val oldestDateMillis: Long = 0L,
        val newestDateMillis: Long = 0L
    )
}
