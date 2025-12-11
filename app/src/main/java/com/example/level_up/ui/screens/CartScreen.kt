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
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            PantallaCarritoVacio(nav, padding)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
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

                ResumenDeCompra(
                    subtotal = subtotal,
                    porcentajeDescuento = porcentajeDescuento,
                    montoDescuento = montoDescuento,
                    totalFinal = totalFinal,
                    estado = estado,
                    enPagar = { nav.navigate("pantalla_datos_pago/$totalFinal") },
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
            Image(
                painter = obtenerImagenProducto(item.codigoProducto),
                contentDescription = item.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$${item.precio * item.cantidad}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ControlesCantidad(item.cantidad, enCantidadCambiada)

                Spacer(Modifier.height(8.dp))

                IconButton(onClick = enEliminar) {
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

        IconButton(
            onClick = { enCantidadCambiada(cantidad - 1) },
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Disminuir")
        }

        Text(
            text = cantidad.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { enCantidadCambiada(cantidad + 1) },
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Aumentar",
                tint = MaterialTheme.colorScheme.onPrimary
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
    enPagar: () -> Unit,
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
        ) {

            Text("Resumen del Pedido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            FilaResumen("Subtotal", "$$subtotal")
            FilaResumen("Descuento ($porcentajeDescuento%)", "-$$montoDescuento")

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            FilaResumen("Total", "$$totalFinal", esBold = true)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = enPagar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Pagar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilaResumen(
    texto: String,
    valor: String,
    esBold: Boolean = false
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(texto)
        Text(valor, fontWeight = if (esBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun PantallaCarritoVacio(nav: NavController, padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(
                imageVector = Icons.Filled.RemoveShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )

            Text("Tu carrito está vacío", fontWeight = FontWeight.Bold)

            Button(onClick = { nav.navigate(Routes.CATALOG) }) {
                Text("Ir al Catálogo")
            }
        }
    }
}
