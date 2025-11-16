package com.example.level_up.api

import com.example.level_up.Entidades.ProductoEntidad
import retrofit2.Response
import retrofit2.http.GET

interface ProductoService {
    @GET("producto")
    suspend fun listarTodos(): Response<List<ProductoEntidad>> // GET /api/producto
}