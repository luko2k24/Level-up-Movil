package cl.levelup.mobile.model.repository

import cl.levelup.mobile.model.local.PedidoDao
import cl.levelup.mobile.model.local.PedidoEntidad
import kotlinx.coroutines.flow.Flow

class PedidoRepository(private val dao: PedidoDao) {

    fun obtenerPedidosPorUsuario(usuarioId: Int): Flow<List<PedidoEntidad>> = dao.obtenerPedidosPorUsuario(usuarioId)

    suspend fun obtenerPedidoPorId(idPedido: Int): PedidoEntidad? = dao.obtenerPedidoPorId(idPedido)

    suspend fun insertarPedido(pedido: PedidoEntidad): Long = dao.insertarPedido(pedido)

    suspend fun actualizarPedido(pedido: PedidoEntidad) = dao.actualizarPedido(pedido)

    suspend fun eliminarPedido(pedido: PedidoEntidad) = dao.eliminarPedido(pedido)

    suspend fun obtenerCantidadPedidos(usuarioId: Int): Int = dao.obtenerCantidadPedidos(usuarioId)

    suspend fun obtenerTotalGastado(usuarioId: Int): Int? = dao.obtenerTotalGastado(usuarioId)
}