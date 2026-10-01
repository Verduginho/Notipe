package com.fabsys.notipe

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Cargar el Dashboard por defecto al abrir la app
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        // Referencias a los íconos de la barra lateral
        val navDashboard = findViewById<ImageView>(R.id.nav_dashboard)
        val navPagos = findViewById<ImageView>(R.id.nav_pagos) // NUEVO: Ícono de la lista (rayitas)
        val navTiendas = findViewById<ImageView>(R.id.nav_tiendas)
        val navEmpleados = findViewById<ImageView>(R.id.nav_empleados)
        val navAjustes = findViewById<ImageView>(R.id.nav_ajustes)

        // Acción: Click en Dashboard (Resumen / Icono "i")
        navDashboard.setOnClickListener {
            cambiarColorIconos(navDashboard, listOf(navPagos, navTiendas, navEmpleados, navAjustes))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        // Acción: Click en Pagos (Feed en vivo / Icono de rayitas)
        navPagos.setOnClickListener {
            cambiarColorIconos(navPagos, listOf(navDashboard, navTiendas, navEmpleados, navAjustes))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PagosFragment())
                .commit()
        }

        // Acción: Click en Tiendas (Icono llave inglesa)
        navTiendas.setOnClickListener {
            cambiarColorIconos(navTiendas, listOf(navDashboard, navPagos, navEmpleados, navAjustes))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TiendasFragment())
                .commit()
        }

        // Acción: Click en Empleados
        navEmpleados.setOnClickListener {
            cambiarColorIconos(navEmpleados, listOf(navDashboard, navPagos, navTiendas, navAjustes))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmpleadosFragment())
                .commit()
        }

        // Acción: Click en Ajustes
        navAjustes.setOnClickListener {
            cambiarColorIconos(navAjustes, listOf(navDashboard, navPagos, navTiendas, navEmpleados))
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AjustesFragment()) // <-- Restaurado a tu pantalla original
                .commit()
        }
    }

    // Función auxiliar para resaltar visualmente el ícono activo en azul y los demás en gris
    private fun cambiarColorIconos(activo: ImageView, inactivos: List<ImageView>) {
        DrawableCompat.setTint(activo.drawable, Color.parseColor("#42A5F5"))
        for (icono in inactivos) {
            DrawableCompat.setTint(icono.drawable, Color.parseColor("#A3AED0"))
        }
    }
}