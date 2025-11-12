package com.example.level_up.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType // <-- NUEVO
import androidx.navigation.navArgument // <-- NUEVO

object Routes {
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val AUTH = "auth"
    const val DETALLE_PRODUCTO = "product_detail/{productId}" // <-- NUEVA RUTA CON ARGUMENTO
}

@Composable
fun LevelUpNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.CATALOG) { CatalogScreen(navController) }
        composable(Routes.CART) { CartScreen(navController) }
        composable(Routes.PROFILE) { PantallaPerfil(navController) }
        composable(Routes.AUTH) { AuthScreen(navController) }
        // --- RUTA DEL DETALLE DE PRODUCTO (RESEÑAS) ---
        composable(
            route = Routes.DETALLE_PRODUCTO,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            PantallaDetalleProducto(navController, productId)
        }
    }
}