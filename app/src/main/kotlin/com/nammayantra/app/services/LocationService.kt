package com.nammayantra.app.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): android.location.Location? = suspendCancellableCoroutine { cont ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            cont.resumeWithException(SecurityException("Location permission not granted"))
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location)
                } else {
                    cont.resumeWithException(Exception("No location available"))
                }
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }

    companion object {
        fun hasPermissions(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
}
