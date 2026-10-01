package com.fabsys.notipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PagoAdapter(private var listaPagos: List<PagoYape>) : RecyclerView.Adapter<PagoAdapter.PagoViewHolder>() {

    class PagoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCliente)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigoYape)
        val tvMonto: TextView = view.findViewById(R.id.tvMontoPago)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pago, parent, false)
        return PagoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagoViewHolder, position: Int) {
        val pago = listaPagos[position]

        holder.tvNombre.text = pago.nombre_remitente
        holder.tvCodigo.text = "Cód: ${pago.codigo_yape}"

        // Formateamos el monto para que siempre tenga 2 decimales (ej. S/ 1.00)
        holder.tvMonto.text = String.format("+ S/ %.2f", pago.monto)
    }

    override fun getItemCount(): Int = listaPagos.size

    fun actualizarPagos(nuevosPagos: List<PagoYape>) {
        listaPagos = nuevosPagos
        notifyDataSetChanged()
    }
}