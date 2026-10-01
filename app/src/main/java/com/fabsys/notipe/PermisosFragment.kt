package com.fabsys.notipe

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PermisosFragment : Fragment() {

    private lateinit var rvCajeros: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permisos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCajeros = view.findViewById(R.id.rvCajerosPermisos)
        rvCajeros.layoutManager = LinearLayoutManager(requireContext())

        cargarListaCajeros()
    }

    private fun cargarListaCajeros() {
        RetrofitClient.api.obtenerUsuarios(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}"
        ).enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful && response.body() != null) {
                    // Filtramos para mostrar solo a los que tienen rol "cajero" (opcional, puedes quitar el filter)
                    val cajeros = response.body()!!.filter { it.rol == "cajero" }

                    rvCajeros.adapter = EmpleadoAdapter(cajeros) { cajeroSeleccionado ->
                        if (cajeroSeleccionado.id != null) {
                            abrirPanelControl(cajeroSeleccionado)
                        } else {
                            Toast.makeText(requireContext(), "Error: Usuario sin ID", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {}
        })
    }

    private fun abrirPanelControl(cajero: Usuario) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_gestionar_permisos, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tvNombreCajeroPermisos).text = "Accesos de: ${cajero.username}"
        val rvTiendasSwitches = dialogView.findViewById<RecyclerView>(R.id.rvTiendasSwitches)
        rvTiendasSwitches.layoutManager = LinearLayoutManager(requireContext())

        dialogView.findViewById<Button>(R.id.btnCerrarPermisos).setOnClickListener { dialog.dismiss() }
        dialog.show()

        // 1. Descargamos TODAS las tiendas
        RetrofitClient.api.obtenerSucursales(RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}").enqueue(object : Callback<List<Sucursal>> {
            override fun onResponse(call: Call<List<Sucursal>>, responseTiendas: Response<List<Sucursal>>) {
                val todasLasTiendas = responseTiendas.body() ?: emptyList()

                // 2. Descargamos LOS PERMISOS de este cajero específico
                RetrofitClient.api.obtenerPermisosPorUsuario(
                    RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}", "eq.${cajero.id}"
                ).enqueue(object : Callback<List<PermisoSucursal>> {
                    override fun onResponse(call: Call<List<PermisoSucursal>>, responsePermisos: Response<List<PermisoSucursal>>) {

                        val permisos = responsePermisos.body() ?: emptyList()
                        // Extraemos solo los IDs de las tiendas a las que sí tiene permiso
                        val tiendasPermitidasIds = permisos.map { it.sucursal_id }.toMutableList()

                        // 3. Unimos todo en el adaptador de interruptores
                        rvTiendasSwitches.adapter = PermisoTiendaAdapter(todasLasTiendas, tiendasPermitidasIds) { idTienda, isChecked ->
                            // Esta lógica se dispara cuando mueves un interruptor
                            if (isChecked) {
                                otorgarPermiso(cajero.id!!, idTienda)
                            } else {
                                revocarPermiso(cajero.id!!, idTienda)
                            }
                        }
                    }
                    override fun onFailure(call: Call<List<PermisoSucursal>>, t: Throwable) {}
                })
            }
            override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {}
        })
    }

    private fun otorgarPermiso(idUsuario: String, idTienda: String) {
        val nuevoPermiso = PermisoSucursal(idUsuario, idTienda)
        RetrofitClient.api.asignarPermiso(RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}", nuevoPermiso)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) Toast.makeText(requireContext(), "Error al guardar permiso", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }

    private fun revocarPermiso(idUsuario: String, idTienda: String) {
        RetrofitClient.api.eliminarPermiso(RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}", "eq.$idUsuario", "eq.$idTienda")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!response.isSuccessful) Toast.makeText(requireContext(), "Error al revocar permiso", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }
}