package com.fabsys.notipe

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TiendaAdapter(
    private var listaTiendas: List<Sucursal>,
    private val onClick: (Sucursal) -> Unit // NUEVO: Detector de toques
) : RecyclerView.Adapter<TiendaAdapter.TiendaViewHolder>() {

    class TiendaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreTiendaItem)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoEmisor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiendaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tienda, parent, false)
        return TiendaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TiendaViewHolder, position: Int) {
        val sucursal = listaTiendas[position]
        holder.tvNombre.text = sucursal.nombre_tienda

        if (sucursal.dispositivo_emisor_id != null) {
            holder.tvEstado.text = "Emisor activo: ${sucursal.dispositivo_emisor_id}"
            holder.tvEstado.setTextColor(Color.parseColor("#4CAF50")) // Verde
        } else {
            holder.tvEstado.text = "Sin dispositivo emisor asignado"
            holder.tvEstado.setTextColor(Color.parseColor("#A3AED0")) // Gris
        }

        // NUEVO: Cuando toquen la tarjeta, avisamos al fragmento
        holder.itemView.setOnClickListener {
            onClick(sucursal)
        }
    }

    override fun getItemCount(): Int = listaTiendas.size

    fun actualizarDatos(nuevasTiendas: List<Sucursal>) {
        listaTiendas = nuevasTiendas
        notifyDataSetChanged()
    }
}