package com.nexosolar.android.domain.usecase.invoice

import com.nexosolar.android.domain.models.Invoice
import java.time.LocalDate

/**
 * Caso de uso que aplica filtros múltiples al listado de facturas.
 * Implementa la lógica de negocio de filtrado por estado, fecha e importe de forma combinada.
 * Permite al ViewModel delegar la complejidad del filtrado y mantener la UI libre de lógica de negocio.
 */
class FilterInvoicesUseCase {

    // ===== Métodos públicos =====

    /**
     * Filtra el listado de facturas según los criterios proporcionados.
     * Los filtros son acumulativos (AND lógico): una factura debe cumplir todos los criterios activos.
     *
     * @param facturasOriginales Lista completa de facturas sin filtrar
     * @param estadosSeleccionados Estados permitidos (null = sin filtro de estado)
     * @param fechaInicio Fecha mínima (inclusive)
     * @param fechaFin Fecha máxima (inclusive)
     * @param importeMin Importe mínimo (inclusive)
     * @param importeMax Importe máximo (inclusive)
     * @return Lista filtrada según los criterios aplicados
     */
    fun invoke(
        facturasOriginales: List<Invoice>?,
        estadosSeleccionados: List<String>?,
        fechaInicio: LocalDate?,
        fechaFin: LocalDate?,
        importeMin: Double?,
        importeMax: Double?
    ): List<Invoice> {

        if (facturasOriginales.isNullOrEmpty()) {
            return ArrayList()
        }

        val facturasFiltradas = ArrayList<Invoice>()

        for (factura in facturasOriginales) {

            // 1. Filtro por Estado
            var cumpleEstado: Boolean
            if (estadosSeleccionados == null) {
                cumpleEstado = true
            } else {
                // Si la lista de estados seleccionados contiene el estado de la factura
                cumpleEstado = estadosSeleccionados.contains(factura.invoiceStatus)
            }

            // 2. Filtro por Fecha
            var cumpleFecha = true
            val fechaFactura = factura.invoiceDate

            if (fechaFactura != null) {
                if (fechaInicio != null) {
                    cumpleFecha = !fechaFactura.isBefore(fechaInicio)
                }

                if (cumpleFecha && fechaFin != null) {
                    cumpleFecha = !fechaFactura.isAfter(fechaFin)
                }
            } else {
                // Si hay rango pero la factura no tiene fecha, ¿se descarta?
                if (fechaInicio != null || fechaFin != null) {
                    cumpleFecha = false
                }
            }

            // 3. Filtro por Importe
            val importe = factura.invoiceAmount.toDouble()
            var cumpleImporte = true

            if (importeMin != null && importe < importeMin) {
                cumpleImporte = false
            }

            if (cumpleImporte && importeMax != null && importe > importeMax) {
                cumpleImporte = false
            }

            if (cumpleEstado && cumpleFecha && cumpleImporte) {
                facturasFiltradas.add(factura)
            }
        }

        return facturasFiltradas
    }
}
