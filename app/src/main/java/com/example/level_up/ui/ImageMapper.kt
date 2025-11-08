package com.example.level_up.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.level_up.R

//Una kotlin class para llamar a las imagenes que ahora pasaran a la app
@Composable
fun obtenerImagenProducto(codigoProducto: String) = when (codigoProducto) {
    // Juegos de Mesa
    "JM001" -> painterResource(id = R.drawable.carcassonne)
    "JM002" -> painterResource(id = R.drawable.catan)

    // Accesorios
    "AC001" -> painterResource(id = R.drawable.mando)
    "AC002" -> painterResource(id = R.drawable.audifonos)

    // Consolas
    "CO001" -> painterResource(id = R.drawable.play)

    // Computadores
    "CG001" -> painterResource(id = R.drawable.pcgamer)

    // Sillas
    "SG001" -> painterResource(id = R.drawable.silla)

    // Mouse
    "MS001" -> painterResource(id = R.drawable.mouse)

    // Mousepad
    "MP001" -> painterResource(id = R.drawable.pad)

    // Poleras
    "PP001" -> painterResource(id = R.drawable.camiseta)

    // Imagen por defecto si no encuentra el código
    else -> painterResource(id = R.drawable.ic_launcher_foreground)
}