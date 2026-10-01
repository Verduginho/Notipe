package com.fabsys.notipe

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment

class AjustesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnPermisoNotificaciones = view.findViewById<Button>(R.id.btnPermisoNotificaciones)
        // Referencia a tu botón de batería (asegúrate de que tenga este ID en tu XML)
        val btnIgnorarBateria = view.findViewById<Button>(R.id.btnIgnorarBateria)
        val btnConfigurarYape = view.findViewById<Button>(R.id.btnConfigurarYape)

        actualizarBotones(btnPermisoNotificaciones, btnIgnorarBateria)

        // Acción 1: Notificaciones
        btnPermisoNotificaciones.setOnClickListener {
            if (!tienePermisoNotificaciones()) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivity(intent)
            }
        }

        // Acción 2: Batería
        btnIgnorarBateria.setOnClickListener {
            if (!estaIgnorandoBateria()) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivity(intent)
            }
        }

        // Acción 3: Abrir ajustes de la app Yape directamente
        btnConfigurarYape.setOnClickListener {
            try {
                // Usamos el paquete exacto de Yape que descubrimos en el Logcat
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:com.bcp.innovacxion.yapeapp")
                }
                startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(requireContext(), "No se encontró Yape en este dispositivo", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let {
            val btnPermisoNotificaciones = it.findViewById<Button>(R.id.btnPermisoNotificaciones)
            val btnIgnorarBateria = it.findViewById<Button>(R.id.btnIgnorarBateria)
            actualizarBotones(btnPermisoNotificaciones, btnIgnorarBateria)
        }
    }

    private fun tienePermisoNotificaciones(): Boolean {
        val paquetesPermitidos = NotificationManagerCompat.getEnabledListenerPackages(requireContext())
        return paquetesPermitidos.contains(requireContext().packageName)
    }

    private fun estaIgnorandoBateria(): Boolean {
        val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)
    }

    private fun actualizarBotones(btnNotificaciones: Button, btnBateria: Button?) {
        // Lógica del botón de notificaciones
        if (tienePermisoNotificaciones()) {
            btnNotificaciones.text = "PERMISO ACTIVO"
            btnNotificaciones.setBackgroundColor(Color.parseColor("#4CAF50"))
            btnNotificaciones.isEnabled = false
        } else {
            btnNotificaciones.text = "CONCEDER PERMISO"
            btnNotificaciones.setBackgroundColor(Color.parseColor("#42A5F5"))
            btnNotificaciones.isEnabled = true
        }

        // Lógica del botón de batería
        btnBateria?.let {
            if (estaIgnorandoBateria()) {
                it.text = "BATERÍA IGNORADA"
                it.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde
                it.isEnabled = false
            } else {
                it.text = "IGNORAR AHORRO DE BATERÍA"
                it.setBackgroundColor(Color.parseColor("#42A5F5")) // Azul
                it.isEnabled = true
            }
        }
    }
}