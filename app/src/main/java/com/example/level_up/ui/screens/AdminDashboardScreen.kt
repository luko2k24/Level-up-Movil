package com.example.level_up.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.Entidades.ProductoEntidad

import com.example.level_up.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(nav: NavController, viewModel: AdminViewModel = viewModel()) {

    val estado by viewModel.state.collectAsState()
    val productos by viewModel.productos.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Panel de Administración",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(onClick = { nav.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }) {
                            Icon(Icons.Filled.Home, contentDescription = "Inicio", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                )
                // --- BARRA DE ACCESO RÁPIDO PARA ADMIN
                FilaAccesoRapidoAdmin(nav)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    "Gestión de Productos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
            }

            // --- BOTÓN AGREGAR
            item {
                Button(
                    onClick = { /* TODO: Implementar navegación a formulario de AGREGAR */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("➕ Agregar Nuevo Producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text(
                    "Productos Existentes (${productos.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp)
                )

                // Mostrar estado de carga o error
                when {
                    estado.isLoading -> CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(16.dp))
                    estado.error != null -> Text(estado.error!!, color = MaterialTheme.colorScheme.error)
                    productos.isEmpty() -> Text("No hay productos cargados en la base de datos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // --- LISTA DE PRODUCTOS
            items(productos, key = { it.id }) { producto ->
                TarjetaProductoAdmin(producto = producto)
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

// --- Componente de Acceso Rápido para Admin ---
@Composable
private fun FilaAccesoRapidoAdmin(nav: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AccionChip(nav, Routes.CATALOG, "Catálogo", Icons.Default.ShoppingBag)
        AccionChip(nav, Routes.CART, "Carrito", Icons.Default.ShoppingCart)
        AccionChip(nav, Routes.PROFILE, "Mi Perfil", Icons.Default.Person)
    }
}

@Composable
private fun AccionChip(nav: NavController, route: String, label: String, icon: ImageVector) {
    AssistChip(
        onClick = { nav.navigate(route) },
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// --- Componente de Tarjeta para la Lista de Productos ---
@Composable
private fun TarjetaProductoAdmin(producto: ProductoEntidad) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Medium, maxLines = 1)
                Text("ID: ${producto.id} | Código: ${producto.codigo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Stock: ${producto.stock} | Precio: $${producto.precio}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón Editar (Electric Blue)
                Button(
                    onClick = { /* TODO: Lógica de UPDATE */ },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Editar") }
                // Botón Eliminar (Rojo/Error)
                Button(
                    onClick = { /* TODO: Lógica de DELETE */ },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            }
        }
    }
}