package com.example.level_up.viewmodel

import com.example.level_up.local.BaseDeDatosApp
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.api.ApiClient // Importado para Retrofit
import com.example.level_up.api.LoginRequest // Importado para el DTO
import com.example.level_up.local.UsuarioEntidad
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException // Importado para manejo de errores de red

// Estado de la autenticación
data class EstadoAuth(
    val nombre: String = "",
    val correo: String = "",
    val edad: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val codigoReferido: String = "",
    val esDuoc: Boolean = false,
    val esModoLogin: Boolean = true,
    val estaCargando: Boolean = false,
    val exito: Boolean = false,
    val usuarioActual: UsuarioEntidad? = null,
    val errores: Map<String, String> = emptyMap()
)

class ViewModelAutenticacion(app: Application) : AndroidViewModel(app) {
    // --- CAMBIO 1: 'private val' -> 'internal var' ---
    internal var repo = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao())

    // --- CAMBIO 2: 'private val' -> 'internal val' ---
    internal val _estado = MutableStateFlow(EstadoAuth())
    val estado: StateFlow<EstadoAuth> = _estado

    // --- Funciones para actualizar el estado desde la UI ---
    fun onNombre(v: String) {
        _estado.value = _estado.value.copy(nombre = v, errores = _estado.value.errores - "nombre")
    }
    fun onCorreo(v: String) {
        _estado.value = _estado.value.copy(
            correo = v,
            esDuoc = Validacion.esCorreoDuoc(v),
            errores = _estado.value.errores - "correo"
        )
    }
    fun onEdad(v: String) {
        _estado.value = _estado.value.copy(edad = v, errores = _estado.value.errores - "edad")
    }
    fun onClave(v: String) {
        _estado.value = _estado.value.copy(clave = v, errores = _estado.value.errores - "clave")
    }
    fun onConfirmarClave(v: String) {
        _estado.value = _estado.value.copy(confirmarClave = v, errores = _estado.value.errores - "confirmarClave")
    }
    fun onCodigoReferido(v: String) {
        _estado.value = _estado.value.copy(codigoReferido = v, errores = _estado.value.errores - "codigoReferido")
    }

    // Cambia entre modo Login y modo Registro
    fun cambiarModo() {
        _estado.value = _estado.value.copy(
            esModoLogin = !_estado.value.esModoLogin,
            errores = emptyMap(),
            exito = false
        )
    }

    fun registrar() = viewModelScope.launch {
        val s = _estado.value
        val errores = mutableMapOf<String, String>()
        val edadInt = s.edad.toIntOrNull() ?: -1

        // Validaciones locales
        if (!Validacion.esNombreValido(s.nombre)) errores["nombre"] = "Nombre debe tener al menos 2 caracteres"
        if (!Validacion.esCorreoValido(s.correo)) errores["correo"] = "Correo electrónico inválido"
        if (!Validacion.esAdulto(edadInt)) errores["edad"] = "Debes ser mayor de 18 años"
        if (!Validacion.esContrasenaValida(s.clave)) errores["clave"] = "Contraseña debe tener al menos 6 caracteres"
        if (!Validacion.contrasenasCoinciden(s.clave, s.confirmarClave)) errores["confirmarClave"] = "Las contraseñas no coinciden"
        if (s.codigoReferido.isNotBlank() && !Validacion.esCodigoReferidoValido(s.codigoReferido)) {
            errores["codigoReferido"] = "Código de referido inválido"
        }

        if (errores.isNotEmpty()) {
            _estado.value = s.copy(errores = errores)
            return@launch
        }

        _estado.value = s.copy(estaCargando = true)

        try {
            val codigoReferidoGenerado = Validacion.generarCodigoReferido(s.nombre)
            val usuarioParaRegistro = UsuarioEntidad(
                nombre = s.nombre.trim(),
                correo = s.correo.lowercase(),
                edad = edadInt,
                contrasena = s.clave,
                esDuoc = s.esDuoc,
                codigoReferido = codigoReferidoGenerado,
                referidoPor = s.codigoReferido.takeIf { it.isNotBlank() } ?: ""
            )

            // *** LLAMADA A LA API DE REGISTRO ***
            val response = ApiClient.usuarioService.registrar(usuarioParaRegistro)

            if (response.isSuccessful && response.body() != null) {
                val usuarioRemoto = response.body()!!

                // Guarda en Room usando la función REGISTRAR del repositorio (CORREGIDO)
                repo.registrar(usuarioRemoto)

                _estado.value = s.copy(
                    estaCargando = false,
                    exito = true,
                    usuarioActual = usuarioRemoto,
                    errores = emptyMap()
                )
            } else {
                // Manejar error de la API (ej: correo duplicado 409)
                val errorMsg = if (response.code() == 409) "Este correo ya está registrado." else "Registro fallido. Error: ${response.code()}"
                _estado.value = s.copy(
                    estaCargando = false,
                    errores = mapOf("general" to errorMsg)
                )
            }
        } catch (e: Exception) {
            val mensajeError = when (e) {
                is HttpException -> "Error HTTP: ${e.code()}. ¿Backend corriendo en 8085?"
                else -> "Error de red. No se pudo conectar al servidor."
            }
            _estado.value = s.copy(
                estaCargando = false,
                errores = mapOf("general" to mensajeError)
            )
        }
    }

    fun iniciarSesion() = viewModelScope.launch {
        val s = _estado.value
        val errores = mutableMapOf<String, String>()

        if (!Validacion.esCorreoValido(s.correo)) errores["correo"] = "Correo electrónico inválido"
        if (!Validacion.esContrasenaValida(s.clave)) errores["clave"] = "Contraseña debe tener al menos 6 caracteres"

        if (errores.isNotEmpty()) {
            _estado.value = s.copy(errores = errores)
            return@launch
        }

        _estado.value = s.copy(estaCargando = true)

        try {
            val request = LoginRequest(correo = s.correo.lowercase(), contrasena = s.clave)

            // *** LLAMADA A LA API DE LOGIN ***
            val response = ApiClient.usuarioService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val usuarioRemoto = response.body()!!

                // Éxito: Actualiza el estado local (Room) con los datos del servidor
                repo.actualizarEstadoSesion(usuarioRemoto.id, true)
                repo.actualizar(usuarioRemoto.copy(sesionIniciada = true))

                _estado.value = s.copy(
                    estaCargando = false,
                    exito = true, // Marcamos como éxito
                    usuarioActual = usuarioRemoto, // Guardamos el usuario
                    errores = emptyMap()
                )
            } else {
                // Manejo de error de credenciales
                _estado.value = s.copy(
                    estaCargando = false,
                    errores = mapOf("general" to "Credenciales inválidas")
                )
            }
        } catch (e: Exception) {
            val mensajeError = when (e) {
                is HttpException -> "Error HTTP: ${e.code()}. ¿Backend corriendo en 8085?"
                else -> "Error de red. No se pudo conectar al servidor."
            }
            _estado.value = s.copy(
                estaCargando = false,
                errores = mapOf("general" to mensajeError)
            )
        }
    }

    fun cerrarSesion() = viewModelScope.launch {
        // CORREGIDO: Usamos usuarioActual, que es la fuente de verdad del estado.
        val usuarioACerrar = _estado.value.usuarioActual

        if (usuarioACerrar != null) {
            // Llama al API para informar al backend (id.toLong() porque el backend usa Long)
            ApiClient.usuarioService.actualizarEstadoSesion(usuarioACerrar.id.toLong(), false)
            // Actualiza el estado local (Room)
            repo.actualizarEstadoSesion(usuarioACerrar.id, false)
        }
        _estado.value = EstadoAuth() // Resetea el estado
    }

    fun limpiarExito() {
        _estado.value = _estado.value.copy(exito = false)
    }
}