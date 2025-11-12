package com.example.level_up.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.local.ProductoEntidad
import com.example.level_up.ui.obtenerImagenProducto
import com.example.level_up.viewmodel.ReviewViewModel
import com.example.level_up.local.ReseniaEntidad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleProducto(
    nav: NavController,
    productId: Int, // El ID pasado por la navegación
    viewModel: ReviewViewModel = viewModel()
) {
    val estado by viewModel.state.collectAsState()
    val producto = estado.product

    // [CARGA DE DATOS] Llama al ViewModel para cargar el producto y las reseñas al iniciar
    LaunchedEffect(productId) {
        viewModel.loadProductReviews(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producto?.nombre ?: "Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (estado.isLoading || producto == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- SECCIÓN DE DETALLE DE PRODUCTO ---
                item {
                    DetalleProductoUI(producto, estado.averageRating, estado.reviewCount)
                }

                item { Divider(Modifier.padding(vertical = 8.dp)) }

                // --- FORMULARIO PARA ENVIAR RESEÑA ---
                item {
                    TarjetaFormularioResena(viewModel)
                }

                item { Divider(Modifier.padding(vertical = 8.dp)) }

                // --- LISTA DE RESEÑAS ---
                item {
                    Text("Comentarios de Usuarios", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                if (estado.reviews.isEmpty()) {
                    item { Text("Aún no hay reseñas para este producto.") }
                } else {
                    items(estado.reviews) { resena ->
                        TarjetaResena(resena)
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun DetalleProductoUI(producto: ProductoEntidad, averageRating: Float, reviewCount: Int) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Image(
            painter = obtenerImagenProducto(codigoProducto = producto.codigo),
            contentDescription = producto.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = producto.nombre,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = "Valoración", tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${String.format("%.1f", averageRating)}/5.0 (${reviewCount} reseñas)",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = producto.descripcion,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Fabricante: ${producto.fabricante}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Precio: $${producto.precio}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}


@Composable
fun TarjetaFormularioResena(viewModel: ReviewViewModel) {
    val estado by viewModel.state.collectAsState()
    var rating by remember { mutableStateOf(5f) }
    var comentario by remember { mutableStateOf("") }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Deja tu Opinión", style = MaterialTheme.typography.titleLarge)

            // Selector de Valoración (Simplificado)
            Text("Tu Valoración: ${String.format("%.1f", rating)} / 5")
            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 0f..5f,
                steps = 4
            )

            // Campo de Comentario
            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario (min 10 caracteres)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Botón Enviar
            Button(
                onClick = { viewModel.submitReview(rating, comentario) },
                enabled = !estado.isSubmitting && estado.currentUser != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.isSubmitting) "Enviando..." else "Enviar Reseña")
            }

            // Mensajes
            if (estado.error != null) {
                Text(estado.error!!, color = MaterialTheme.colorScheme.error)
                DisposableEffect(estado.error) {
                    onDispose { viewModel.clearError() }
                }
            }
            if (estado.submitSuccess) {
                Text("Reseña enviada con éxito.", color = MaterialTheme.colorScheme.primary)
                DisposableEffect(estado.submitSuccess) {
                    onDispose {
                        viewModel.clearSubmitSuccess()
                        comentario = "" // Limpia el campo
                        rating = 5f // Resetea la valoración
                    }
                }
            }
            if (estado.currentUser == null) {
                Text("Debes iniciar sesión para dejar una reseña.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TarjetaResena(resena: ReseniaEntidad) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(resena.nombreUsuario, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text("${String.format("%.1f", resena.valoracion)} / 5", style = MaterialTheme.typography.bodyMedium)
            }
            Text(resena.comentario, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(resena.fechaCreacion))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}