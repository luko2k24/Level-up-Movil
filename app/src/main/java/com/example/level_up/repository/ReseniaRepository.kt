package com.example.level_up.repository


import com.example.level_up.api.ReseniaService // <-- ¡AÑADIDO!
import com.example.level_up.local.ReseniaDao
import com.example.level_up.local.ReseniaEntidad
import kotlinx.coroutines.flow.Flow
import retrofit2.Response // <-- ¡AÑADIDO!

class ReseniaRepository(
    private val dao: ReseniaDao,
    private val apiService: ReseniaService // <-- ¡NUEVA DEPENDENCIA!
) {

    fun obtenerreseniaPorProducto(idProducto: Int): Flow<List<ReseniaEntidad>> =
        dao.obtenerreseniaPorProducto(idProducto)

    // --- NUEVA FUNCIÓN: Envía la reseña al backend y solo guarda si es exitoso ---
    suspend fun enviarYGuardarResena(resenia: ReseniaEntidad): Response<ReseniaEntidad> {
        // 1. Llama al microservicio de reseñas (API)
        val response = apiService.crear(resenia)

        // 2. Si la respuesta es exitosa (200, 201), guarda localmente
        if (response.isSuccessful && response.body() != null) {
            dao.AgregarResenia(resenia) // Guardamos la versión local (Room)
        }

        // 3. Retorna la respuesta de la API para manejo de errores en el ViewModel
        return response
    }

    // Mantener la función de inserción local para otros usos internos de Room
    suspend fun insertarResenaLocal(resenia: ReseniaEntidad) {
        dao.AgregarResenia(resenia)
    }

    suspend fun actualizarresenia(resenia: ReseniaEntidad) {
        dao.ActualizarResenia(resenia)
    }

    suspend fun eliminarResena(resenia: ReseniaEntidad) {
        dao.EliminarResenia(resenia)
    }

    suspend fun obtenerPromedioResenas(idProducto: Int): Float? =
        dao.PromedioResenia(idProducto)

    suspend fun contarResenas(idProducto: Int): Int =
        dao.ObtenercantidadResenia(idProducto)
}