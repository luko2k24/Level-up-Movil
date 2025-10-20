package cl.levelup.mobile.model.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReseniaDao {
    @Query("SELECT * FROM Resenia WHERE productoId = :productId ORDER BY fechaCreacion DESC")
    fun obtenerreseniaPorProducto(productId: Int): Flow<List<ReseniaEntidad>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun AgregarResenia(Resenia: ReseniaEntidad)

    @Update
    suspend fun ActualizarResenia(Resenia: ReseniaEntidad)

    @Delete
    suspend fun EliminarResenia(Resenia: ReseniaEntidad)

    @Query("SELECT AVG(valoracion) FROM Resenia WHERE productoId = :productId")
    suspend fun PromedioResenia(productId: Int): Float?

    @Query("SELECT COUNT(*) FROM Resenia WHERE productoId = :productId")
    suspend fun ObtenercantidadResenia(productId: Int): Int
}
