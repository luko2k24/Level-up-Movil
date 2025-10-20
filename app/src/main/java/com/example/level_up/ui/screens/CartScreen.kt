package com.example.level_up.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.viewmodel.CartViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(nav: NavController, vm: CartViewModel = viewModel()) {
    val items by vm.items.collectAsState()
    val subtotal by vm.subtotal.collectAsState()
    val discountPct by vm.discountPct.collectAsState()
    val discountAmount by vm.discountAmount.collectAsState()
    val finalTotal by vm.finalTotal.collectAsState()
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Carrito") }) }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            // Lista de ítems
            LazyColumn(Modifier.weight(1f)) {
                items(items, key = { it.id }) { it ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(it.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "x${it.cantidad} · $${it.precio * it.cantidad}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vm.updateQuantity(it, it.cantidad - 1) }) {
                                Icon(Icons.Filled.Remove, contentDescription = "Disminuir")
                            }
                            Text("${it.cantidad}", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { vm.updateQuantity(it, it.cantidad + 1) }) {
                                Icon(Icons.Filled.Add, contentDescription = "Aumentar")
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { vm.removeById(it.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Totales (Subtotal, Descuento, Total final)
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                    Text("$${subtotal}", style = MaterialTheme.typography.bodyLarge)
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Descuento (${discountPct}%)", style = MaterialTheme.typography.bodyLarge)
                    Text("-$${discountAmount}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Divider(Modifier.padding(vertical = 6.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleLarge)
                    Text("$${finalTotal}", style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botón Pagar
            Button(
                onClick = { vm.processOrder() },
                enabled = items.isNotEmpty() && !state.isProcessingOrder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isProcessingOrder) "Procesando..." else "Pagar")
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            if (state.orderSuccess) {
                Spacer(Modifier.height(8.dp))
                Text("¡Compra realizada con éxito!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
