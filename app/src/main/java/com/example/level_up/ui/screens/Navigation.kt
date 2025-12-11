package com.example.level_up.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

object Routes {
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val AUTH = "auth"
    const val DETALLE_PRODUCTO = "product_detail/{productId}"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    const val PANTALLA_DATOS_PAGO = "pantalla_datos_pago/{total}"
    const val PANTALLA_CONFIRMACION = "pantalla_confirmacion/{total}/{nombre}/{correo}/{direccion}"
}

@Composable
fun LevelUpNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.CATALOG) { CatalogScreen(navController) }
        composable(Routes.CART) { CartScreen(navController) }
        composable(Routes.PROFILE) { PantallaPerfil(navController) }
        composable(Routes.AUTH) { AuthScreen(navController) }
        composable(Routes.ADMIN_DASHBOARD) { AdminDashboardScreen(navController) }

        // --- DETALLE DE PRODUCTO ---
        composable(
            route = Routes.DETALLE_PRODUCTO,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            PantallaDetalleProducto(navController, productId)
        }

        // ⭐⭐⭐ RUTAS NUEVAS DEL PROCESO DE PAGO ⭐⭐⭐

        // 👉 Pantalla para ingresar datos del pago
        composable(
            route = Routes.PANTALLA_DATOS_PAGO,
            arguments = listOf(navArgument("total") { type = NavType.IntType })
        ) { backStackEntry ->
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            PantallaDatosPago(navController, total)
        }

        // 👉 Pantalla de confirmación del pago
        composable(
            route = Routes.PANTALLA_CONFIRMACION,
            arguments = listOf(
                navArgument("total") { type = NavType.IntType },
                navArgument("nombre") { type = NavType.StringType },
                navArgument("correo") { type = NavType.StringType },
                navArgument("direccion") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val correo = backStackEntry.arguments?.getString("correo") ?: ""
            val direccion = backStackEntry.arguments?.getString("direccion") ?: ""

            PantallaConfirmacionCompra(
                navController,
                total,
                nombre,
                correo,
                direccion
            )
        }
    }
}
