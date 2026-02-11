package com.nexosolar.android.ui.invoices

import com.nexosolar.android.core.ErrorClassifier
import com.nexosolar.android.domain.models.Invoice

/**
 * Representa todos los estados posibles de la pantalla de Facturas.
 * Reemplaza al antiguo InvoiceStateManager.
 */
sealed interface InvoiceUIState {

    // 1. Cargando (Shimmer visible)
    object Loading : InvoiceUIState

    // 2. Datos cargados correctamente (Lista visible)
    data class Success(val invoices: List<Invoice>) : InvoiceUIState

    // 3. Carga exitosa pero sin resultados (Empty View)
    object Empty : InvoiceUIState

    // 4. Error (Pantalla de error)
    // Guardamos el tipo de error para saber qué icono/texto mostrar (Red vs Servidor)
    data class Error(
        val message: String,
        val type: ErrorClassifier.ErrorType
    ) : InvoiceUIState
}
