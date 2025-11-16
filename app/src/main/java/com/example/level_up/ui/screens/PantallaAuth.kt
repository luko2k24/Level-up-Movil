package com.example.level_up.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.viewmodel.ViewModelAutenticacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: ViewModelAutenticacion = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    // Navegación: Cuando el estado sea "exito", navega al perfil/dashboard y limpia el estado
    LaunchedEffect(estado.exito) {
        if (estado.exito) {

            val destino = if (estado.usuarioActual?.rol == "ADMIN") { //
                Routes.ADMIN_DASHBOARD //
            } else {
                Routes.PROFILE //
            }

            navController.navigate(destino) { //

                popUpTo(Routes.HOME) { inclusive = true } //
            }
            viewModel.limpiarExito()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // <-- Ahora esto funcionará
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (estado.esModoLogin) "Iniciar Sesión" else "Crear Cuenta",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    // --- Campos de Registro
                    AnimatedVisibility(visible = !estado.esModoLogin) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CampoDeTexto(
                                valor = estado.nombre,
                                enCambio = viewModel::onNombre,
                                etiqueta = "Nombre",
                                error = estado.errores["nombre"]
                            )
                            CampoDeTexto(
                                valor = estado.edad,
                                enCambio = viewModel::onEdad,
                                etiqueta = "Edad",
                                error = estado.errores["edad"],
                                tipoTeclado = KeyboardType.Number
                            )
                        }
                    }

                    // --- Campos Comunes (Email y Clave) ---
                    CampoDeTexto(
                        valor = estado.correo,
                        enCambio = viewModel::onCorreo,
                        etiqueta = "Correo Electrónico",
                        error = estado.errores["correo"],
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoDeTextoClave(
                        valor = estado.clave,
                        enCambio = viewModel::onClave,
                        etiqueta = "Contraseña",
                        error = estado.errores["clave"]
                    )

                    // --- Campos de Registro
                    AnimatedVisibility(visible = !estado.esModoLogin) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CampoDeTextoClave(
                                valor = estado.confirmarClave,
                                enCambio = viewModel::onConfirmarClave,
                                etiqueta = "Confirmar Contraseña",
                                error = estado.errores["confirmarClave"]
                            )
                            CampoDeTexto(
                                valor = estado.codigoReferido,
                                enCambio = viewModel::onCodigoReferido,
                                etiqueta = "Código de Referido (Opcional)",
                                error = estado.errores["codigoReferido"]
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // --- Botón Principal (Login o Registro) ---
                    if (estado.estaCargando) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                if (estado.esModoLogin) {
                                    viewModel.iniciarSesion()
                                } else {
                                    viewModel.registrar()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (estado.esModoLogin) "Ingresar" else "Registrarme")
                        }
                    }

                    // --- Botón para cambiar de modo ---
                    TextButton(
                        onClick = viewModel::cambiarModo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (estado.esModoLogin) "¿No tienes cuenta? Regístrate" else "Ya tengo cuenta. Iniciar Sesión")
                    }

                    // --- Error General ---
                    if (estado.errores.containsKey("general")) {
                        Text(
                            text = estado.errores["general"]!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun CampoDeTexto(
    valor: String,
    enCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = enCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CampoDeTextoClave(
    valor: String,
    enCambio: (String) -> Unit,
    etiqueta: String,
    error: String?
) {
    var verClave by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = enCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val icono = if (verClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
            IconButton(onClick = { verClave = !verClave }) {
                Icon(icono, contentDescription = "Ver/Ocultar clave")
            }
        }
    )
}