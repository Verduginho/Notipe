package com.fabsys.notipe

data class PagoYape(
    val nombre_remitente: String,
    val monto: Double,
    val codigo_yape: String,
    val sucursal_id: String? = null, // <-- SOLO CAMBIA ESTO
    val fecha_pago: String? = null
)