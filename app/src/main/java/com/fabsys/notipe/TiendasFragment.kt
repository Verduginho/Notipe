package com.fabsys.notipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TiendasFragment : Fragment() {

    private lateinit var rvTiendas: RecyclerView
    private lateinit var tiendaAdapter: TiendaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tiendas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAgregar = view.findViewById<MaterialButton>(R.id.btnAgregarTienda)
        rvTiendas = view.findViewById(R.id.rvTiendas)

// Configurar RecyclerView con el clic en la tarjeta
        rvTiendas.layoutManager = LinearLayoutManager(requireContext())
        tiendaAdapter = TiendaAdapter(emptyList()) { sucursalSeleccionada ->
            mostrarDialogoAsignarEmisor(sucursalSeleccionada)
        }
        rvTiendas.adapter = tiendaAdapter

        // Cargar tiendas al abrir la pantalla
        cargarTiendasDesdeSupabase()

        btnAgregar.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    private fun cargarTiendasDesdeSupabase() {
        val call = RetrofitClient.api.obtenerSucursales(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}"
        )

        call.enqueue(object : Callback<List<Sucursal>> {
            override fun onResponse(call: Call<List<Sucursal>>, response: Response<List<Sucursal>>) {
                if (response.isSuccessful && response.body() != null) {
                    val lista = response.body()!!
                    tiendaAdapter.actualizarDatos(lista)
                } else {
                    Toast.makeText(requireContext(), "Error al cargar tiendas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogoAgregar() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agregar_tienda, null)
        val etNombreTienda = dialogView.findViewById<TextInputEditText>(R.id.etNombreTienda)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva Sucursal")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombreTienda.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    guardarTiendaEnSupabase(nombre)
                } else {
                    Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarTiendaEnSupabase(nombre: String) {
        val nuevaSucursal = Sucursal(nombre_tienda = nombre, dispositivo_emisor_id = "Sin asignar")

        val call = RetrofitClient.api.crearSucursal(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}",
            sucursal = nuevaSucursal
        )

        call.enqueue(object : Callback<List<Sucursal>> {
            override fun onResponse(call: Call<List<Sucursal>>, response: Response<List<Sucursal>>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Tienda creada con éxito", Toast.LENGTH_SHORT).show()
                    cargarTiendasDesdeSupabase() // Recargamos la lista automáticamente
                } else {
                    val errorReal = response.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(requireContext(), "Error: $errorReal", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun mostrarDialogoAsignarEmisor(sucursal: Sucursal) {
        if (sucursal.id == null) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Asignar Lector")
            .setMessage("¿Deseas que este celular sea el encargado de capturar los Yapes de ${sucursal.nombre_tienda}?")
            .setPositiveButton("Sí, asignar") { _, _ ->
                asignarCelularEnSupabase(sucursal.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun asignarCelularEnSupabase(idSucursal: String) {
        // Obtenemos un ID único y seguro del celular actual
        val androidId = android.provider.Settings.Secure.getString(requireContext().contentResolver, android.provider.Settings.Secure.ANDROID_ID)

        // Preparamos el dato a modificar
        val actualizacion = mapOf("dispositivo_emisor_id" to androidId)

        val call = RetrofitClient.api.asignarDispositivo(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}",
            idTienda = "eq.$idSucursal",
            datos = actualizacion
        )

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Celular asignado con éxito", Toast.LENGTH_SHORT).show()
                    cargarTiendasDesdeSupabase() // Recargamos para que se ponga en verde
                } else {
                    Toast.makeText(requireContext(), "Error al asignar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }


}