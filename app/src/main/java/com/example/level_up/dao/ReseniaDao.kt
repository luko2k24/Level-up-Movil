package com.example.level_up.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.level_up.Entidades.ReseniaEntidad
import kotlinx.coroutines.flow.Flow

@Dao
interface ReseniaDao {
    @Query("SELECT * FROM Resenia WHERE productoId = :productId ORDER BY fechaCreacion DESC")
    fun obtenerreseniaPorProducto(productId: Int): Flow<List<ReseniaEntidad>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
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