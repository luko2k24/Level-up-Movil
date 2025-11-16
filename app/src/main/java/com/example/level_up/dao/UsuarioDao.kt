package com.example.level_up.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.level_up.Entidades.UsuarioEntidad
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM Usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntidad?

    @Query("SELECT * FROM Usuarios WHERE sesionIniciada = 1 LIMIT 1")
    suspend fun obtenerUsuarioActual(): UsuarioEntidad?

    @Query("SELECT * FROM Usuarios WHERE codigoReferido = :codigoReferido LIMIT 1")
    suspend fun buscarPorCodigoReferido(codigoReferido: String): UsuarioEntidad?

    @Query("SELECT * FROM Usuarios ORDER BY puntosLevelUp DESC LIMIT 10")
    fun obtenerTopUsuarios(): Flow<List<UsuarioEntidad>>


    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertar(usuario: UsuarioEntidad): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntidad)


    @Query("UPDATE Usuarios SET sesionIniciada = :sesionIniciada WHERE id = :usuarioId")
    suspend fun actualizarEstadoSesion(usuarioId: Int, sesionIniciada: Boolean)

    @Query("UPDATE Usuarios SET puntosLevelUp = :puntos, nivel = :nivel WHERE id = :usuarioId")
    suspend fun actualizarNivelUsuario(usuarioId: Int, puntos: Int, nivel: Int)

    @Query("UPDATE Usuarios SET totalCompras = :totalCompras WHERE id = :usuarioId")
    suspend fun actualizarTotalCompras(usuarioId: Int, totalCompras: Int)

    @Query("DELETE FROM Usuarios WHERE id = :usuarioId")
    suspend fun eliminarUsuario(usuarioId: Int)
}