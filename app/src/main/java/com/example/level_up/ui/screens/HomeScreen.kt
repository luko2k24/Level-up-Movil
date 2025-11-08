package com.example.level_up.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
// --- CORRECCIÓN: Importar el ViewModel desde el paquete correcto ---
import com.example.level_up.viewmodel.CatalogoViewModel
import com.example.level_up.R
import com.example.level_up.ui.obtenerImagenProducto
import com.example.level_up.viewmodel.AuthViewModel
import com.example.level_up.viewmodel.AuthState

// (El resto del archivo es idéntico al que te di antes y está correcto)
private data class AccionRapida(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String,
    val color: Color
)

private data class Noticia(
    val titulo: String,
    val resumen: String,
    val icono: ImageVector,
    val colorIcono: Color
)

@Composable
private fun getAccionesRapidas(): List<AccionRapida> {
    return listOf(
        AccionRapida(
            "Catálogo",
            Icons.Default.ShoppingBag,
            Routes.CATALOG,
            MaterialTheme.colorScheme.primary
        ),
        AccionRapida(
            "Carrito",
            Icons.Default.ShoppingCart,
            Routes.CART,
            MaterialTheme.colorScheme.secondary
        ),
        AccionRapida(
            "Perfil",
            Icons.Default.Person,
            Routes.PROFILE,
            MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun getNoticias(): List<Noticia> {
    return listOf(
        Noticia(
            "¡Nuevas Consolas!",
            "Descubre lo último en hardware que ha llegado a la tienda.",
            Icons.Default.VideogameAsset,
            MaterialTheme.colorScheme.primary
        ),
        Noticia(
            "Torneo de Catan",
            "Inscríbete en el torneo nacional y gana premios.",
            Icons.Default.EmojiEvents,
            MaterialTheme.colorScheme.secondary
        )
    )
}

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    // --- CORRECCIÓN: Usar el ViewModel del paquete correcto ---
    catalogViewModel: CatalogoViewModel = viewModel()
) {
    val estadoAuth by authViewModel.state.collectAsState()
    val productosDestacados by catalogViewModel.productosDestacados.collectAsState()
    val acciones = getAccionesRapidas()
    val noticias = getNoticias()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

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
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier
                    .statusBarsPadding()
                    .height(24.dp))
            }
            item {
                Cabecera(visible = visible, authState = estadoAuth)
            }
            item {
                AccionesRapidas(
                    visible = visible,
                    acciones = acciones,
                    onAccionClick = { ruta -> navController.navigate(ruta) }
                )
            }
            if (productosDestacados.isNotEmpty()) {
                item {
                    ProductosDestacados(
                        visible = visible,
                        productos = productosDestacados.take(5),
                        onProductoClick = { navController.navigate(Routes.CATALOG) }
                    )
                }
            }
            item {
                SeccionNoticias(
                    visible = visible,
                    noticias = noticias
                )
            }
            item {
                PorQueElegirnos(visible = visible)
            }
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        AnimatedVisibility(
            visible = visible && estadoAuth.currentUser == null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp),
            enter = fadeIn(animationSpec = tween(600, delayMillis = 300))
        ) {
            AssistChip(
                onClick = { navController.navigate(Routes.AUTH) },
                label = { Text("Iniciar sesión") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun Cabecera(visible: Boolean, authState: AuthState) {
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
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo de Level-Up Gamer",
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Level-Up Gamer",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tu tienda gamer de confianza",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    textAlign = TextAlign.Center
                )
                if (authState.currentUser != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "¡Bienvenido, ${authState.currentUser.nombre}!",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccionesRapidas(
    visible: Boolean,
    acciones: List<AccionRapida>,
    onAccionClick: (String) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
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
                        onClick = { onAccionClick(accion.ruta) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductosDestacados(
    visible: Boolean,
    productos: List<cl.levelup.mobile.model.local.ProductoEntidad>,
    onProductoClick: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
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
                items(productos) { producto ->
                    TarjetaProductoDestacado(
                        producto = producto,
                        onClick = { onProductoClick(producto.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionNoticias(
    visible: Boolean,
    noticias: List<Noticia>
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(600, delayMillis = 600))
    ) {
        Column {
            Text(
                text = "Últimas Noticias",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(noticias) { noticia ->
                    TarjetaNoticia(noticia = noticia, onClick = { /* TODO: Ir a la noticia */ })
                }
            }
        }
    }
}


@Composable
private fun PorQueElegirnos(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(600, delayMillis = 800))
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
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
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


@Composable
private fun TarjetaAccionRapida(
    accion: AccionRapida,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
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
                color = accion.color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TarjetaProductoDestacado(
    producto: cl.levelup.mobile.model.local.ProductoEntidad,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .height(220.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Image(
                painter = obtenerImagenProducto(producto.codigo),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = producto.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$${producto.precio}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TarjetaNoticia(
    noticia: Noticia,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .height(130.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = noticia.icono,
                contentDescription = noticia.titulo,
                tint = noticia.colorIcono,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = noticia.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = noticia.resumen,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun RowScope.ItemBeneficio(
    emoji: String,
    titulo: String,
    descripcion: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 2
        )
        Text(
            text = descripcion,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 2
        )
    }
}