package cl.levelup.mobile.model.repository

import cl.levelup.mobile.model.local.CarritoDao
import cl.levelup.mobile.model.local.CarritoEntidad
import kotlinx.coroutines.flow.Flow

class CarritoRepository(private val dao: CarritoDao) {

    fun observarCarrito(): Flow<List<CarritoEntidad>> = dao.observarCarrito()

    suspend fun obtenerItemPorProductoId(producId: Int) = dao.obtenerItemPorProductoId(producId)

    suspend fun obtenerConteoItems(): Int = dao.obtenerCantidadItems() // Función traducida

    suspend fun obtenerTotalCarrito(): Int = dao.obtenerTotalCarrito() ?: 0 // Función traducida

    suspend fun insertarOActualizar(item: CarritoEntidad) = dao.insertar(item) // Función traducida (reemplaza 'upsert')

    suspend fun actualizarItemCarrito(item: CarritoEntidad) = dao.actualizar(item) // Función traducida

    suspend fun eliminarItemCarrito(item: CarritoEntidad) = dao.eliminar(item) // Función traducida

    suspend fun eliminarProductoDelCarrito(productId: Int) = dao.eliminarPorProductoId(productId) // Función traducida

    suspend fun limpiar() = dao.eliminarTodos() // Función traducida

    suspend fun actualizarCantidad(productId: Int, quantity: Int) = dao.actualizarCantidad(productId, quantity)

    suspend fun eliminarPorId(id: Int) = dao.eliminarPorId(id)
}