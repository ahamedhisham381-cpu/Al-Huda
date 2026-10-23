package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Location pipeline (never hangs, every wait has a timeout):
 *  1. Google Fused Location (best: uses Wi-Fi/cell/GPS together)
 *  2. Fused last known location
 *  3. Android LocationManager (network, then GPS) for phones without Google Play services
 *  4. any cached fix
 * If all fail the caller falls back to an IP-based lookup (see GeoRepository.ipLocate).
 */
class LocationHelper(private val context: Context) {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    fun isLocationServiceOn(): Boolean = try {
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } catch (_: Exception) {
        false
    }

    // ---------- 1 & 2: Google Fused Location ----------

    @SuppressLint("MissingPermission")
    private suspend fun fusedCurrent(): Location? = suspendCancellableCoroutine { cont ->
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                .addOnCanceledListener { if (cont.isActive) cont.resume(null) }
        } catch (_: Throwable) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fusedLast(): Location? = suspendCancellableCoroutine { cont ->
        try {
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                .addOnCanceledListener { if (cont.isActive) cont.resume(null) }
        } catch (_: Throwable) {
            if (cont.isActive) cont.resume(null)
        }
    }

    // ---------- 3: Plain Android LocationManager ----------

    @SuppressLint("MissingPermission")
    private fun bestLastKnown(): Location? {
        var best: Location? = null
        val providers = try { manager.getProviders(false) } catch (_: Exception) { emptyList<String>() }
        for (p in providers) {
            val loc = try { manager.getLastKnownLocation(p) } catch (_: Exception) { null } ?: continue
            val current = best
            if (current == null || loc.time > current.time) best = loc
        }
        return best
    }

    @SuppressLint("MissingPermission")
    private suspend fun managerFreshFix(provider: String): Location? =
        suspendCancellableCoroutine { cont ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val signal = CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    manager.getCurrentLocation(provider, signal, context.mainExecutor) { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                } else {
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            manager.removeUpdates(this)
                            if (cont.isActive) cont.resume(location)
                        }
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    }
                    cont.invokeOnCancellation { manager.removeUpdates(listener) }
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(null)
            }
        }

    private fun enabledProviders(): List<String> {
        val result = mutableListOf<String>()
        try { if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) result += LocationManager.NETWORK_PROVIDER } catch (_: Exception) {}
        try { if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) result += LocationManager.GPS_PROVIDER } catch (_: Exception) {}
        return result
    }

    /** Returns a device location, or null (permission missing, location off, or no fix in time). */
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.Main.immediate) {
        if (!hasPermission()) return@withContext null

        // 1. Fused, fresh fix
        withTimeoutOrNull(10_000L) { fusedCurrent() }?.let { return@withContext it }

        // 2. Fused, last known
        withTimeoutOrNull(3_000L) { fusedLast() }?.let { return@withContext it }

        // 3. Platform LocationManager
        val cached = bestLastKnown()
        if (cached != null && System.currentTimeMillis() - cached.time < 10 * 60 * 1000L) {
            return@withContext cached
        }
        for (provider in enabledProviders()) {
            withTimeoutOrNull(12_000L) { managerFreshFix(provider) }?.let { return@withContext it }
        }

        // 4. Any cached fix (city-level accuracy is enough for prayer times)
        cached
    }
}
