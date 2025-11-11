package com.example.level_up.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM Productos ORDER BY categoria, nombre")
    fun observarTodos(): Flow<List<ProductoEntidad>>

    @Query("SELECT * FROM Productos WHERE categoria = :categoria ORDER BY nombre")
    fun obtenerPorCategoria(categoria: String): Flow<List<ProductoEntidad>>

    @Query("SELECT * FROM Productos WHERE destacado = 1 ORDER BY valoracion DESC")
    fun obtenerDestacados(): Flow<List<ProductoEntidad>>

    @Query("SELECT * FROM Productos WHERE nombre LIKE '%' || :busqueda || '%' OR descripcion LIKE '%' || :busqueda || '%' ORDER BY nombre")
    fun buscarProductos(busqueda: String): Flow<List<ProductoEntidad>>

    @Query("SELECT DISTINCT categoria FROM Productos ORDER BY categoria")
    fun obtenerCategorias(): Flow<List<String>>

    @Query("SELECT * FROM Productos WHERE id = :productoId")
    suspend fun obtenerPorId(productoId: Int): ProductoEntidad?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(vararg productos: ProductoEntidad)

    @Update
    suspend fun actualizar(producto: ProductoEntidad)

    @Delete
    suspend fun eliminar(producto: ProductoEntidad)

    // Utilitarias
    @Query("SELECT COUNT(*) FROM Productos")
    suspend fun contar(): Int

    @Query("DELETE FROM Productos")
    suspend fun eliminarTodos() // <<< FUNCIÓN AGREGADA

    @Query("UPDATE Productos SET valoracion = :valoracion WHERE id = :productoId")
    suspend fun actualizarValoracion(productoId: Int, valoracion: Float)
}