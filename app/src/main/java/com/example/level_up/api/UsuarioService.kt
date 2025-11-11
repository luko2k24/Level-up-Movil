package com.example.level_up.api

import androidx.compose.ui.graphics.vector.Path
import androidx.room.Query
import com.example.level_up.local.UsuarioEntidad
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.PUT

interface UsuarioService {
    @POST("usuarios/registrar")
    suspend fun registrar(@Body usuario: UsuarioEntidad): Response<UsuarioEntidad> // POST /api/usuarios/registrar

    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioEntidad> // POST /api/usuarios/login

    @PUT("usuarios/{id}/sesion")
    suspend fun actualizarEstadoSesion(
        @Path("id") id: Long, // El ID en Java es Long
        @Query("iniciada") iniciada: Boolean
    ): Response<UsuarioEntidad> // PUT /api/usuarios/{id}/sesion?iniciada={iniciada}

    // Aquí puedes agregar otros endpoints como actualizarStats, obtenerDescuento, etc.
}