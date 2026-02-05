package com.nexosolar.android.ui.invoices

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nexosolar.android.R
import com.nexosolar.android.core.DateUtils
import com.nexosolar.android.databinding.ItemInvoiceBinding
import com.nexosolar.android.domain.models.Invoice
import com.nexosolar.android.domain.models.InvoiceState
import java.util.Locale

/**
 * InvoiceAdapter
 *
 * Adaptador de RecyclerView que utiliza ListAdapter para una gestión eficiente de cambios.
 * Maneja la lógica visual de las facturas siguiendo el patrón de diseño Clean Code.
 */
class InvoiceAdapter : ListAdapter<Invoice, InvoiceAdapter.InvoiceViewHolder>(InvoiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = ItemInvoiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        // getItem(position) es proporcionado por ListAdapter y es seguro
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder interno que encapsula la lógica de vinculación de datos.
     * Esto cumple con el principio de responsabilidad única (SRP).
     */
    inner class InvoiceViewHolder(private val binding: ItemInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(factura: Invoice) {
            val context = itemView.context

            bindFecha(factura, context)
            bindImporte(factura)
            bindEstado(factura, context)

            itemView.setOnClickListener { showPopup(context) }
        }

        private fun bindFecha(factura: Invoice, context: Context) {
            binding.txtFecha.text = DateUtils.formatDate(factura.invoiceDate).ifEmpty {
                context.getString(R.string.sin_fecha)
            }
        }

        private fun bindImporte(factura: Invoice) {
            binding.txtImporte.text = String.format(Locale.getDefault(), "%.2f €", factura.invoiceAmount)
        }

        private fun bindEstado(factura: Invoice, context: Context) {
            val estado = factura.estadoEnum

            // Si es pagada, ocultamos según requerimiento, de lo contrario configuramos y mostramos
            if (estado == InvoiceState.PAID) {
                binding.txtEstado.visibility = View.GONE
                return
            }

            binding.txtEstado.apply {
                visibility = View.VISIBLE

                // Centralizamos la lógica de color y texto
                when (estado) {
                    InvoiceState.PENDING, InvoiceState.CANCELLED -> {
                        setText(if (estado == InvoiceState.PENDING) R.string.estado_pendiente else R.string.estado_anulada)
                        setTextColor(ContextCompat.getColor(context, R.color.texto_alerta))
                    }
                    InvoiceState.FIXED_FEE, InvoiceState.PAYMENT_PLAN -> {
                        setText(if (estado == InvoiceState.FIXED_FEE) R.string.estado_cuota_fija else R.string.estado_plan_pago)
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                    else -> {
                        text = factura.invoiceStatus
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                }
            }
        }
    }

    /**
     * Muestra un diálogo informativo.
     * Se extrae el Context del ViewHolder para mayor limpieza.
     */
    private fun showPopup(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Información")
            .setMessage("Esta funcionalidad aún no está disponible")
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Callback para calcular las diferencias entre listas de forma optimizada.
     * Permite animaciones automáticas en el filtrado.
     */
    class InvoiceDiffCallback : DiffUtil.ItemCallback<Invoice>() {
        override fun areItemsTheSame(oldItem: Invoice, newItem: Invoice): Boolean =
            oldItem.invoiceID == newItem.invoiceID

        override fun areContentsTheSame(oldItem: Invoice, newItem: Invoice): Boolean =
            oldItem == newItem
    }
}