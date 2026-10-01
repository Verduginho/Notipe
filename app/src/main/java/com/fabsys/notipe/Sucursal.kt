package com.fabsys.notipe

data class Sucursal(
    val id: String? = null, // <-- NUEVO
    val nombre_tienda: String,
    val dispositivo_emisor_id: String
)