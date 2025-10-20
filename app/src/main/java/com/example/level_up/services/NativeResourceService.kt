package cl.levelup.mobile.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.*
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

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun obtenerUbicacionActual(): Location? {
        if (!tienePermisoUbicacion()) return null

        return try {
            val ubicacion = gestorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ubicacion ?: gestorUbicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }
    }

    fun crearArchivoImagen(): File? {
        return try {
            val marcaTiempo = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val directorioAlmacenamiento = contexto.getExternalFilesDir("Imagenes") // Traducido de "Pictures"
            File.createTempFile("JPEG_${marcaTiempo}_", ".jpg", directorioAlmacenamiento)
        } catch (e: Exception) {
            null
        }
    }

    fun obtenerUriImagen(archivo: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                contexto,
                "${contexto.packageName}.fileprovider",
                archivo
            )
        } catch (e: Exception) {
            null
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun obtenerCadenaUbicacion(): String {
        val ubicacion = obtenerUbicacionActual()
        return if (ubicacion != null) {
            "Lat: ${String.format("%.4f", ubicacion.latitude)}, " +
                    "Lng: ${String.format("%.4f", ubicacion.longitude)}"
        } else {
            "Ubicación no disponible"
        }
    }
}

@Composable
fun recordarServicioRecursosNativos(): ServicioRecursosNativos {
    val contexto = androidx.compose.ui.platform.LocalContext.current
    return remember { ServicioRecursosNativos(contexto) }
}

@Composable
fun recordarLanzadorPermisos(
    enResultadoPermiso: (Boolean) -> Unit // Traducido de 'onPermissionResult'
): androidx.activity.result.ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { esConcedido -> // Traducido de 'isGranted'
        enResultadoPermiso(esConcedido)
    }
}