package com.example.level_up.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavController? = null
) {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var verClave by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icono = if (verClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { verClave = !verClave }) { Icon(icono, contentDescription = null) }
                    }
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        // TODO: lógica de login
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ingresar")
                }

                TextButton(
                    onClick = { /* TODO: ir a Registro */ },
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Crear cuenta") }
            }
        }
    }
}
