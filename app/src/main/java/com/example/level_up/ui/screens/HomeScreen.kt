package com.example.level_up.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.levelup.mobile.viewmodel.CatalogViewModel
import com.example.level_up.viewmodel.AuthViewModel

data class AccionRapida(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val catalogViewModel: CatalogViewModel = viewModel()

    val estadoAuth by authViewModel.state.collectAsState()
    val productosDestacados by catalogViewModel.featuredProducts.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val acciones = listOf(
        AccionRapida("Catálogo", Icons.Default.ShoppingBag, Routes.CATALOG, MaterialTheme.colorScheme.primary),
        AccionRapida("Carrito", Icons.Default.ShoppingCart, Routes.CART, MaterialTheme.colorScheme.secondary),
        AccionRapida("Perfil", Icons.Default.Person, Routes.PROFILE, MaterialTheme.colorScheme.primary)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .padding(16.dp)
    ) {

        if (estadoAuth.currentUser == null) {
            AssistChip(
                onClick = { navController.navigate(Routes.AUTH) },
                label = { Text("Iniciar sesión") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) },
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopEnd)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Encabezado sin logo
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn()
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
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Level-Up Gamer",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Level-Up Gamer",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tu tienda gamer de confianza",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )

                        if (estadoAuth.currentUser != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = "¡Bienvenido, ${estadoAuth.currentUser?.nombre}!",
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Acciones rápidas
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn()
            ) {
                Column {
                    Text(
                        text = "Acciones Rápidas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(acciones) { accion ->
                            TarjetaAccionRapida(
                                accion = accion,
                                onClick = { navController.navigate(accion.ruta) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Productos destacados
            if (productosDestacados.isNotEmpty()) {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ) + fadeIn()
                ) {
                    Column {
                        Text(
                            text = "Productos Destacados",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(productosDestacados.take(3)) { producto ->
                                TarjetaProductoDestacado(
                                    producto = producto,
                                    onClick = { navController.navigate(Routes.CATALOG) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beneficios
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Por qué elegir Level-Up Gamer?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ItemBeneficio("🎮", "Productos premium", "Calidad garantizada")
                            ItemBeneficio("🚚", "Envío nacional", "A todo Chile")
                            ItemBeneficio("💎", "Descuentos DUOC", "20% permanente")
                            ItemBeneficio("⭐", "Puntos Level-Up", "Sube de nivel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaAccionRapida(
    accion: AccionRapida,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = accion.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = accion.icono,
                contentDescription = accion.titulo,
                tint = accion.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = accion.titulo,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = accion.color
            )
        }
    }
}

@Composable
fun TarjetaProductoDestacado(
    producto: cl.levelup.mobile.model.local.ProductoEntidad,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${producto.precio}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = producto.categoria,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ItemBeneficio(
    emoji: String,
    titulo: String,
    descripcion: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = descripcion,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
