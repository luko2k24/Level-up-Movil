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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.level_up.services.recordarServicioRecursosNativos
import com.example.level_up.viewmodel.ProfileViewModel
// [IMPORT NECESARIO PARA PEDIDOS]
import com.example.level_up.local.PedidoEntidad
import androidx.compose.animation.slideOutHorizontally // [IMPORT NECESARIO PARA ANIMACIÓN]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(nav: NavController, viewModel: ProfileViewModel = viewModel()) {

    val estado by viewModel.state.collectAsState()
    val usuario = estado.currentUser // El usuario real que inició sesión

    // --- Lógica de Permisos ---
    val servicioNativo = recordarServicioRecursosNativos()

    // Estado para saber si los permisos están dados
    var tienePermisoCamara by remember { mutableStateOf(servicioNativo.tienePermisoCamara()) }
    var tienePermisoUbicacion by remember { mutableStateOf(servicioNativo.tienePermisoUbicacion()) }

    // Estado para mostrar la ubicación real
    var textoUbicacion by remember { mutableStateOf("Permiso denegado") }

    // Lanzador para pedir permiso de CÁMARA
    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { fueConcedido ->
        tienePermisoCamara = fueConcedido
    }

    // Lanzador para pedir permiso de UBICACIÓN
    val lanzadorPermisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { fueConcedido ->
        tienePermisoUbicacion = fueConcedido
        if (fueConcedido) {
            // Si nos dan permiso, pedimos la ubicación
            servicioNativo.obtenerUbicacionActual { resultado ->
                textoUbicacion = resultado
            }
        } else {
            textoUbicacion = "Permiso denegado"
        }
    }

    // Actualiza el texto de la ubicación si ya teníamos permiso al entrar
    LaunchedEffect(tienePermisoUbicacion) {
        if (tienePermisoUbicacion) {
            servicioNativo.obtenerUbicacionActual { resultado ->
                textoUbicacion = resultado
            }
        }
    }

    // Estado para las animaciones
    var esVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { esVisible = true }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // --- 1. Cabecera del Perfil ---
        item {
            CabeceraPerfil(
                visible = esVisible,
                nombre = usuario?.nombre ?: "Invitado",
                email = usuario?.correo ?: "invitado@level-up.cl",
                nivel = usuario?.nivel ?: 1
            )
        }

        // --- 2. Recursos del Dispositivo (Permisos Nativos) ---
        item {
            TarjetaRecursosNativos(
                visible = esVisible,
                tienePermisoCamara = tienePermisoCamara,
                tienePermisoUbicacion = tienePermisoUbicacion,
                textoUbicacion = textoUbicacion,
                enPedirCamara = { lanzadorPermisoCamara.launch(Manifest.permission.CAMERA) },
                enPedirUbicacion = { lanzadorPermisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            )
        }

        // --- 3. Estadísticas del Usuario ---
        if (usuario != null) {
            item {
                TarjetaEstadisticas(
                    visible = esVisible,
                    puntos = usuario.puntosLevelUp,
                    compras = usuario.totalCompras,
                    descuento = viewModel.getDiscountPercentage(),
                    infoNivel = viewModel.getUserLevelInfo()
                )
            }
            // --- [NUEVA SECCIÓN DE PEDIDOS] ---
            item {
                TarjetaPedidos(
                    visible = esVisible,
                    pedidos = estado.userOrders,
                    totalGastado = estado.totalSpent
                )
            }
        }

        // --- 4. Acciones (Cerrar Sesión) ---
        item {
            AccionesPerfil(
                visible = esVisible,
                enCerrarSesion = {
                    viewModel.logout()
                    nav.navigate(Routes.HOME) { // Vuelve al Home limpiando la pila
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                enVolverInicio = { nav.navigate(Routes.HOME) }
            )
        }
    }
}

// --- Componentes Refactorizados ---

@Composable
private fun CabeceraPerfil(visible: Boolean, nombre: String, email: String, nivel: Int) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(600))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary, // Azul
                contentColor = MaterialTheme.colorScheme.onSecondary // Blanco
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.2f)), // Avatar con fondo oscuro
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(Modifier.height(16.dp))

                Text(
                    text = nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                // Etiqueta de Nivel
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Nivel",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary // Estrella Verde Neón
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Nivel $nivel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRecursosNativos(
    visible: Boolean,
    tienePermisoCamara: Boolean,
    tienePermisoUbicacion: Boolean,
    textoUbicacion: String,
    enPedirCamara: () -> Unit,
    enPedirUbicacion: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Text(
                    text = "Recursos del Dispositivo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Fila de Ubicación
                FilaPermiso(
                    titulo = "Ubicación",
                    subtitulo = textoUbicacion,
                    icono = Icons.Filled.LocationOn,
                    concedido = tienePermisoUbicacion,
                    enPedirPermiso = enPedirUbicacion
                )

                // Fila de Cámara
                FilaPermiso(
                    titulo = "Cámara",
                    subtitulo = if (tienePermisoCamara) "Acceso permitido" else "Permiso denegado",
                    icono = Icons.Filled.Camera,
                    concedido = tienePermisoCamara,
                    enPedirPermiso = enPedirCamara
                )
            }
        }
    }
}

@Composable
private fun FilaPermiso(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    concedido: Boolean,
    enPedirPermiso: () -> Unit
) {
    val colorIcono = if (concedido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = colorIcono,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Button(
            onClick = enPedirPermiso,
            enabled = !concedido,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorIcono,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            Text(if (concedido) "✓" else "Permitir")
        }
    }
}

@Composable
private fun TarjetaEstadisticas(
    visible: Boolean,
    puntos: Int,
    compras: Int,
    descuento: Int,
    infoNivel: Pair<Int, Int> // (Nivel Actual, Puntos Faltantes)
) {
    AnimatedVisibility(
        visible = visible,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TarjetaStat(
                        icono = Icons.Filled.Star,
                        titulo = "Puntos",
                        valor = "$puntos",
                        color = MaterialTheme.colorScheme.primary
                    )
                    TarjetaStat(
                        icono = Icons.Filled.ShoppingCart,
                        titulo = "Compras",
                        valor = "$compras",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    TarjetaStat(
                        icono = Icons.Filled.LocalOffer,
                        titulo = "Descuento",
                        valor = "$descuento%",
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

                // TODO: Calcular el progreso real
                LinearProgressIndicator(
                    progress = { 0.6f }, // Placeholder
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Faltan ${infoNivel.second} puntos para el nivel ${infoNivel.first + 1}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// [INICIO DE LAS FUNCIONES QUE FALTABAN]

// --- [NUEVOS COMPONENTES DE PEDIDOS] ---

@Composable
private fun TarjetaPedidos(visible: Boolean, pedidos: List<com.example.level_up.local.PedidoEntidad>, totalGastado: Int) {
    // Usamos AnimatedVisibility para que el contenido aparezca con un efecto
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it }, // Viene desde la derecha
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(600, delayMillis = 600)),
        exit = androidx.compose.animation.slideOutHorizontally()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Historial de Pedidos (${pedidos.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total gastado en Level-Up: $$totalGastado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Divider()

                if (pedidos.isEmpty()) {
                    Text("Aún no tienes pedidos registrados.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    pedidos.take(3).forEach { pedido -> // Mostrar solo los 3 más recientes
                        FilaPedido(pedido)
                        // Lógica para no poner el divisor después del último elemento mostrado
                        if (pedidos.indexOf(pedido) < 2 && pedidos.size > pedidos.indexOf(pedido) + 1) {
                            Divider(Modifier.height(1.dp).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.surface)
                        }
                    }
                    if (pedidos.size > 3) {
                        TextButton(onClick = { /* TODO: Navegar a Pantalla de Historial Completo */ }) {
                            Text("Ver todos los ${pedidos.size} pedidos")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaPedido(pedido: com.example.level_up.local.PedidoEntidad) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Pedido #${pedido.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Total final: $${pedido.montoFinal}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(pedido.fechaCreacion))
        Text(
            text = fecha,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// [FIN DE LAS FUNCIONES QUE FALTABAN]

@Composable
private fun AccionesPerfil(
    visible: Boolean,
    enCerrarSesion: () -> Unit,
    enVolverInicio: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(600, delayMillis = 600))
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = enCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error // Botón rojo
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }

            OutlinedButton(
                onClick = enVolverInicio,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Volver al Inicio")
            }
        }
    }
}

@Composable
fun RowScope.TarjetaStat(
    icono: ImageVector,
    titulo: String,
    valor: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = Modifier.weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}