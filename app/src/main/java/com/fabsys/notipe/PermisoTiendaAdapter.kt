package com.fabsys.notipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch

class PermisoTiendaAdapter(
    private val listaTiendas: List<Sucursal>,
    private val tiendasPermitidasIds: MutableList<String>,
    private val onPermisoCambiado: (String, Boolean) -> Unit
) : RecyclerView.Adapter<PermisoTiendaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvPermisoNombreTienda)
        val switchPermiso: MaterialSwitch = view.findViewById(R.id.switchPermisoTienda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_permiso_tienda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tienda = listaTiendas[position]
        holder.tvNombre.text = tienda.nombre_tienda

        // Desconectamos el escuchador temporalmente para evitar activaciones falsas al reciclar vistas
        holder.switchPermiso.setOnCheckedChangeListener(null)

        // Encendemos el interruptor si la tienda está en la lista de permitidas del usuario
        holder.switchPermiso.isChecked = tiendasPermitidasIds.contains(tienda.id)

        // Escuchamos cuando el administrador cambia el interruptor manualmente
        holder.switchPermiso.setOnCheckedChangeListener { _, isChecked ->
            tienda.id?.let { idTienda ->
                onPermisoCambiado(idTienda, isChecked)
            }
        }
    }

    override fun getItemCount(): Int = listaTiendas.size
}