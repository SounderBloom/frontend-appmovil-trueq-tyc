package com.trueq.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Ubicación por defecto (CDMX), usada solo si no hay permiso o falla la
// geolocalización. Igual que UBICACION_POR_DEFECTO en useUbicacion.ts.
data class Coordenadas(val latitud: Double, val longitud: Double)

private val UBICACION_POR_DEFECTO = Coordenadas(19.4326, -99.1332)

fun tienePermisoUbicacion(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
suspend fun obtenerUbicacionActual(context: Context): Coordenadas {
    if (!tienePermisoUbicacion(context)) return UBICACION_POR_DEFECTO

    return try {
        suspendCancellableCoroutine { continuacion ->
            val cliente = LocationServices.getFusedLocationProviderClient(context)
            cliente.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuacion.resume(Coordenadas(location.latitude, location.longitude))
                    } else {
                        continuacion.resume(UBICACION_POR_DEFECTO)
                    }
                }
                .addOnFailureListener {
                    continuacion.resume(UBICACION_POR_DEFECTO)
                }
        }
    } catch (e: Exception) {
        UBICACION_POR_DEFECTO
    }
}
