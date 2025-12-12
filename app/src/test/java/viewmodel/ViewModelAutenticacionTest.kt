package com.example.level_up.viewmodel

import android.app.Application
import com.example.level_up.MainCoroutineRule
import com.example.level_up.api.LoginRequest
import com.example.level_up.api.UsuarioService
import com.example.level_up.Entidades.UsuarioEntidad
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelAutenticacionTest {

    // Registra la regla JUnit5 para coroutines (setMain)
    @JvmField
    @RegisterExtension
    val coroutineRule = MainCoroutineRule()

    @RelaxedMockK
    private lateinit var mockApplication: Application

    @MockK
    private lateinit var mockUsuarioRepository: UsuarioRepository

    @MockK
    private lateinit var mockUsuarioService: UsuarioService

    private lateinit var viewModel: ViewModelAutenticacion

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(Validacion) // mockear objeto estático Validacion

        viewModel = ViewModelAutenticacion(mockApplication)

        // Inyectar mocks en las propiedades internas del ViewModel
        viewModel.repo = mockUsuarioRepository
        viewModel.usuarioService = mockUsuarioService
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(Validacion)
    }

    @Test
    fun `iniciarSesion con credenciales validas debe poner exito en true`() = runTest {
        val correo = "test@duocuc.cl"
        val pass = "123456"
        val usuarioMock = UsuarioEntidad(id = 1, nombre = "Test", correo = correo, edad = 20, contrasena = pass)

        // Simular validaciones puras
        every { Validacion.esCorreoValido(correo) } returns true
        every { Validacion.esContrasenaValida(pass) } returns true

        // Simular servicio y repo
        coEvery { mockUsuarioService.login(any<LoginRequest>()) } returns Response.success(usuarioMock)
        coEvery { mockUsuarioRepository.actualizarEstadoSesion(any<Int>(), any<Boolean>()) } just Runs
        coEvery { mockUsuarioRepository.actualizar(any<UsuarioEntidad>()) } just Runs

        viewModel.onCorreo(correo)
        viewModel.onClave(pass)
        viewModel.iniciarSesion()

        // Avanzar el scheduler del dispatcher de prueba para ejecutar coroutines pendientes
        coroutineRule.dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.estado.value
        assertTrue(estado.exito)
        assertEquals(usuarioMock, estado.usuarioActual)
        assertTrue(estado.errores.isEmpty())
    }

    // Puedes agregar más tests similares (credenciales invalidas, error de red, etc.)
}
