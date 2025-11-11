package com.example.level_up.api

import com.example.level_up.local.UsuarioEntidad
import retrofit2.Response
import retrofit2.http.*

interface UsuarioService {
    @POST("usuarios/registrar")
    suspend fun registrar(@Body usuario: UsuarioEntidad): Response<UsuarioEntidad>

    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioEntidad>

    @PUT("usuarios/{id}/sesion")
    suspend fun actualizarEstadoSesion(
        @Path("id") id: Long,
        @Query("iniciada") iniciada: Boolean
    ): Response<UsuarioEntidad>

    @PUT("usuarios/{id}/stats")
    suspend fun actualizarStats(
        @Path("id") id: Long,
        @Body stats: StatsUpdateRequest
    ): Response<UsuarioEntidad>
}