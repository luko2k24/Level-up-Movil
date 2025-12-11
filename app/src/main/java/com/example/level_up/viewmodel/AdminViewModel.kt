package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.Entidades.ProductoEntidad
import com.example.level_up.api.ApiClient
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AdminState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalProductos: Int = 0,
    val totalDestacados: Int = 0
)

class AdminViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BaseDeDatosApp.obtener(app)
    private val repo = ProductoRepository(db.ProductoDao())
    private val api = ApiClient.productoService

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    // Observa productos desde Room
    val productos: StateFlow<List<ProductoEntidad>> =
        repo.observarTodos()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    init {
        refreshProductsFromAPI()

        // Actualiza estadísticas en vivo
        viewModelScope.launch {
            productos.collect { lista ->
                _state.update { old ->
                    old.copy(
                        totalProductos = lista.size,
                        totalDestacados = lista.count { it.destacado }
                    )
                }
            }
        }
    }

    // 🔵 Cargar desde API → Room
    fun refreshProductsFromAPI() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.listarTodos()
                if (response.isSuccessful && response.body() != null) {

                    val remotos = response.body()!!

                    repo.eliminarTodos()
                    repo.insertarTodos(*remotos.toTypedArray())

                } else {
                    _state.update {
                        it.copy(error = "Error al listar productos: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Error servidor (${e.code()}). ¿Backend 8081 activo?"
                    else -> "Error de red conectando al microservicio."
                }
                _state.update { it.copy(error = msg) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // 🔵 CREAR producto (POST)
    fun crearProducto(producto: ProductoEntidad) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val response = api.crear(producto)
                if (response.isSuccessful && response.body() != null) {

                    val creado = response.body()!!
                    // CORRECTO: usar insertarTodos con un solo elemento
                    repo.insertarTodos(creado)

                } else {
                    _state.update { it.copy(error = "Error al crear: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al crear producto (backend apagado).") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // 🔵 ACTUALIZAR producto (PUT)
    fun actualizarProducto(producto: ProductoEntidad) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val response = api.actualizar(producto.id, producto)
                if (response.isSuccessful && response.body() != null) {

                    val actualizado = response.body()!!
                    repo.actualizar(actualizado)

                } else {
                    _state.update { it.copy(error = "Error al actualizar: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al actualizar producto.") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // 🔵 ELIMINAR producto (DELETE)
    fun eliminarProducto(producto: ProductoEntidad) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val response = api.eliminar(producto.id)
                if (response.isSuccessful) {

                    repo.eliminar(producto)

                } else {
                    _state.update { it.copy(error = "Error al eliminar: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al eliminar producto.") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun limpiarError() {
        _state.update { it.copy(error = null) }
    }
}
