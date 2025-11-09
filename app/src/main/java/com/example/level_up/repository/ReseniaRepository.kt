package com.example.level_up.repository


import com.example.level_up.local.ReseniaDao
import com.example.level_up.local.ReseniaEntidad
import kotlinx.coroutines.flow.Flow

class ReseniaRepository(private val dao: ReseniaDao) {

    fun obtenerreseniaPorProducto(idProducto: Int): Flow<List<ReseniaEntidad>> =
        dao.obtenerreseniaPorProducto(idProducto)

    suspend fun insertarResena(resenia: ReseniaEntidad) {
        dao.AgregarResenia(resenia)
    }

    suspend fun actualizarresenia(resenia: ReseniaEntidad) {
        dao.ActualizarResenia(resenia)
    }

    suspend fun eliminarResena(resena: ReseniaEntidad) {
        dao.EliminarResenia(resena)
    }

    suspend fun obtenerPromedioResenas(idProducto: Int): Float? =
        dao.PromedioResenia(idProducto)

    suspend fun contarResenas(idProducto: Int): Int =
        dao.ObtenercantidadResenia(idProducto)
}