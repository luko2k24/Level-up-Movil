package com.example.level_up.viewmodel
import cl.levelup.mobile.model.local.PedidoEntidad
import cl.levelup.mobile.model.local.UsuarioEntidad
import cl.levelup.mobile.model.repository.PedidoRepository
import cl.levelup.mobile.model.repository.UsuarioRepository
import cl.levelup.mobile.utils.Validacion
import com.example.level_up.local.BaseDeDatosApp
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val currentUser: UsuarioEntidad? = null,
    val userOrders: List<PedidoEntidad> = emptyList(),
    val totalSpent: Int = 0
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val userRepo = UsuarioRepository(db.UsuarioDao())
    private val orderRepo = PedidoRepository(db.PedidoDao())

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val user = userRepo.obtenerUsuarioActual()
                if (user != null) {
                    val orders = orderRepo.obtenerPedidosPorUsuario(user.id).first()
                    val totalSpent = orderRepo.obtenerTotalGastado(user.id) ?: 0
                    
                    _state.value = _state.value.copy(
                        currentUser = user,
                        userOrders = orders,
                        totalSpent = totalSpent,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Usuario no encontrado"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    fun updateUser(updatedUser: UsuarioEntidad) {
        viewModelScope.launch {
            try {
                userRepo.actualizar(updatedUser)
                _state.value = _state.value.copy(
                    currentUser = updatedUser,
                    isEditing = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Error al actualizar perfil: ${e.message}"
                )
            }
        }
    }

    fun toggleEditMode() {
        _state.value = _state.value.copy(isEditing = !_state.value.isEditing)
    }

    fun logout() {
        viewModelScope.launch {
            val user = _state.value.currentUser
            if (user != null) {
                userRepo.actualizarEstadoSesion(user.id, false)
            }
            _state.value = ProfileState()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun getUserLevelInfo(): Pair<Int, Int> {
        val user = _state.value.currentUser ?: return Pair(1, 0)
        val currentLevel = user.nivel
        val pointsForNextLevel = when (currentLevel) {
            1 -> 500 - user.puntosLevelUp
            2 -> 2000 - user.puntosLevelUp
            3 -> 5000 - user.puntosLevelUp
            4 -> 10000 - user.puntosLevelUp
            else -> 0
        }
        return Pair(currentLevel, pointsForNextLevel)
    }

    fun getDiscountPercentage(): Int {
        val user = _state.value.currentUser ?: return 0
        return Validacion.obtenerPorcentajeDescuento(user.nivel)
    }
}
