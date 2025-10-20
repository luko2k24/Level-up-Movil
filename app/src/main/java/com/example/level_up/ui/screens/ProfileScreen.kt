package com.example.level_up.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Versión autocontenida de ProfileScreen para compilar sin ViewModels ni servicios externos.
 * Puedes sustituir la data de UiUser por la de tu capa de datos (Room/SharedPrefs) cuando quieras.
 */

data class UiUser(
    val name: String = "Invitado",
    val email: String = "invitado@example.com",
    val level: Int = 1,
    val levelUpPoints: Int = 120,
    val totalPurchases: Int = 3,
    val isDuoc: Boolean = false
)

private fun nextLevelInfo(currentLevel: Int, currentPoints: Int): Pair<Int, Int> {

    val nextLevel = currentLevel + 1
    val required = nextLevel * 250
    val missing = (required - currentPoints).coerceAtLeast(0)
    return nextLevel to missing
}

private fun discountFor(user: UiUser): Int {
    // 20% si es DUOC; si no, por nivel (5% básico + 2% por nivel, tope 20%)
    return if (user.isDuoc) 20 else (5 + user.level * 2).coerceIn(0, 20)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavController) {

    // --- USER MOCK (reemplaza con tu data real cuando conectes Auth/Room)
    var currentUser by remember {
        mutableStateOf(
            UiUser(
                name = "Gamer Pro",
                email = "gamer@duocuc.cl", // prueba DUOC para ver el 20%
                level = 3,
                levelUpPoints = 420,
                totalPurchases = 9,
                isDuoc = true
            )
        )
    }


    var locationPermissionGranted by remember { mutableStateOf(false) }
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf("Permiso denegado") }

    val requestLocation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
        currentLocation = if (granted) "Ubicación habilitada" else "Permiso denegado"
    }

    val requestCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Header
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(600))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = currentUser.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentUser.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))

                    // Level badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Level",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Nivel ${currentUser.level}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Recursos del dispositivo (permisos)
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(20.dp)) {

                    Text(
                        text = "Recursos del Dispositivo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    // Ubicación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = if (locationPermissionGranted)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Ubicación",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currentLocation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { if (!locationPermissionGranted) requestLocation.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                            enabled = !locationPermissionGranted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (locationPermissionGranted)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(if (locationPermissionGranted) "✓" else "Permitir")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Cámara
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Camera,
                                contentDescription = "Camera",
                                tint = if (cameraPermissionGranted)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Cámara",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (cameraPermissionGranted) "Acceso permitido" else "Permiso denegado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { if (!cameraPermissionGranted) requestCamera.launch(Manifest.permission.CAMERA) },
                            enabled = !cameraPermissionGranted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cameraPermissionGranted)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(if (cameraPermissionGranted) "✓" else "Permitir")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Estadísticas del usuario
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "Estadísticas del Usuario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    val lvlInfo = nextLevelInfo(currentUser.level, currentUser.levelUpPoints)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(
                            icon = Icons.Filled.Star,
                            title = "Puntos Level-Up",
                            value = "${currentUser.levelUpPoints}",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            icon = Icons.Filled.ShoppingCart,
                            title = "Compras",
                            value = "${currentUser.totalPurchases}",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        StatCard(
                            icon = Icons.Filled.LocalOffer,
                            title = "Descuento",
                            value = "${discountFor(currentUser)}%",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Progreso al siguiente nivel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { 0.6f }, // placeholder
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Faltan ${lvlInfo.second} puntos para el nivel ${lvlInfo.first}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Acciones
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(600, delayMillis = 600))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Si quieres simular logout/login cambia currentUser = null o a un user invitado
                Button(
                    onClick = {
                        // Simulación de logout: vuelve a invitado
                        currentUser = UiUser()
                        nav.navigate(Routes.HOME)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { nav.navigate(Routes.HOME) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Volver al Inicio")
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
