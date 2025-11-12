package com.example.level_up.viewmodel // <--- Refleja el paquete del ViewModel

import android.app.Application
import com.example.level_up.MainCoroutineRule // <--- Importa la regla que creamos
import com.example.level_up.api.ApiClient
import com.example.level_up.api.LoginRequest
import com.example.level_up.api.UsuarioService
import com.example.level_up.local.UsuarioEntidad
import com.example.level_up.repository.UsuarioRepository
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

    // 4. Mocks para la API
    private val apiClientMock = mockkStatic(ApiClient::class)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        // Simular el ApiClient estático
        every { ApiClient.usuarioService } returns mockUsuarioService

        // Crear el ViewModel real
        viewModel = ViewModelAutenticacion(mockApplication)

        // --- ¡CAMBIO IMPORTANTE! ---
        // Necesitamos "inyectar" nuestro repositorio falso en el ViewModel.
        // Debes cambiar la variable 'repo' en ViewModelAutenticacion.kt
        //
        // EN: app/src/main/java/com/example/level_up/viewmodel/ViewModelAutenticacion.kt
        // CAMBIA:
        // private val repo = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao())
        // POR:
        // internal var repo = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao())
        //
        // Esto nos permite hacer lo siguiente:
        viewModel.repo = mockUsuarioRepository
    }

    @AfterEach
    fun tearDown() {
        unmockkAll() // Limpiar mocks estáticos
    }

    // --- INICIAN LAS PRUEBAS ---

    @Test
    fun `iniciarSesion con credenciales validas debe poner exito en true`() = runTest {
        // 1. Arrange (Preparar)
        val correo = "test@duocuc.cl"
        val pass = "123456"
        val usuarioMock = UsuarioEntidad(id = 1, nombre = "Test", correo = correo, edad = 20, contrasena = pass)

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
        // 1. Act
        viewModel.onCorreo("correo-invalido")
        viewModel.onClave("123456")
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
        // 1. Act
        viewModel.onNombre("Test")
        viewModel.onCorreo("test@test.cl")
        viewModel.onEdad("20")
        viewModel.onClave("123456")
        viewModel.onConfirmarClave("654321") // Diferente
        viewModel.registrar()

        // 3. Assert
        val estado = viewModel.estado.value
        assertFalse(estado.estaCargando)
        assertEquals("Las contraseñas no coinciden", estado.errores["confirmarClave"])
        coVerify(exactly = 0) { mockUsuarioService.registrar(any()) }
    }

    @Test
    fun `cerrarSesion debe llamar a la API y resetear estado`() = runTest {
        // 1. Arrange
        val usuarioMock = UsuarioEntidad(id = 1, nombre = "Test", correo = "test@test.cl", edad = 20, contrasena = "123")

        // --- CAMBIO 3: Usar '_estado' (con guion bajo) ---
        // Forzamos el estado de "logueado"
        viewModel._estado.value = viewModel.estado.value.copy(usuarioActual = usuarioMock)

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