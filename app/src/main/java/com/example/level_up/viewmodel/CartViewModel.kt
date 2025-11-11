package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.api.ApiClient
import com.example.level_up.api.StatsUpdateRequest // <<< Importación necesaria
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.CarritoEntidad
import com.example.level_up.local.PedidoEntidad
import com.example.level_up.local.UsuarioEntidad
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.PedidoRepository
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class CartState(
    val isLoading: Boolean = false,
    val isProcessingOrder: Boolean = false,
    val error: String? = null,
    val orderSuccess: Boolean = false,
    val currentUser: UsuarioEntidad? = null
)

// NOTA: EL data class StatsUpdateRequest FUE ELIMINADO DE AQUÍ Y MOVIO A /API

class CartViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val cartRepo = CarritoRepository(db.CarritoDao())
    private val orderRepo = PedidoRepository(db.PedidoDao())
    private val userRepo = UsuarioRepository(db.UsuarioDao())

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    /** Lista reactiva de ítems */
    val items: StateFlow<List<CarritoEntidad>> = cartRepo.observarCarrito()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Subtotal sin descuento */
    val subtotal: StateFlow<Int> = items
        .map { list -> list.sumOf { it.precio * it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Porcentaje de descuento según usuario (DUOC = 20% o por nivel) */
    val discountPct: StateFlow<Int> = state
        .map { s ->
            val u = s.currentUser
            when {
                u == null -> 0
                u.esDuoc -> 20
                else -> Validacion.obtenerPorcentajeDescuento(u.nivel)
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)


    val discountAmount: StateFlow<Int> = combine(subtotal, discountPct) { sub, pct ->
        (sub * pct) / 100
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Total final con descuento aplicado */
    val finalTotal: StateFlow<Int> = combine(subtotal, discountAmount) { sub, disc ->
        (sub - disc).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** Conteo de unidades */
    val itemCount: StateFlow<Int> = items
        .map { list -> list.sumOf { it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(currentUser = userRepo.obtenerUsuarioActual())
        }
    }

    fun updateQuantity(item: CarritoEntidad, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    cartRepo.eliminarItemCarrito(item)
                } else {
                    cartRepo.actualizarCantidad(item.productoId, newQuantity)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al actualizar cantidad: ${e.message}")
            }
        }
    }

    /** eliminar por ID  */
    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                cartRepo.eliminarPorId(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al eliminar producto: ${e.message}")
            }
        }
    }

    /** Eliminar por entidad */
    fun removeItem(item: CarritoEntidad) {
        viewModelScope.launch {
            try {
                cartRepo.eliminarItemCarrito(item)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al eliminar producto: ${e.message}")
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepo.limpiar()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al vaciar carrito: ${e.message}")
            }
        }
    }

    /**
     * Procesa la orden llamando a los microservicios.
     */
    fun processOrder() {
        viewModelScope.launch {
            val currentItems = items.value
            val currentUser = _state.value.currentUser

            if (currentItems.isEmpty()) {
                _state.value = _state.value.copy(error = "El carrito está vacío")
                return@launch
            }

            if (currentUser == null) {
                _state.value = _state.value.copy(error = "Debes iniciar sesión para realizar una compra")
                return@launch
            }

            _state.value = _state.value.copy(isProcessingOrder = true)

            try {
                val subtotal = currentItems.sumOf { it.precio * it.cantidad }
                val discountPercentage = if (currentUser.esDuoc) 20 else Validacion.obtenerPorcentajeDescuento(currentUser.nivel)
                val discountAmount = (subtotal * discountPercentage) / 100
                val finalAmount = subtotal - discountAmount

                // Generar JSON de ítems
                val itemsJson = currentItems.joinToString(";") { "${it.nombre}:${it.cantidad}:${it.precio}" }

                val order = PedidoEntidad(
                    usuarioId = currentUser.id,
                    montoTotal = subtotal,
                    montoDescuento = discountAmount,
                    montoFinal = finalAmount,
                    estado = "completed",
                    itemsJson = itemsJson
                )

                // 1. *** LLAMADA A LA API: CREAR PEDIDO (msvc-pedidos:8083) ***
                val orderResponse = ApiClient.pedidoService.crear(order)
                if (!orderResponse.isSuccessful) {
                    throw HttpException(orderResponse)
                }

                // 2. Calcular y actualizar stats
                val newTotalPurchases = currentUser.totalCompras + 1
                val pointsEarned = (finalAmount / 1000).toInt() // 1 point per 1000 CLP
                val newPoints = currentUser.puntosLevelUp + pointsEarned
                val newLevel = Validacion.calcularNivel(newPoints)

                // Instanciación de StatsUpdateRequest (CORRECTO)
                val statsRequest = StatsUpdateRequest(
                    puntos = newPoints,
                    nivel = newLevel,
                    totalCompras = newTotalPurchases
                )

                // *** LLAMADA A LA API: ACTUALIZAR ESTADÍSTICAS (msvc-usuarios:8085) ***
                val statsResponse = ApiClient.usuarioService.actualizarStats(currentUser.id.toLong(), statsRequest)
                if (!statsResponse.isSuccessful) {
                    throw HttpException(statsResponse)
                }

                // 3. Actualizar la caché local del usuario con los nuevos stats
                val updatedUser = statsResponse.body() // El backend retorna el usuario actualizado
                if (updatedUser != null) {
                    // Copiamos los campos actualizados y mantenemos el ID local (Room)
                    userRepo.actualizar(updatedUser.copy(id = currentUser.id))
                }

                // 4. Limpiar carrito local y notificar éxito
                cartRepo.limpiar()

                _state.value = _state.value.copy(
                    isProcessingOrder = false,
                    orderSuccess = true,
                    error = null
                )

            } catch (e: Exception) {
                val mensajeError = when (e) {
                    is HttpException -> "Error de servicio: Falló el Microservicio de Pedidos o Usuarios (${(e as HttpException).code()})."
                    else -> "Error de red. Asegúrate de que el backend esté corriendo y la IP sea correcta."
                }
                _state.value = _state.value.copy(
                    isProcessingOrder = false,
                    error = mensajeError
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearOrderSuccess() {
        _state.value = _state.value.copy(orderSuccess = false)
    }

    fun getDiscountPercentage(): Int {
        val user = _state.value.currentUser ?: return 0
        return if (user.esDuoc) 20 else Validacion.obtenerPorcentajeDescuento(user.nivel)
    }
}