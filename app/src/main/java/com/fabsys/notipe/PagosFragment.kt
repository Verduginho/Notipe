package com.fabsys.notipe

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PagosFragment : Fragment() {

    private lateinit var rvPagos: RecyclerView
    private lateinit var pagoAdapter: PagoAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pagos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPagos = view.findViewById(R.id.rvPagos)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)

        // Configurar RecyclerView
        rvPagos.layoutManager = LinearLayoutManager(requireContext())
        pagoAdapter = PagoAdapter(emptyList())
        rvPagos.adapter = pagoAdapter

        // Configurar el gesto de deslizar para actualizar
        swipeRefresh.setOnRefreshListener {
            cargarPagos()
        }

        // Cargar pagos por primera vez al abrir la pantalla
        cargarPagos()
    }

    private fun cargarPagos() {
        swipeRefresh.isRefreshing = true

        // 1. Obtenemos el ID de este celular
        val androidId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)

        // 2. Obtenemos la fecha de HOY desde las 00:00:00
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoy = sdf.format(Date())
        val filtroFecha = "gte.${fechaHoy}T00:00:00"

        // 3. Consultamos a qué tienda pertenece este celular
        RetrofitClient.api.obtenerSucursalPorDispositivo(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}",
            emisorId = "eq.$androidId"
        ).enqueue(object : Callback<List<Sucursal>> {
            override fun onResponse(call: Call<List<Sucursal>>, response: Response<List<Sucursal>>) {
                val sucursales = response.body()

                if (response.isSuccessful && !sucursales.isNullOrEmpty()) {
                    val idSucursal = sucursales[0].id
                    val filtroSucursal = "eq.$idSucursal"

                    // 4. Descargamos SOLAMENTE los pagos de ESTA tienda y de HOY
                    RetrofitClient.api.obtenerPagosDelDia(
                        apiKey = RetrofitClient.SUPABASE_KEY,
                        authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}",
                        sucursalId = filtroSucursal,
                        fechaFiltro = filtroFecha
                    ).enqueue(object : Callback<List<PagoYape>> {
                        override fun onResponse(call: Call<List<PagoYape>>, responsePagos: Response<List<PagoYape>>) {
                            swipeRefresh.isRefreshing = false // Detener animación

                            if (responsePagos.isSuccessful) {
                                val lista = responsePagos.body() ?: emptyList()
                                pagoAdapter.actualizarPagos(lista) // Tu adaptador original
                            } else {
                                Toast.makeText(requireContext(), "Error al cargar los pagos", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<List<PagoYape>>, t: Throwable) {
                            swipeRefresh.isRefreshing = false
                            Toast.makeText(requireContext(), "Error de red al cargar pagos", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), "Este celular no está asignado a ninguna tienda", Toast.LENGTH_LONG).show()
                    pagoAdapter.actualizarPagos(emptyList()) // Limpiamos la lista por seguridad
                }
            }

            override fun onFailure(call: Call<List<Sucursal>>, t: Throwable) {
                swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Error de red al verificar sucursal", Toast.LENGTH_SHORT).show()
            }
        })
    }
}