package com.fabsys.notipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmpleadoAdapter(
    private val listaUsuarios: List<Usuario>,
    private val onUsuarioClick: ((Usuario) -> Unit)? = null // <-- NUEVO: Escuchador de clics
) : RecyclerView.Adapter<EmpleadoAdapter.EmpleadoViewHolder>() {

    class EmpleadoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInicial: TextView = view.findViewById(R.id.tvInicial)
        val tvNombreUsuario: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvRolUsuario: TextView = view.findViewById(R.id.tvRolUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpleadoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empleado, parent, false)
        return EmpleadoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpleadoViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.tvNombreUsuario.text = usuario.username
        holder.tvRolUsuario.text = "Rol: ${usuario.rol}"

        if (usuario.username.isNotEmpty()) {
            holder.tvInicial.text = usuario.username.substring(0, 1).uppercase()
        }

        // <-- NUEVO: Acción al tocar la tarjeta del cajero
        holder.itemView.setOnClickListener {
            onUsuarioClick?.invoke(usuario)
        }
    }

    override fun getItemCount(): Int = listaUsuarios.size
}