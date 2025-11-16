package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.api.ApiClient // <<< Importado para Retrofit
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.Entidades.CarritoEntidad
import com.example.level_up.Entidades.ProductoEntidad
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException // <<< Importado para manejo de errores de red
import kotlin.String

// --- Estado del Catálogo
data class EstadoCatalogo(
    val textoBusqueda: String = "",
    val categoriaSeleccionada: String = "Todas",
    val estaCargando: Boolean = false,
    val error: String? = null
)

// --- ViewModel
class CatalogoViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val repoProducto = ProductoRepository(db.ProductoDao())
    private val repoCarrito = CarritoRepository(db.CarritoDao())

    private val _estado = MutableStateFlow(EstadoCatalogo())
    val estado: StateFlow<EstadoCatalogo> = _estado.asStateFlow()

    // --- Flujos de datos
    val categorias = repoProducto.obtenerCategorias().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val productos = combine(
        repoProducto.observarTodos(),
        _estado
    ) { productos, estado ->
        productos.filter { producto ->
            val coincideCategoria = estado.categoriaSeleccionada == "Todas" || producto.categoria == estado.categoriaSeleccionada
            val coincideBusqueda = estado.textoBusqueda.isBlank() ||
                    producto.nombre.contains(estado.textoBusqueda, ignoreCase = true) ||
                    producto.descripcion.contains(estado.textoBusqueda, ignoreCase = true)
            coincideCategoria && coincideBusqueda
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val productosDestacados = repoProducto.obtenerDestacados().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    init {
        viewModelScope.launch {
            // Lógica de carga de datos desde el backend
            cargarProductosDesdeAPI()
        }
    }

    private suspend fun cargarProductosDesdeAPI() {
        _estado.value = _estado.value.copy(estaCargando = true)
        try {
            // *** LLAMADA A LA API DE PRODUCTOS ***
            val response = ApiClient.productoService.listarTodos()

            if (response.isSuccessful && response.body() != null) {
                val productosRemotos = response.body()!!

                // 1. Limpiar caché local
                repoProducto.eliminarTodos()
                // 2. Insertar datos de la API en Room (caché)
                repoProducto.insertarTodos(*productosRemotos.toTypedArray())
            } else {
                _estado.value = _estado.value.copy(error = "Error al obtener productos: ${response.code()}")
            }
        } catch (e: Exception) {
            val msg = when (e) {
                is HttpException -> "Error HTTP: ${e.code()}. ¿Backend corriendo en 8081?"
                else -> "Error de red. Asegúrate de que el backend de Productos (8081) esté corriendo."
            }
            _estado.value = _estado.value.copy(error = msg)
        } finally {
            _estado.value = _estado.value.copy(estaCargando = false)
        }
    }


    fun actualizarBusqueda(texto: String) {
        _estado.value = _estado.value.copy(textoBusqueda = texto)
    }

    fun actualizarCategoria(categoria: String) {
        _estado.value = _estado.value.copy(categoriaSeleccionada = categoria)
    }

    fun agregarAlCarrito(producto: ProductoEntidad) {
        viewModelScope.launch {
            try {
                val itemExistente = repoCarrito.obtenerItemPorProductoId(producto.id)
                if (itemExistente != null) {
                    // Si ya existe, solo suma 1 a la cantidad
                    repoCarrito.actualizarCantidad(producto.id, itemExistente.cantidad + 1)
                } else {
                    // Si no existe, crea un nuevo item
                    repoCarrito.insertarOActualizar(
                        CarritoEntidad(
                            productoId = producto.id,
                            codigoProducto = producto.codigo,
                            nombre = producto.nombre,
                            precio = producto.precio,
                            cantidad = 1
                        )
                    )
                }
            } catch (e: Exception) {
                _estado.value = _estado.value.copy(error = "Error al agregar al carrito: ${e.message}")
            }
        }
    }

    fun limpiarError() {
        _estado.value = _estado.value.copy(error = null)
    }
}