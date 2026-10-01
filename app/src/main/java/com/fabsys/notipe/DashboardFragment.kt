package com.fabsys.notipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DashboardFragment : Fragment() {

    private lateinit var tvTotalHoy: TextView
    private lateinit var tvOperacionesHoy: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTotalHoy = view.findViewById(R.id.tvTotalHoy)
        tvOperacionesHoy = view.findViewById(R.id.tvOperacionesHoy)

        tvTotalHoy.text = "S/ 0.00"
        tvOperacionesHoy.text = "0"

        calcularMetricasHoy()
    }

    private fun calcularMetricasHoy() {
        val call = RetrofitClient.api.obtenerPagos(
            apiKey = RetrofitClient.SUPABASE_KEY,
            authToken = "Bearer ${RetrofitClient.SUPABASE_KEY}"
        )

        call.enqueue(object : Callback<List<PagoYape>> {
            override fun onResponse(call: Call<List<PagoYape>>, response: Response<List<PagoYape>>) {
                if (response.isSuccessful && response.body() != null) {
                    val listaPagos = response.body()!!

                    // 1. Configuramos el reloj de la app exactamente en la hora local
                    val zonaHoraria = ZoneId.of("America/Lima")
                    val fechaDeHoy = LocalDate.now(zonaHoraria)

                    // 2. Filtramos la lista para quedarnos SOLO con los pagos de hoy
                    val pagosDeHoy = listaPagos.filter { pago ->
                        try {
                            val fechaTexto = pago.fecha_pago ?: ""
                            // Ajustamos el texto de la nube para que Kotlin lo entienda
                            var fechaLimpia = fechaTexto.replace(" ", "T")
                            if (fechaLimpia.endsWith("+00")) fechaLimpia += ":00"

                            // Convertimos la hora global a tu hora local y verificamos el día
                            val instantePago = Instant.parse(fechaLimpia)
                            val fechaDelPago = instantePago.atZone(zonaHoraria).toLocalDate()

                            fechaDelPago == fechaDeHoy
                        } catch (e: Exception) {
                            false // Si hay un error con la fecha de un pago, lo descartamos del conteo
                        }
                    }

                    // 3. Calculamos usando la lista ya filtrada
                    val totalOperaciones = pagosDeHoy.size
                    val ingresosTotales = pagosDeHoy.sumOf { it.monto }

                    // 4. Actualizamos la pantalla
                    tvOperacionesHoy.text = totalOperaciones.toString()
                    tvTotalHoy.text = String.format("S/ %.2f", ingresosTotales)

                } else {
                    Toast.makeText(requireContext(), "Error al calcular métricas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PagoYape>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }
}