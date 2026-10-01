package com.fabsys.notipe

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmpleadosFragment : Fragment() {

    private lateinit var rvEmpleados: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_empleados, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNuevoEmpleado = view.findViewById<Button>(R.id.btnNuevoEmpleado)
        rvEmpleados = view.findViewById(R.id.rvEmpleados)
        rvEmpleados.layoutManager = LinearLayoutManager(requireContext())

        btnNuevoEmpleado.setOnClickListener {
            mostrarDialogoNuevoCajero()
        }

        cargarListaEmpleados()
    }

    private fun cargarListaEmpleados() {
        RetrofitClient.api.obtenerUsuarios(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}"
        ).enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!!

                    // Aquí usamos el Adaptador que actualizamos para detectar clics
                    rvEmpleados.adapter = EmpleadoAdapter(lista) { cajeroSeleccionado ->
                        if (cajeroSeleccionado.id != null) {
                            abrirPanelControl(cajeroSeleccionado)
                        } else {
                            Toast.makeText(requireContext(), "Error: Usuario sin ID", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar cajeros", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {}
        })
    }

    // --- LÓGICA DE PERMISOS (TRAÍDA DEL FRAGMENTO DE PERMISOS) ---

    private fun abrirPanelControl(cajero: Usuario) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_gestionar_permisos, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tvNombreCajeroPermisos).text = "Accesos de: ${cajero.username}"
        val rvTiendasSwitches = dialogView.findViewById<RecyclerView>(R.id.rvTiendasSwitches)
        rvTiendasSwitches.layoutManager = LinearLayoutManager(requireContext())

        dialogView.findViewById<Button>(R.id.btnCerrarPermisos).setOnClickListener { dialog.dismiss() }
        dialog.show()

        RetrofitClient.api.obtenerSucursales(RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}").enqueue(object : Callback<List<Sucursal>> {
            override fun onResponse(call: Call<List<Sucursal>>, responseTiendas: Response<List<Sucursal>>) {
                val todasLasTiendas = responseTiendas.body() ?: emptyList()

                RetrofitClient.api.obtenerPermisosPorUsuario(
                    RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}", "eq.${cajero.id}"
                ).enqueue(object : Callback<List<PermisoSucursal>> {
                    override fun onResponse(call: Call<List<PermisoSucursal>>, responsePermisos: Response<List<PermisoSucursal>>) {

                        val permisos = responsePermisos.body() ?: emptyList()
                        val tiendasPermitidasIds = permisos.map { it.sucursal_id }.toMutableList()

                        rvTiendasSwitches.adapter = PermisoTiendaAdapter(todasLasTiendas, tiendasPermitidasIds) { idTienda, isChecked ->
                            if (isChecked) otorgarPermiso(cajero.id!!, idTienda) else revocarPermiso(cajero.id!!, idTienda)
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
                override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }

    private fun revocarPermiso(idUsuario: String, idTienda: String) {
        RetrofitClient.api.eliminarPermiso(RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}", "eq.$idUsuario", "eq.$idTienda")
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }

    // --- LÓGICA DE CREACIÓN (LA QUE YA TENÍAS) ---

    private fun mostrarDialogoNuevoCajero() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agregar_empleado, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreEmpleado)
        val etUsuario = dialogView.findViewById<EditText>(R.id.etUsuarioEmpleado)
        val etClave = dialogView.findViewById<EditText>(R.id.etClaveEmpleado)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarEmpleado)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val usuarioTexto = etUsuario.text.toString().trim()
            val clave = etClave.text.toString().trim()

            if (nombre.isNotEmpty() && usuarioTexto.isNotEmpty() && clave.isNotEmpty()) {
                val nuevoUsuario = Usuario(username = usuarioTexto, password_hash = clave, rol = "cajero", estado_activo = true)
                RetrofitClient.api.crearUsuario(RetrofitClient.SUPABASE_KEY, "Bearer ${RetrofitClient.SUPABASE_KEY}", nuevoUsuario)
                    .enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(requireContext(), "¡Cajero creado!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                cargarListaEmpleados()
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {}
                    })
            }
        }
        dialog.show()
    }
}