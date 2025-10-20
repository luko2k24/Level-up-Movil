package cl.levelup.mobile.model.repository


import cl.levelup.mobile.model.local.ReseniaDao
import cl.levelup.mobile.model.local.ReseniaEntidad
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