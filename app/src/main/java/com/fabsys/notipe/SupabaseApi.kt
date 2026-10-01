package com.fabsys.notipe

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @GET("rest/v1/usuarios_sistema")
    fun validarLogin(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("username") username: String = "eq.",
        @Query("password_hash") password: String = "eq."
    ): Call<List<Usuario>>

    // NUEVO: Método para insertar una nueva tienda
    @POST("rest/v1/sucursales")
    fun crearSucursal(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body sucursal: Sucursal
    ): Call<List<Sucursal>>

    // NUEVO: Método para leer las tiendas
    @GET("rest/v1/sucursales")
    fun obtenerSucursales(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Call<List<Sucursal>>

    // NUEVO: Método para asignar el dispositivo a una tienda específica
    @retrofit2.http.PATCH("rest/v1/sucursales")
    fun asignarDispositivo(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("id") idTienda: String, // Formato: "eq.ID_DE_LA_TIENDA"
        @Body datos: Map<String, String>
    ): Call<Void>

    // NUEVO: Método para insertar un pago capturado
    @POST("rest/v1/pagos_yape")
    fun registrarPago(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Body pago: PagoYape
    ): retrofit2.Call<Void>

    @POST("rest/v1/usuarios_sistema")
    fun crearUsuario(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Body usuario: Usuario
    ): retrofit2.Call<Void>

    // NUEVO: Buscar tienda asignada a este celular
    @GET("rest/v1/sucursales")
    fun obtenerTiendaPorDispositivo(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("dispositivo_emisor_id") androidId: String
    ): retrofit2.Call<List<Sucursal>>

    // NUEVO: Obtener los últimos pagos ordenados del más reciente al más antiguo
    @GET("rest/v1/pagos_yape?select=*&order=fecha_pago.desc")
    fun obtenerPagos(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String
    ): retrofit2.Call<List<PagoYape>>

    @GET("rest/v1/usuarios_sistema")
    fun obtenerUsuarios(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String
    ): retrofit2.Call<List<Usuario>>

    @GET("rest/v1/usuarios_sistema")
    fun loginUsuario(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("username") usernameEq: String, // Filtro por usuario
        @Query("password_hash") passwordEq: String // Filtro por contraseña
    ): retrofit2.Call<List<Usuario>>

    @GET("rest/v1/permisos_sucursales")
    fun obtenerPermisosPorUsuario(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("usuario_id") usuarioIdEq: String // Para buscar "eq.EL_ID_DEL_CAJERO"
    ): retrofit2.Call<List<PermisoSucursal>>

    @POST("rest/v1/permisos_sucursales")
    fun asignarPermiso(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Body permiso: PermisoSucursal
    ): retrofit2.Call<Void>

    @DELETE("rest/v1/permisos_sucursales")
    fun eliminarPermiso(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("usuario_id") usuarioIdEq: String,
        @Query("sucursal_id") sucursalIdEq: String
    ): retrofit2.Call<Void>

    @POST("rest/v1/pagos_yape")
    fun registrarPagoYape(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Body pago: PagoYape
    ): retrofit2.Call<Void>

    @GET("rest/v1/sucursales")
    fun obtenerSucursalPorDispositivo(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("dispositivo_emisor_id") emisorId: String
    ): Call<List<Sucursal>>

    // Trae los pagos ordenados del más nuevo al más viejo, filtrados por sucursal y fecha
    @GET("rest/v1/pagos_yape?select=*&order=fecha_pago.desc")
    fun obtenerPagosDelDia(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Query("sucursal_id") sucursalId: String, // Usaremos: "eq.AQUI_EL_ID"
        @Query("fecha_pago") fechaFiltro: String  // Usaremos: "gte.2026-09-18T00:00:00"
    ): retrofit2.Call<List<PagoYape>>

}

