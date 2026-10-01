package com.fabsys.notipe

data class Usuario(
    val id: String? = null, // <-- NUEVO
    val username: String,
    val password_hash: String,
    val rol: String,
    val estado_activo: Boolean
)