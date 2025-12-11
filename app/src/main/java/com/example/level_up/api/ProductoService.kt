package com.example.level_up.api

import com.example.level_up.Entidades.ProductoEntidad
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductoService {

    // LISTAR TODOS
    @GET("producto")
    suspend fun listarTodos(): Response<List<ProductoEntidad>>

    // BUSCAR POR ID
    @GET("producto/{id}")
    suspend fun obtenerPorId(
        @Path("id") id: Int
    ): Response<ProductoEntidad>

    // CREAR PRODUCTO
    @POST("producto")
    suspend fun crear(
        @Body producto: ProductoEntidad
    ): Response<ProductoEntidad>

    // ACTUALIZAR PRODUCTO
    @PUT("producto/{id}")
    suspend fun actualizar(
        @Path("id") id: Int,
        @Body producto: ProductoEntidad
    ): Response<ProductoEntidad>

    // ELIMINAR PRODUCTO
    @DELETE("producto/{id}")
    suspend fun eliminar(
        @Path("id") id: Int
    ): Response<Unit>
}
