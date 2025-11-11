package com.example.level_up.api

import com.example.level_up.local.PedidoEntidad
import retrofit2.Response
import retrofit2.http.*

interface PedidoService {
    @POST("pedidos")
    suspend fun crear(@Body pedido: PedidoEntidad): Response<PedidoEntidad> // POST /api/pedidos

    @GET("pedidos/usuario/{usuarioId}")
    suspend fun porUsuario(@Path("usuarioId") usuarioId: Int): Response<List<PedidoEntidad>> // GET /api/pedidos/usuario/{usuarioId}
}