package com.example.level_up.viewmodel

import android.app.Application
import com.example.level_up.MainCoroutineRule
import com.example.level_up.api.LoginRequest
import com.example.level_up.api.UsuarioService
import com.example.level_up.Entidades.UsuarioEntidad
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion // Importar Validacion
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import retrofit2.Response

@ExperimentalCoroutinesApi
class ViewModelAutenticacionTest {

    // 1. Registrar la regla de coroutines
    @JvmField
    @RegisterExtension
    val coroutineRule = MainCoroutineRule()

    // 2. Mocks (Simulaciones)
    @RelaxedMockK
    private lateinit var mockApplication: Application

    @MockK
    private lateinit var mockUsuarioRepository: UsuarioRepository

    @MockK
    private lateinit var mockUsuarioService: UsuarioService

    // 3. El ViewModel a probar
    private lateinit var viewModel: ViewModelAutenticacion

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        // --- CORRECCIÓN DE MOCKS ESTÁTICOS ---
        mockkObject(Validacion) // <-- ¡LA SOLUCIÓN AL NPE! Mockear el validador

        // Crear el ViewModel real
        viewModel = ViewModelAutenticacion(mockApplication)

        // --- Inyectar mocks en las variables 'internal' ---
        viewModel.repo = mockUsuarioRepository
        viewModel.usuarioService = mockUsuarioService // <-- ¡NUEVA INYECCIÓN!
    }

    @AfterEach
    fun tearDown() {
        // Limpiar solo el mock estático de Validacion
        unmockkObject(Validacion)
    }

    // --- INICIAN LAS PRUEBAS ---

    @Test
    fun `iniciarSesion con credenciales validas debe poner exito en true`() = runTest {
        // 1. Arrange (Preparar)
        val correo = "test@duocuc.cl"
        val pass = "123456"
        val usuarioMock = UsuarioEntidad(id = 1, nombre = "Test", correo = correo, edad = 20, contrasena = pass)

        // --- Simular que la validación PASA ---
        every { Validacion.esCorreoValido(correo) } returns true
        every { Validacion.esContrasenaValida(pass) } returns true

        // Simular API y Repo
        coEvery { mockUsuarioService.login(LoginRequest(correo, pass)) } returns Response.success(usuarioMock)
        coEvery { mockUsuarioRepository.actualizarEstadoSesion(1, true) } just Runs
        coEvery { mockUsuarioRepository.actualizar(any()) } just Runs

        // 2. Act (Actuar)
        viewModel.onCorreo(correo)
        viewModel.onClave(pass)
        viewModel.iniciarSesion()

        // 3. Assert (Verificar)
        val estado = viewModel.estado.value
        assertTrue(estado.exito)
        assertEquals(usuarioMock, estado.usuarioActual)
        assertTrue(estado.errores.isEmpty())
    }

    @Test
    fun `iniciarSesion con credenciales invalidas debe poner error`() = runTest {
        // 1. Arrange
        val correo = "test@duocuc.cl"
        val pass = "123456"

        // --- Simular que la validación PASA ---
        every { Validacion.esCorreoValido(correo) } returns true
        every { Validacion.esContrasenaValida(pass) } returns true

        // Simular que la API Falla (401)
        coEvery { mockUsuarioService.login(any()) } returns Response.error(401, "".toResponseBody(null))

        // 2. Act
        viewModel.onCorreo(correo)
        viewModel.onClave(pass)
        viewModel.iniciarSesion()

        // 3. Assert
        val estado = viewModel.estado.value
        assertFalse(estado.exito)
        assertNull(estado.usuarioActual)
        assertEquals("Credenciales inválidas", estado.errores["general"])
    }

    @Test
    fun `iniciarSesion con correo invalido no debe llamar a la API`() = runTest {
        // 1. Arrange
        val correo = "correo-invalido"
        val pass = "123456"

        // --- Simular que la validación FALLA ---
        every { Validacion.esCorreoValido(correo) } returns false
        every { Validacion.esContrasenaValida(pass) } returns true // Simular que esta pasa

        // 2. Act
        viewModel.onCorreo(correo)
        viewModel.onClave(pass)
        viewModel.iniciarSesion()

        // 3. Assert
        val estado = viewModel.estado.value
        assertFalse(estado.estaCargando)
        assertEquals("Correo electrónico inválido", estado.errores["correo"])
        coVerify(exactly = 0) { mockUsuarioService.login(any()) } // Verificar que NUNCA se llamó
    }

    @Test
    fun `registrar con datos validos debe poner exito en true`() = runTest {
        // 1. Arrange
        val nombre = "Usuario Nuevo"
        val correo = "nuevo@test.cl"
        val edad = "25"
        val clave = "password"
        val usuarioRegistradoMock = UsuarioEntidad(id = 2, nombre = nombre, correo = correo, edad = 25, contrasena = clave)

        // --- Simular que TODA la validación PASA ---
        every { Validacion.esNombreValido(nombre) } returns true
        every { Validacion.esCorreoValido(correo) } returns true
        every { Validacion.esAdulto(25) } returns true
        every { Validacion.esContrasenaValida(clave) } returns true
        every { Validacion.contrasenasCoinciden(clave, clave) } returns true
        every { Validacion.esCodigoReferidoValido(any()) } returns true
        every { Validacion.generarCodigoReferido(nombre) } returns "USU1234"

        // Simular API y Repo
        coEvery { mockUsuarioService.registrar(any()) } returns Response.success(usuarioRegistradoMock)
        coEvery { mockUsuarioRepository.registrar(any()) } returns 2L

        // 2. Act
        viewModel.onNombre(nombre)
        viewModel.onCorreo(correo)
        viewModel.onEdad(edad)
        viewModel.onClave(clave)
        viewModel.onConfirmarClave(clave)
        viewModel.registrar()

        // 3. Assert
        val estado = viewModel.estado.value
        assertTrue(estado.exito)
        assertEquals(usuarioRegistradoMock, estado.usuarioActual)
    }

    @Test
    fun `registrar con correo duplicado debe poner error`() = runTest {
        // 1. Arrange
        val nombre = "Usuario Nuevo"
        val correo = "nuevo@test.cl"
        val edad = "25"
        val clave = "password"

        // --- Simular que TODA la validación PASA ---
        every { Validacion.esNombreValido(nombre) } returns true
        every { Validacion.esCorreoValido(correo) } returns true
        every { Validacion.esAdulto(25) } returns true
        every { Validacion.esContrasenaValida(clave) } returns true
        every { Validacion.contrasenasCoinciden(clave, clave) } returns true
        every { Validacion.esCodigoReferidoValido(any()) } returns true
        every { Validacion.generarCodigoReferido(nombre) } returns "USU1234"

        // Simular API (Error 409 Conflict)
        coEvery { mockUsuarioService.registrar(any()) } returns Response.error(409, "".toResponseBody(null))

        // 2. Act
        viewModel.onNombre(nombre)
        viewModel.onCorreo(correo)
        viewModel.onEdad(edad)
        viewModel.onClave(clave)
        viewModel.onConfirmarClave(clave)
        viewModel.registrar()

        // 3. Assert
        val estado = viewModel.estado.value
        assertFalse(estado.exito)
        assertEquals("Este correo ya está registrado.", estado.errores["general"])
    }

    @Test
    fun `registrar con contrasenas no coincidentes debe poner error`() = runTest {
        // 1. Arrange
        val clave1 = "123456"
        val clave2 = "654321" // Diferente

        // --- Simular validaciones (una falla) ---
        every { Validacion.esNombreValido(any()) } returns true
        every { Validacion.esCorreoValido(any()) } returns true
        every { Validacion.esAdulto(any()) } returns true
        every { Validacion.esContrasenaValida(clave1) } returns true
        every { Validacion.contrasenasCoinciden(clave1, clave2) } returns false // <-- La que falla

        // 2. Act
        viewModel.onNombre("Test")
        viewModel.onCorreo("test@test.cl")
        viewModel.onEdad("20")
        viewModel.onClave(clave1)
        viewModel.onConfirmarClave(clave2)
        viewModel.registrar()

        // 3. Assert
        val estado = viewModel.estado.value
        assertFalse(estado.estaCargando)
        assertEquals("Las contraseñas no coinciden", estado.errores["confirmarClave"])
        coVerify(exactly = 0) { mockUsuarioService.registrar(any()) } // API no debe ser llamada
    }

    @Test
    fun `cerrarSesion debe llamar a la API y resetear estado`() = runTest {
        // 1. Arrange
        val usuarioMock = UsuarioEntidad(id = 1, nombre = "Test", correo = "test@test.cl", edad = 20, contrasena = "123")

        // Forzamos el estado de "logueado" (usando _estado)
        viewModel._estado.value = viewModel.estado.value.copy(usuarioActual = usuarioMock)

        // Simular API y Repo
        coEvery { mockUsuarioService.actualizarEstadoSesion(1L, false) } returns Response.success(usuarioMock)
        coEvery { mockUsuarioRepository.actualizarEstadoSesion(1, false) } just Runs

        // 2. Act
        viewModel.cerrarSesion()

        // 3. Assert
        val estado = viewModel.estado.value
        assertNull(estado.usuarioActual)
        assertTrue(estado.esModoLogin) // El estado se resetea al inicial
        coVerify { mockUsuarioService.actualizarEstadoSesion(1L, false) }
    }
}