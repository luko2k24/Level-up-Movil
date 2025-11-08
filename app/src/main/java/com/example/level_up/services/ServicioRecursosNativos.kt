package com.example.level_up.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// --- SE ELIMINA LA IMPORTACIÓN DE @RequiresPermission ---
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ServicioRecursosNativos(private val contexto: Context) {

    private val gestorUbicacion = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun tienePermisoCamara(): Boolean {
        return ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun tienePermisoUbicacion(): Boolean {
        return ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // --- ¡CORRECCIÓN! ---
    // Se elimina la anotación "@RequiresPermission(...)" de aquí abajo,
    // porque la función YA comprueba el permiso internamente.
    fun obtenerUbicacionActual(enResultado: (String) -> Unit) {
        if (!tienePermisoUbicacion()) {
            enResultado("Permiso de ubicación denegado.")
            return
        }

        try {
            // Intenta obtener la última ubicación conocida (rápido)
            val ubicacion = gestorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: gestorUbicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (ubicacion != null) {
                enResultado(formatearUbicacion(ubicacion))
            } else {
                // Si no hay última ubicación, solicita una nueva (lento)
                gestorUbicacion.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L, // 5 segundos
                    10f,   // 10 metros
                ) { loc ->
                    // Cuando la ubicación llegue, la envía y deja de escuchar
                    enResultado(formatearUbicacion(loc))
                    gestorUbicacion.removeUpdates { }
                }
            }
        } catch (e: SecurityException) {
            // Este try/catch es la segunda forma de manejar el error de seguridad
            enResultado("Error de seguridad de ubicación.")
        }
    }

    private fun formatearUbicacion(ubicacion: Location): String {
        return "Lat: ${String.format("%.4f", ubicacion.latitude)}, " +
                "Lng: ${String.format("%.4f", ubicacion.longitude)}"
    }
}

// Composable "remember" para usar el servicio fácilmente
@Composable
fun recordarServicioRecursosNativos(): ServicioRecursosNativos {
    val contexto = LocalContext.current
    return remember { ServicioRecursosNativos(contexto) }
}