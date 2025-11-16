package com.example.level_up.api

import com.example.level_up.Entidades.ReseniaEntidad
import retrofit2.Response
import retrofit2.http.*

interface ReseniaService {
    @POST("resenias")
    suspend fun crear(@Body resenia: ReseniaEntidad): Response<ReseniaEntidad> // POST /api/resenias

    @GET("resenias/producto/{productoId}")
    suspend fun porProducto(@Path("productoId") productoId: Int): Response<List<ReseniaEntidad>> // GET /api/resenias/producto/{productoId}
}