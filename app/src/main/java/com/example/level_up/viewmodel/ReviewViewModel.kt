package com.example.level_up.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.api.ApiClient // <-- ¡AÑADIDO!
import com.example.level_up.local.BaseDeDatosApp
import com.example.level_up.local.ProductoEntidad
import com.example.level_up.local.ReseniaEntidad
import com.example.level_up.local.UsuarioEntidad
import com.example.level_up.repository.ProductoRepository
import com.example.level_up.repository.ReseniaRepository
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException // <-- ¡AÑADIDO!

data class ReviewState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val currentUser: UsuarioEntidad? = null,
    val product: ProductoEntidad? = null,
    val reviews: List<ReseniaEntidad> = emptyList(),
    val averageRating: Float = 0f,
    val reviewCount: Int = 0
)

class ReviewViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BaseDeDatosApp.obtener(app)

    // --- INYECCIÓN DE DEPENDENCIAS CORREGIDA: Se pasa el servicio de la API ---
    private val reviewRepo = ReseniaRepository(db.ReseniaDao(), ApiClient.reseniaService)
    private val productRepo = ProductoRepository(db.ProductoDao())
    private val userRepo = UsuarioRepository(db.UsuarioDao())
    // ------------------------------------------

    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentUser = userRepo.obtenerUsuarioActual()
            )
            // Agregado para precargar al iniciar
            _state.value.product?.id?.let { loadProductReviews(it) }
        }
    }

    // --- loadProductReviews (Actualizado para cargar de la API y usar el caché como fallback) ---
    fun loadProductReviews(productId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                // 1. Intenta cargar desde la API
                val apiResponse = ApiClient.reseniaService.porProducto(productId)

                if (apiResponse.isSuccessful && apiResponse.body() != null) {
                    val remoteReviews = apiResponse.body()!!

                    val averageRatingRemote = remoteReviews.map { it.valoracion }.average().toFloat()
                    val reviewCountRemote = remoteReviews.size
                    val product = productRepo.obtenerPorId(productId)

                    _state.value = _state.value.copy(
                        product = product,
                        reviews = remoteReviews,
                        averageRating = averageRatingRemote,
                        reviewCount = reviewCountRemote,
                        isLoading = false,
                        error = null
                    )
                } else {
                    // 2. Si la API falla al cargar (ej. error 500), usa el caché local
                    val product = productRepo.obtenerPorId(productId)
                    val reviewsLocal = reviewRepo.obtenerreseniaPorProducto(productId).first()
                    val averageRatingLocal = reviewRepo.obtenerPromedioResenas(productId) ?: 0f
                    val reviewCountLocal = reviewRepo.contarResenas(productId)

                    _state.value = _state.value.copy(
                        product = product,
                        reviews = reviewsLocal,
                        averageRating = averageRatingLocal,
                        reviewCount = reviewCountLocal,
                        isLoading = false,
                        error = "No se pudieron obtener las reseñas más recientes del servidor. Usando cache local."
                    )
                }
            } catch (e: Exception) {
                // 3. Si hay error de red (ej. Microservicio apagado), usa el caché local
                val product = productRepo.obtenerPorId(productId)
                val reviewsLocal = reviewRepo.obtenerreseniaPorProducto(productId).first()
                val averageRatingLocal = reviewRepo.obtenerPromedioResenas(productId) ?: 0f
                val reviewCountLocal = reviewRepo.contarResenas(productId)

                _state.value = _state.value.copy(
                    product = product,
                    reviews = reviewsLocal,
                    averageRating = averageRatingLocal,
                    reviewCount = reviewCountLocal,
                    isLoading = false,
                    error = "Error de red al cargar reseñas. Usando datos de cache local."
                )
            }
        }
    }


    // --- submitReview (CORREGIDO: Llama a la API y solo guarda localmente si es exitoso) ---
    fun submitReview(rating: Float, comment: String) {
        viewModelScope.launch {
            val user = _state.value.currentUser
            val product = _state.value.product

            // Validaciones (se mantienen igual)
            if (user == null) {
                _state.value = _state.value.copy(error = "Debes iniciar sesión para dejar una reseña")
                return@launch
            }
            if (product == null) {
                _state.value = _state.value.copy(error = "Producto no encontrado")
                return@launch
            }
            if (!Validacion.esCalificacionValida(rating)) {
                _state.value = _state.value.copy(error = "Calificación debe estar entre 0 y 5")
                return@launch
            }
            if (!Validacion.esComentarioResenaValido(comment)) {
                _state.value = _state.value.copy(error = "Comentario debe tener al menos 10 caracteres")
                return@launch
            }

            _state.value = _state.value.copy(isSubmitting = true)

            try {
                // 1. Crear entidad (MÓVIL)
                val review = ReseniaEntidad(
                    productoId = product.id,
                    usuarioId = user.id,
                    nombreUsuario = user.nombre,
                    comentario = comment.trim(),
                    valoracion = rating
                )

                // 2. LLAMADA A LA API Y GUARDADO LOCAL CONDICIONAL
                // Si la API falla, esta línea lanzará una excepción (HttpException o de red)
                val apiResponse = reviewRepo.enviarYGuardarResena(review)

                if (!apiResponse.isSuccessful) {
                    // Si el backend responde con un código de error (ej. 404, 500)
                    throw HttpException(apiResponse)
                }

                // 3. Si el backend fue exitoso, actualizamos el promedio del producto
                val nuevoPromedio = reviewRepo.obtenerPromedioResenas(product.id) ?: rating
                productRepo.actualizarValoracion(product.id, nuevoPromedio)

                // 4. Recarga reseñas en estado para refrescar la lista (usando la API)
                loadProductReviews(product.id)

                _state.value = _state.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                // 5. MANEJO DE ERROR: No se guarda localmente si el backend falla.
                val mensajeError = when (e) {
                    is HttpException -> "Error HTTP: Falló el Microservicio de Reseñas. Código ${e.code()}. Asegúrate de que esté corriendo en 8084."
                    else -> "Error de red. Asegúrate de que el microservicio de Reseñas esté activo."
                }
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = mensajeError
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearSubmitSuccess() {
        _state.value = _state.value.copy(submitSuccess = false)
    }
}