package cl.levelup.mobile.model.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {


    @Query("SELECT * FROM Pedidos WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun obtenerPedidosPorUsuario(usuarioId: Int): Flow<List<PedidoEntidad>>

    @Query("SELECT * FROM Pedidos WHERE id = :pedidoId")
    suspend fun obtenerPedidoPorId(pedidoId: Int): PedidoEntidad?

    @Query("SELECT COUNT(*) FROM Pedidos WHERE usuarioId = :usuarioId")
    suspend fun obtenerCantidadPedidos(usuarioId: Int): Int

    @Query("SELECT SUM(montoFinal) FROM Pedidos WHERE usuarioId = :usuarioId AND estado = 'completed'")
    suspend fun obtenerTotalGastado(usuarioId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPedido(pedido: PedidoEntidad): Long

    @Update
    suspend fun actualizarPedido(pedido: PedidoEntidad)

    @Delete
    suspend fun eliminarPedido(pedido: PedidoEntidad)
}
