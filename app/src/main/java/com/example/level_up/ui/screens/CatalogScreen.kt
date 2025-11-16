package com.example.level_up.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.Entidades.ProductoEntidad
import com.example.level_up.viewmodel.CatalogoViewModel
import com.example.level_up.ui.obtenerImagenProducto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(nav: NavController, viewModel: CatalogoViewModel = viewModel()) { // <-- USA EL VIEWMODEL CORREGIDO
    val estado by viewModel.estado.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    val todasLasCategorias = listOf("Todas") + categorias

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = estado.textoBusqueda,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )

            Spacer(Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(todasLasCategorias) { categoria ->
                    FilterChip(
                        selected = categoria == estado.categoriaSeleccionada,
                        onClick = { viewModel.actualizarCategoria(categoria) },
                        label = { Text(categoria) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos, key = { it.id }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        alHacerClick = {
                            //  Navega a la pantalla de detalle pasando el ID del producto
                            nav.navigate("product_detail/${producto.id}")
                        },
                        alAgregarAlCarrito = { viewModel.agregarAlCarrito(producto) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaProducto(
    producto: ProductoEntidad,
    alHacerClick: () -> Unit,
    alAgregarAlCarrito: () -> Unit
) {
    Card(
        onClick = alHacerClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Image(
                painter = obtenerImagenProducto(codigoProducto = producto.codigo),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = producto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${producto.precio}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = alAgregarAlCarrito,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir al carrito",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}