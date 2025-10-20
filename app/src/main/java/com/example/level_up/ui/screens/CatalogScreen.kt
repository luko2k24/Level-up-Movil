package com.example.level_up.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.levelup.mobile.viewmodel.CatalogViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CatalogScreen(nav: NavController, vm: CatalogViewModel = viewModel()) {
    val products by vm.products.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Catálogo")
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(products) { p ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { }) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${p.nombre} - ${p.categoria}")
                        Text("Precio: $${p.precio}")
                        Spacer(Modifier.height(6.dp))
                        Button(onClick = { vm.addToCart(p) }) {
                            Text("Agregar al carrito")
                        }
                    }
                }
            }
        }
    }
}