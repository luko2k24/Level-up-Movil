package com.example.level_up.utils // Mismo paquete que el archivo a probar

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ValidacionTest {

    // --- Esta es una prueba unitaria simple ---
    @Test
    fun `esCorreoDuoc debe devolver verdadero para correos @duocuc_cl`() {
        // 1. Preparación (Arrange)
        val correoValido = "un.alumno@duocuc.cl"
        val correoInvalido = "un.alumno@gmail.com"

        // 2. Actuación (Act)
        val resultadoValido = Validacion.esCorreoDuoc(correoValido)
        val resultadoInvalido = Validacion.esCorreoDuoc(correoInvalido)

        // 3. Afirmación (Assert)
        assertTrue(resultadoValido)  // Afirmamos que esto es verdadero
        assertFalse(resultadoInvalido) // Afirmamos que esto es falso
    }

    // --- Esta es una prueba parametrizada (avanzada pero muy útil) ---
    // Prueba la misma función con múltiples datos de forma automática.
    @ParameterizedTest
    @CsvSource(
        "un.nombre@duocuc.cl, true",  // correo, resultadoEsperado
        "OTRO.nombre@duocuc.cl, true",
        "alumno@duoc.cl, false",
        "profesor@gmail.com, false",
        "correo-sin-arroba, false"
    )
    fun `probar esCorreoDuoc con multiples casos`(correo: String, resultadoEsperado: Boolean) {
        val resultadoReal = Validacion.esCorreoDuoc(correo)
        assertEquals(resultadoEsperado, resultadoReal)
    }

    // --- Otra prueba simple para otra función ---
    @Test
    fun `esAdulto debe devolver verdadero para 18 o mas`() {
        assertTrue(Validacion.esAdulto(18))
        assertTrue(Validacion.esAdulto(25))
        assertFalse(Validacion.esAdulto(17))
        assertFalse(Validacion.esAdulto(10))
    }
}