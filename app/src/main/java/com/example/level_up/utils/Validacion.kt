package com.example.level_up.utils

import android.util.Patterns


object Validacion {


    fun esAdulto(edad: Int): Boolean = edad >= 18


    fun esCorreoDuoc(correo: String): Boolean = correo.lowercase().endsWith("@duocuc.cl")


    fun esCorreoValido(correo: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(correo).matches()

    /**
     * Verifica si la contraseña tiene una longitud mínima de 6 caracteres.
     */
    fun esContrasenaValida(contrasena: String): Boolean = contrasena.length >= 6

    /**
     * Comprueba si la contraseña y la confirmación de la contraseña coinciden.
     */
    fun contrasenasCoinciden(contrasena: String, confirmarContrasena: String): Boolean = contrasena == confirmarContrasena

    /**
     * Verifica si el nombre es válido (longitud de al menos 2 caracteres después de quitar espacios).
     */
    fun esNombreValido(nombre: String): Boolean = nombre.trim().length >= 2

    // ----------------------------------------------------
    // Validaciones de Producto
    // ----------------------------------------------------

    /**
     * Verifica si el precio es válido (mayor a 0).
     */
    fun esPrecioValido(precio: Int): Boolean = precio > 0

    /**
     * Verifica si el stock es válido (mayor o igual a 0).
     */
    fun esStockValido(stock: Int): Boolean = stock >= 0

    /**
     * Verifica si la calificación (rating) está en el rango válido de 0.0 a 5.0.
     */
    fun esCalificacionValida(calificacion: Float): Boolean = calificacion in 0.0..5.0

    // ----------------------------------------------------
    // Validaciones de Carrito
    // ----------------------------------------------------

    /**
     * Verifica si la cantidad del producto a añadir es válida (mayor a 0).
     */
    fun esCantidadValida(cantidad: Int): Boolean = cantidad > 0

    // ----------------------------------------------------
    // Validación de Código de Referido
    // ----------------------------------------------------

    /**
     * Verifica si el código de referido tiene una longitud mínima de 6 caracteres.
     */
    fun esCodigoReferidoValido(codigo: String): Boolean = codigo.length >= 6

    // ----------------------------------------------------
    // Validación de Reseña (Review)
    // ----------------------------------------------------

    /**
     * Verifica si el comentario de la reseña tiene una longitud de al menos 10 caracteres.
     */
    fun esComentarioResenaValido(comentario: String): Boolean = comentario.trim().length >= 10

    // ----------------------------------------------------
    // Lógica de Negocio
    // ----------------------------------------------------

    /**
     * Genera un código de referido único tomando las primeras 3 letras del nombre
     * y añadiendo un sufijo numérico aleatorio de 4 dígitos.
     */
    fun generarCodigoReferido(nombre: String): String {
        val nombreLimpio = nombre.replace(" ", "").uppercase()
        val sufijoAleatorio = (1000..9999).random()
        return "${nombreLimpio.take(3)}$sufijoAleatorio"
    }

    /**
     * Calcula el nivel del usuario en base a sus puntos acumulados.
     */
    fun calcularNivel(puntos: Int): Int {
        return when {
            puntos >= 10000 -> 5
            puntos >= 5000 -> 4
            puntos >= 2000 -> 3
            puntos >= 500 -> 2
            else -> 1
        }
    }

    /**
     * Obtiene el porcentaje de descuento asociado al nivel del usuario.
     */
    fun obtenerPorcentajeDescuento(nivel: Int): Int {
        return when (nivel) {
            5 -> 15
            4 -> 12
            3 -> 10
            2 -> 7
            1 -> 5
            else -> 0
        }
    }
}