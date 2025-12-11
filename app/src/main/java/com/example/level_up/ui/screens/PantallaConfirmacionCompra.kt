package com.example.level_up.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PantallaConfirmacionCompra(
    nav: NavController,
    total: Int,
    nombre: String,
    correo: String,
    direccion: String
) {

    var procesando by remember { mutableStateOf(false) }
    var exito by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text("Confirmación de Compra", style = MaterialTheme.typography.headlineSmall)

        Text("Nombre: $nombre")
        Text("Correo: $correo")
        Text("Dirección: $direccion")
        Text("Total a pagar: $$total", fontWeight = FontWeight.Bold)

        Button(
            onClick = {
                procesando = true
                scope.launch {
                    delay(1500)
                    exito = true
                    procesando = false
                }
            },
            enabled = !procesando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (procesando) "Procesando..." else "Confirmar Pago")
        }

        if (exito) {
            Text(
                "¡Compra realizada con éxito!",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { nav.navigate(Routes.CATALOG) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al Catálogo")
            }
        }
    }
}
