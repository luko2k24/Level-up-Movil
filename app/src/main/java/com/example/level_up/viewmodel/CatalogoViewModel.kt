package com.example.level_up.viewmodel // <-- ¡¡ESTA LÍNEA ESTÁ CORREGIDA!!

// (El resto de las importaciones están bien)
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.CarritoEntidad
import com.example.level_up.local.ProductoEntidad
import com.example.level_up.repository.CarritoRepository
import com.example.level_up.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.String

// --- Estado del Catálogo (traducido) ---
data class EstadoCatalogo(
    val textoBusqueda: String = "",
    val categoriaSeleccionada: String = "Todas",
    val estaCargando: Boolean = false,
    val error: String? = null
)

// --- ViewModel (traducido) ---
class CatalogoViewModel(app: Application) : AndroidViewModel(app) {
    private val db = BaseDeDatosApp.obtener(app)
    private val repoProducto = ProductoRepository(db.ProductoDao())
    private val repoCarrito = CarritoRepository(db.CarritoDao())

    private val _estado = MutableStateFlow(EstadoCatalogo())
    val estado: StateFlow<EstadoCatalogo> = _estado.asStateFlow()

    // --- Flujos de datos (traducidos) ---
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
            // Si la base de datos está vacía, la poblamos
            if (repoProducto.contar() == 0) {
                inicializarProductos()
            }
        }
    }

    // --- Funciones (traducidas) ---
    private suspend fun inicializarProductos() {
        // (El resto del archivo no cambia y está correcto)
        val productosDeEjemplo = listOf(
            ProductoEntidad(
                codigo = "JM001",
                categoria = "Juegos de Mesa",
                nombre = "Catan",
                precio = 29990,
                stock = 8,
                descripcion = "Un clásico juego de estrategia donde los jugadores compiten por colonizar y expandirse en la isla de Catan. Ideal para 3-4 jugadores.",
                fabricante = "Catan Studio",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "JM002",
                categoria = "Juegos de Mesa",
                nombre = "Carcassonne",
                precio = 24990,
                stock = 6,
                descripcion = "Un juego de colocación de fichas donde los jugadores construyen el paisaje alrededor de la fortaleza medieval de Carcassonne.",
                fabricante ="Z-Man Games",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "AC001",
                categoria = "Accesorios",
                nombre = "Controlador Inalámbrico Xbox Series X",
                precio = 59990,
                stock = 12,
                descripcion = "Ofrece una experiencia de juego cómoda con botones mapeables y una respuesta táctil mejorada.",
                fabricante = "Microsoft",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "AC002",
                categoria = "Accesorios",
                nombre = "Auriculares Gamer HyperX Cloud II",
                precio = 79990,
                stock = 6,
                descripcion = "Proporcionan un sonido envolvente de calidad con un micrófono desmontable y almohadillas de espuma viscoelástica.",
                fabricante = "HyperX",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "CO001",
                categoria = "Consolas",
                nombre = "PlayStation 5",
                precio = 549990,
                stock = 5,
                descripcion = "La consola de última generación de Sony, que ofrece gráficos impresionantes y tiempos de carga ultrarrápidos.",
                fabricante = "Sony",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "CG001",
                categoria = "Computadores Gamers",
                nombre = "PC Gamer ASUS ROG Strix",
                precio = 1299990,
                stock = 3,
                descripcion = "Un potente equipo diseñado para los gamers más exigentes, equipado con los últimos componentes.",
                fabricante = "ASUS",
                destacado = true
            ),
            ProductoEntidad(
                codigo = "SG001",
                categoria = "Sillas Gamers",
                nombre = "Silla Gamer Secretlab Titan",
                precio = 349990,
                stock = 4,
                descripcion = "Diseñada para el máximo confort, esta silla ofrece un soporte ergonómico y personalización ajustable.",
                fabricante = "Secretlab",
                destacado = false
            ),
            ProductoEntidad(
                codigo = "MS001",
                categoria = "Mouse",
                nombre = "Mouse Gamer Logitech G502 HERO",
                precio = 49990,
                stock = 10,
                descripcion = "Con sensor de alta precisión y botones personalizables, este mouse es ideal para gamers que buscan control preciso.",
                fabricante = "Logitech",
                destacado = false
            ),
            ProductoEntidad(
                codigo = "MP001",
                categoria = "Mousepad",
                nombre = "Mousepad Razer Goliathus Extended Chroma",
                precio = 29990,
                stock = 8,
                descripcion = "Ofrece un área de juego amplia con iluminación RGB personalizable, asegurando una superficie suave y uniforme.",
                fabricante = "Razer",
                destacado = false
            ),
            ProductoEntidad(
                codigo = "PP001",
                categoria = "Poleras Personalizadas",
                nombre = "Polera Gamer Personalizada 'Level-Up'",
                precio = 14990,
                stock = 20,
                descripcion = "Una camiseta cómoda y estilizada, con la posibilidad de personalizarla con tu gamer tag o diseño favorito.",
                fabricante = "Level-Up Gamer",
                destacado = false
            )
        )
        repoProducto.insertarTodos(*productosDeEjemplo.toTypedArray())
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