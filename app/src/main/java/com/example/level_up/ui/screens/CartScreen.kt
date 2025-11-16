package com.example.level_up.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.Entidades.CarritoEntidad
import com.example.level_up.ui.obtenerImagenProducto
import com.example.level_up.viewmodel.CartViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(nav: NavController, viewModel: CartViewModel = viewModel()) {

    val items by viewModel.items.collectAsState()
    val subtotal by viewModel.subtotal.collectAsState()
    val porcentajeDescuento by viewModel.discountPct.collectAsState()
    val montoDescuento by viewModel.discountAmount.collectAsState()
    val totalFinal by viewModel.finalTotal.collectAsState()
    val estado by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {

                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // Comprobamos si el carrito está vacío
        if (items.isEmpty()) {
            PantallaCarritoVacio(nav = nav, padding = padding)
        } else {
            // Pantalla con productos
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Lista de productos
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        TarjetaItemCarrito(
                            item = item,
                            enCantidadCambiada = { nuevaCantidad ->
                                viewModel.updateQuantity(item, nuevaCantidad)
                            },
                            enEliminar = { viewModel.removeById(item.id) }
                        )
                    }
                }

                // Resumen de la compra
                ResumenDeCompra(
                    subtotal = subtotal,
                    porcentajeDescuento = porcentajeDescuento,
                    montoDescuento = montoDescuento,
                    totalFinal = totalFinal,
                    estado = estado,
                    enProcesarPedido = { viewModel.processOrder() }
                )
            }
        }
    }
}


@Composable
fun TarjetaItemCarrito(
    item: CarritoEntidad,
    enCantidadCambiada: (Int) -> Unit,
    enEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            Image(
                painter = obtenerImagenProducto(codigoProducto = item.codigoProducto),
                contentDescription = item.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )

            Spacer(Modifier.width(12.dp))

            // Nombre y Precio
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$${item.precio * item.cantidad}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.width(8.dp))

            // Controles
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ControlesCantidad(
                    cantidad = item.cantidad,
                    enCantidadCambiada = enCantidadCambiada
                )
                Spacer(Modifier.height(8.dp))
                IconButton(onClick = enEliminar, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
fun ControlesCantidad(
    cantidad: Int,
    enCantidadCambiada: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Botón Restar
        IconButton(
            onClick = { enCantidadCambiada(cantidad - 1) },
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Disminuir", modifier = Modifier.size(20.dp))
        }

        Text(
            text = "$cantidad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center
        )

        // Botón Sumar
        IconButton(
            onClick = { enCantidadCambiada(cantidad + 1) },
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Aumentar",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
fun ResumenDeCompra(
    subtotal: Int,
    porcentajeDescuento: Int,
    montoDescuento: Int,
    totalFinal: Int,
    estado: com.example.level_up.viewmodel.CartState,
    enProcesarPedido: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Resumen del Pedido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            FilaResumen(texto = "Subtotal", valor = "$$subtotal")
            FilaResumen(
                texto = "Descuento ($porcentajeDescuento%)",
                valor = "-$$montoDescuento",
                colorValor = MaterialTheme.colorScheme.primary
            )

            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

            FilaResumen(
                texto = "Total",
                valor = "$$totalFinal",
                esBold = true
            )

            Spacer(Modifier.height(12.dp))

            // Botón Pagar
            Button(
                onClick = enProcesarPedido,
                enabled = !estado.isProcessingOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    if (estado.isProcessingOrder) "Procesando..." else "Pagar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mensajes de Error o Éxito
            if (estado.error != null) {
                Text(
                    estado.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (estado.orderSuccess) {
                Text(
                    "¡Compra realizada con éxito!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun FilaResumen(
    texto: String,
    valor: String,
    esBold: Boolean = false,
    colorValor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = if (esBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (esBold) FontWeight.Bold else FontWeight.Normal,
            color = if (esBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = if (esBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,

            fontWeight = if (esBold) FontWeight.Bold else FontWeight.Medium,
            color = colorValor
        )
    }
}

/**
 * Pantalla que se muestra cuando el carrito está vacío.
 */
@Composable
fun PantallaCarritoVacio(nav: NavController, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            Icon(
                imageVector = Icons.Filled.RemoveShoppingCart,
                contentDescription = "Carrito vacío",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = "Tu carrito está vacío",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Parece que aún no has agregado productos. ¡Explora nuestro catálogo para empezar!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { nav.navigate(Routes.CATALOG) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir al Catálogo")
            }
        }
    }
}