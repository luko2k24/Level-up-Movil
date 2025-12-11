package com.example.level_up.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // --- Control del diálogo (crear/editar)
    var mostrarDialogo by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<ProductoEntidad?>(null) }

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
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            nav.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Filled.Home, contentDescription = "Inicio")
                        }
                    }
                )
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

            // ---------------- BOTÓN CREAR ----------------
            item {
                Button(
                    onClick = {
                        productoEditando = null
                        mostrarDialogo = true
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("➕ Agregar Nuevo Producto")
                }
            }

            // ---- Texto estado ----
            item {
                if (estado.isLoading)
                    CircularProgressIndicator()

                if (estado.error != null)
                    Text(estado.error!!, color = MaterialTheme.colorScheme.error)

                Text(
                    "Productos Existentes (${productos.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // ---------------- LISTA DE PRODUCTOS ----------------
            items(productos, key = { it.id }) { producto ->
                TarjetaProductoAdmin(
                    producto = producto,
                    onEditar = {
                        productoEditando = producto
                        mostrarDialogo = true
                    },
                    onEliminar = {
                        viewModel.eliminarProducto(producto)
                    }
                )
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    // --- DIALOGO CRUD ---
    if (mostrarDialogo) {
        DialogoProductoAdmin(
            producto = productoEditando,
            onCancelar = { mostrarDialogo = false },
            onGuardar = { prod ->
                if (productoEditando == null)
                    viewModel.crearProducto(prod)
                else
                    viewModel.actualizarProducto(prod)

                mostrarDialogo = false
            }
        )
    }
}

// --------------------- TARJETA CON BOTONES CRUD ---------------------
@Composable
fun TarjetaProductoAdmin(
    producto: ProductoEntidad,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Medium)
                Text("Código: ${producto.codigo}")
                Text("Precio: $${producto.precio}")
                Text("Stock: ${producto.stock}")
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEditar,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Editar") }

                Button(
                    onClick = onEliminar,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            }
        }
    }
}

// ---------------- DIALOGO PARA CREAR / EDITAR ----------------
@Composable
fun DialogoProductoAdmin(
    producto: ProductoEntidad?,
    onCancelar: () -> Unit,
    onGuardar: (ProductoEntidad) -> Unit,
) {
    var codigo by remember { mutableStateOf(producto?.codigo ?: "") }
    var nombre by remember { mutableStateOf(producto?.nombre ?: "") }
    var categoria by remember { mutableStateOf(producto?.categoria ?: "") }
    var precio by remember { mutableStateOf(producto?.precio?.toString() ?: "") }
    var stock by remember { mutableStateOf(producto?.stock?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            Button(onClick = {
                onGuardar(
                    ProductoEntidad(
                        id = producto?.id ?: 0,
                        codigo = codigo,
                        nombre = nombre,
                        categoria = categoria,
                        precio = precio.toIntOrNull() ?: 0,
                        stock = stock.toIntOrNull() ?: 0
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        },
        title = { Text(if (producto == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código") })
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") })
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") })
            }
        }
    )
}

// ---- ACCESO RÁPIDO ----
@Composable
private fun FilaAccesoRapidoAdmin(nav: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = { nav.navigate(Routes.CATALOG) }, label = { Text("Catálogo") }, leadingIcon = { Icon(Icons.Default.ShoppingBag, null) })
        AssistChip(onClick = { nav.navigate(Routes.CART) }, label = { Text("Carrito") }, leadingIcon = { Icon(Icons.Default.ShoppingCart, null) })
        AssistChip(onClick = { nav.navigate(Routes.PROFILE) }, label = { Text("Mi Perfil") }, leadingIcon = { Icon(Icons.Default.Person, null) })
    }
}
