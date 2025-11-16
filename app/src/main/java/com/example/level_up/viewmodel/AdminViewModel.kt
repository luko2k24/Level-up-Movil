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
    val error: String? = null

)

class AdminViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val repoProducto = ProductoRepository(db.ProductoDao())
    private val productoService = ApiClient.productoService

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    // Flujo de datos que observa los productos en la base de datos local
    val productos: StateFlow<List<ProductoEntidad>> = repoProducto.observarTodos().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        // Al iniciar, forzamos una recarga desde la API para tener datos frescos
        refreshProductsFromAPI()
    }

    // Lógica para listar y sincronizar productos desde el backend
    fun refreshProductsFromAPI() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = productoService.listarTodos()

                if (response.isSuccessful && response.body() != null) {
                    val remoteProducts = response.body()!!
                    // 1. Limpiamos y reinsertamos en Room (caché)
                    repoProducto.eliminarTodos()
                    repoProducto.insertarTodos(*remoteProducts.toTypedArray())
                } else {
                    _state.value = _state.value.copy(error = "Error al obtener productos: ${response.code()}")
                }
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Error HTTP: ${e.code()}. Microservicio de Productos (8081) inactivo."
                    else -> "Error de red. Asegúrate de que el backend de Productos (8081) esté corriendo."
                }
                _state.value = _state.value.copy(error = msg)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }


}