package cl.levelup.mobile.model.repository


import cl.levelup.mobile.model.local.UsuarioDao
import cl.levelup.mobile.model.local.UsuarioEntidad
import kotlinx.coroutines.flow.Flow


class UsuarioRepository(private val dao: UsuarioDao) {


    suspend fun registrar(user: UsuarioEntidad): Long = dao.insertar(user)
    suspend fun buscarPorCorreo(email: String): UsuarioEntidad? = dao.buscarPorCorreo(email)
    suspend fun obtenerUsuarioActual(): UsuarioEntidad? = dao.obtenerUsuarioActual()

    suspend fun buscarPorCodigoReferido(referralCode: String): UsuarioEntidad? = dao.buscarPorCodigoReferido(referralCode)


    fun obtenerMejoresUsuarios(): Flow<List<UsuarioEntidad>> = dao.obtenerTopUsuarios()


    suspend fun actualizar(user: UsuarioEntidad) = dao.actualizar(user)
    suspend fun actualizarEstadoSesion(userId: Int, isLoggedIn: Boolean) = dao.actualizarEstadoSesion(userId, isLoggedIn)
    suspend fun actualizarNivelUsuario(userId: Int, points: Int, level: Int) = dao.actualizarNivelUsuario(userId, points, level)
    suspend fun actualizarTotalCompras(userId: Int, totalPurchases: Int) = dao.actualizarTotalCompras(userId, totalPurchases)

    suspend fun eliminarUsuario(userId: Int) = dao.eliminarUsuario(userId)
}