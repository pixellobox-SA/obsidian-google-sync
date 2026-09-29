package app.standbyclock.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * Approximate location only (ACCESS_COARSE_LOCATION). Coordinates are also rounded
 * to two decimals (~1 km) before they are stored or sent anywhere.
 */
object LocationProvider {

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun approximatePlace(context: Context): Place? {
        if (!hasPermission(context)) return null
        val lm = context.getSystemService(LocationManager::class.java) ?: return null

        val location = try {
            fresh(context, lm) ?: lastKnown(lm)
        } catch (_: SecurityException) {
            null
        } ?: return null

        val lat = round2(location.latitude)
        val lon = round2(location.longitude)
        return Place(name = placeName(context, lat, lon) ?: "", lat = lat, lon = lon)
    }

    @SuppressLint("MissingPermission")
    private suspend fun fresh(context: Context, lm: LocationManager): Location? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) return null
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                lm.getCurrentLocation(
                    LocationManager.NETWORK_PROVIDER,
                    signal,
                    context.mainExecutor,
                ) { loc -> if (cont.isActive) cont.resume(loc) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnown(lm: LocationManager): Location? =
        listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

    /** Uses the phone's built-in geocoder to turn coordinates into a town name. */
    private suspend fun placeName(context: Context, lat: Double, lon: Double): String? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            runCatching {
                @Suppress("DEPRECATION")
                Geocoder(context, Locale.getDefault()).getFromLocation(lat, lon, 1)
                    ?.firstOrNull()
                    ?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
            }.getOrNull()
        }

    private fun round2(v: Double) = (v * 100).roundToInt() / 100.0
}
