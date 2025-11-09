package com.example.level_up.viewmodel

import com.example.level_up.local.BaseDeDatosApp
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.level_up.local.UsuarioEntidad
import com.example.level_up.repository.UsuarioRepository
import com.example.level_up.utils.Validacion

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estado de la autenticación (traducido)
data class EstadoAuth(
    val nombre: String = "",
    val correo: String = "",
    val edad: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val codigoReferido: String = "",
    val esDuoc: Boolean = false,
    val esModoLogin: Boolean = true, // Empezamos en modo Login
    val estaCargando: Boolean = false,
    val exito: Boolean = false,
    val usuarioActual: UsuarioEntidad? = null,
    val errores: Map<String, String> = emptyMap()
)

class ViewModelAutenticacion(app: Application) : AndroidViewModel(app) {
    private val repo = UsuarioRepository(BaseDeDatosApp.obtener(app).UsuarioDao())
    private val _estado = MutableStateFlow(EstadoAuth())
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

        // Validaciones (usando tu archivo Validacion.kt)
        if (!Validacion.esNombreValido(s.nombre)) errores["nombre"] = "Nombre debe tener al menos 2 caracteres"
        if (!Validacion.esCorreoValido(s.correo)) errores["correo"] = "Correo electrónico inválido"
        if (!Validacion.esAdulto(edadInt)) errores["edad"] = "Debes ser mayor de 18 años"
        if (!Validacion.esContrasenaValida(s.clave)) errores["clave"] = "Contraseña debe tener al menos 6 caracteres"
        if (!Validacion.contrasenasCoinciden(s.clave, s.confirmarClave)) errores["confirmarClave"] = "Las contraseñas no coinciden"
        if (s.codigoReferido.isNotBlank() && !Validacion.esCodigoReferidoValido(s.codigoReferido)) {
            errores["codigoReferido"] = "Código de referido inválido"
        }

        // Comprobar si el correo ya existe
        if (repo.buscarPorCorreo(s.correo) != null) {
            errores["correo"] = "Este correo ya está registrado"
        }

        if (errores.isNotEmpty()) {
            _estado.value = s.copy(errores = errores)
            return@launch
        }

        _estado.value = s.copy(estaCargando = true)

        try {
            val codigoReferidoGenerado = Validacion.generarCodigoReferido(s.nombre)
            val usuario = UsuarioEntidad(
                nombre = s.nombre.trim(),
                correo = s.correo.lowercase(),
                edad = edadInt,
                contrasena = s.clave, // ¡Importante! En un proyecto real, esto debería estar encriptado (hashed)
                esDuoc = s.esDuoc,
                codigoReferido = codigoReferidoGenerado,
                referidoPor = s.codigoReferido.takeIf { it.isNotBlank() } ?: ""
            )

            // Registra al usuario
            val nuevoId = repo.registrar(usuario)

            // --- ¡MEJORA IMPORTANTE! ---
            // Inicia sesión automáticamente después de registrar
            val usuarioRegistrado = usuario.copy(id = nuevoId.toInt())
            repo.actualizarEstadoSesion(usuarioRegistrado.id, true)

            _estado.value = s.copy(
                estaCargando = false,
                exito = true, // Marcamos como éxito
                usuarioActual = usuarioRegistrado, // Guardamos el usuario
                errores = emptyMap()
            )
        } catch (e: Exception) {
            _estado.value = s.copy(
                estaCargando = false,
                errores = mapOf("general" to "Error al registrar usuario: ${e.message}")
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
            val usuario = repo.buscarPorCorreo(s.correo.lowercase())
            // Comprueba si el usuario existe y la contraseña coincide
            if (usuario != null && usuario.contrasena == s.clave) {
                // ¡Éxito! Marcamos la sesión como iniciada
                repo.actualizarEstadoSesion(usuario.id, true)
                _estado.value = s.copy(
                    estaCargando = false,
                    exito = true, // Marcamos como éxito
                    usuarioActual = usuario, // Guardamos el usuario
                    errores = emptyMap()
                )
            } else {
                _estado.value = s.copy(
                    estaCargando = false,
                    errores = mapOf("general" to "Credenciales inválidas")
                )
            }
        } catch (e: Exception) {
            _estado.value = s.copy(
                estaCargando = false,
                errores = mapOf("general" to "Error al iniciar sesión: ${e.message}")
            )
        }
    }

    fun cerrarSesion() = viewModelScope.launch {
        val usuarioActual = _estado.value.usuarioActual
        if (usuarioActual != null) {
            repo.actualizarEstadoSesion(usuarioActual.id, false)
        }
        _estado.value = EstadoAuth() // Resetea el estado
    }

    fun limpiarExito() {
        _estado.value = _estado.value.copy(exito = false)
    }
}