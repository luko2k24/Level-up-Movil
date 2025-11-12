package com.example.level_up.utils // <--- Este paquete es correcto

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ValidacionTest {

    // --- (Tu prueba existente) ---
    @ParameterizedTest
    @CsvSource(
        "un.nombre@duocuc.cl, true",
        "OTRO.nombre@duocuc.cl, true",
        "alumno@duoc.cl, false",
        "profesor@gmail.com, false",
        "correo-sin-arroba, false"
    )
    fun `probar esCorreoDuoc con multiples casos`(correo: String, resultadoEsperado: Boolean) {
        val resultadoReal = Validacion.esCorreoDuoc(correo)
        assertEquals(resultadoEsperado, resultadoReal)
    }

    @Test
    fun `esAdulto debe devolver verdadero para 18 o mas`() {
        assertTrue(Validacion.esAdulto(18))
        assertTrue(Validacion.esAdulto(25))
        assertFalse(Validacion.esAdulto(17))
        assertFalse(Validacion.esAdulto(10))
    }

    // --- PRUEBAS AÑADIDAS PARA COBERTURA 100% DE Validacion.kt ---

    @Test
    fun `esNombreValido es verdadero para 2 o mas caracteres`() {
        assertTrue(Validacion.esNombreValido("Lukas"))
        assertTrue(Validacion.esNombreValido("Lu"))
        assertFalse(Validacion.esNombreValido("L"))
        assertFalse(Validacion.esNombreValido(" "))
    }

    @Test
    fun `esCorreoValido`() {
        assertTrue(Validacion.esCorreoValido("test@test.com"))
        assertFalse(Validacion.esCorreoValido("test.com"))
    }

    @Test
    fun `esContrasenaValida es verdadero para 6 o mas caracteres`() {
        assertTrue(Validacion.esContrasenaValida("123456"))
        assertTrue(Validacion.esContrasenaValida("password"))
        assertFalse(Validacion.esContrasenaValida("12345"))
    }

    @Test
    fun `contrasenasCoinciden`() {
        assertTrue(Validacion.contrasenasCoinciden("pass", "pass"))
        assertFalse(Validacion.contrasenasCoinciden("pass1", "pass2"))
    }

    @Test
    fun `esCodigoReferidoValido es verdadero para 6 o mas caracteres`() {
        assertTrue(Validacion.esCodigoReferidoValido("ABC1234"))
        assertFalse(Validacion.esCodigoReferidoValido("ABC"))
    }

    @Test
    fun `esCalificacionValida`() {
        assertTrue(Validacion.esCalificacionValida(0.0f))
        assertTrue(Validacion.esCalificacionValida(5.0f))
        assertTrue(Validacion.esCalificacionValida(3.5f))
        assertFalse(Validacion.esCalificacionValida(5.1f))
        assertFalse(Validacion.esCalificacionValida(-1.0f))
    }

    @Test
    fun `esCantidadValida`() {
        assertTrue(Validacion.esCantidadValida(1))
        assertFalse(Validacion.esCantidadValida(0))
    }

    @Test
    fun `esComentarioResenaValido es verdadero para 10 o mas caracteres`() {
        assertTrue(Validacion.esComentarioResenaValido("1234567890"))
        assertTrue(Validacion.esComentarioResenaValido("  1234567890  ")) // prueba con espacios
        assertFalse(Validacion.esComentarioResenaValido("123456789"))
    }

    @Test
    fun `generarCodigoReferido crea un codigo valido`() {
        val codigo = Validacion.generarCodigoReferido("Lukas")
        assertTrue(codigo.startsWith("LUK"))
        assertEquals(7, codigo.length) // 3 letras + 4 números
    }

    @Test
    fun `calcularNivel funciona correctamente`() {
        assertEquals(1, Validacion.calcularNivel(0))
        assertEquals(1, Validacion.calcularNivel(499))
        assertEquals(2, Validacion.calcularNivel(500))
        assertEquals(3, Validacion.calcularNivel(2000))
        assertEquals(4, Validacion.calcularNivel(5000))
        assertEquals(5, Validacion.calcularNivel(10000))
    }

    @Test
    fun `obtenerPorcentajeDescuento funciona correctamente`() {
        assertEquals(0, Validacion.obtenerPorcentajeDescuento(0))
        assertEquals(5, Validacion.obtenerPorcentajeDescuento(1))
        assertEquals(7, Validacion.obtenerPorcentajeDescuento(2))
        assertEquals(10, Validacion.obtenerPorcentajeDescuento(3))
        assertEquals(12, Validacion.obtenerPorcentajeDescuento(4))
        assertEquals(15, Validacion.obtenerPorcentajeDescuento(5))
    }
}