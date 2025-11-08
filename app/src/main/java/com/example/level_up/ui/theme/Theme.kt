package com.example.level_up.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Paleta de colores oscuros basada en el PDF
private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen, // Color de Acento [cite: 72]
    secondary = ElectricBlue, // Color de Acento [cite: 72]
    background = Black, // Fondo Principal
    surface = Black, // Fondo Principal

    // --- Colores "On" (Texto) ---
    onPrimary = Black, // Texto sobre Verde Neón
    onSecondary = White, // Texto sobre Azul Eléctrico
    onBackground = White, // Texto Principal [cite: 85]
    onSurface = White, // Texto Principal [cite: 85]

    // --- Contenedores ---
    // (Usamos un gris muy oscuro para que las Cards se diferencien del fondo negro)
    primaryContainer = DarkSurface,
    onPrimaryContainer = White,
    secondaryContainer = ElectricBlue.copy(alpha = 0.2f),
    onSecondaryContainer = ElectricBlue,

    // --- Variantes (Texto secundario) ---
    surfaceVariant = DarkSurface, // Para cards como "Por qué elegirnos"
    onSurfaceVariant = LightGrayD3 // Texto Secundario [cite: 87]
)

@Composable
fun LevelupTheme(
    // Forzamos el tema oscuro como pide la estética del PDF
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Solo usamos la paleta oscura
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Barra de estado negra
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}