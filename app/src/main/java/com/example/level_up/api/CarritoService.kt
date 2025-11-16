package com.example.level_up.api

import com.example.level_up.Entidades.CarritoEntidad
import retrofit2.Response
import retrofit2.http.*

interface CarritoService {
    @GET("carrito/usuario/{usuarioId}")
    suspend fun listarPorUsuario(@Path("usuarioId") usuarioId: Int): Response<List<CarritoEntidad>> // GET /api/carrito/usuario/{usuarioId}

    @POST("carrito")
    suspend fun agregar(@Body item: CarritoEntidad): Response<CarritoEntidad> // POST /api/carrito

    @PUT("carrito/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body item: CarritoEntidad): Response<CarritoEntidad> // PUT /api/carrito/{id}

    @DELETE("carrito/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Void> // DELETE /api/carrito/{id}
}