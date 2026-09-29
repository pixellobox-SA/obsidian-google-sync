package app.standbyclock.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class Weather(
    val temperature: Double,
    val high: Double,
    val low: Double,
    val code: Int,
    val isDay: Boolean,
    val place: String,
    val fahrenheit: Boolean,
    val fetchedAt: Long,
) {
    fun toJson(): String = JSONObject()
        .put("t", temperature).put("hi", high).put("lo", low)
        .put("code", code).put("day", isDay).put("place", place)
        .put("f", fahrenheit).put("at", fetchedAt)
        .toString()

    companion object {
        fun fromJson(s: String): Weather? = runCatching {
            val o = JSONObject(s)
            Weather(
                temperature = o.getDouble("t"),
                high = o.getDouble("hi"),
                low = o.getDouble("lo"),
                code = o.getInt("code"),
                isDay = o.getBoolean("day"),
                place = o.optString("place"),
                fahrenheit = o.getBoolean("f"),
                fetchedAt = o.getLong("at"),
            )
        }.getOrNull()
    }
}

/**
 * Weather from Open-Meteo (free, no key, no account). The last result is cached and
 * a new one is fetched at most once every 30 minutes.
 */
object WeatherRepository {
    const val REFRESH_INTERVAL_MS = 30 * 60 * 1000L
    private const val RETRY_AFTER_FAILURE_MS = 5 * 60 * 1000L

    @Volatile
    private var lastAttempt = 0L

    fun cached(context: Context): Weather? =
        Prefs(context).weatherJson?.let { Weather.fromJson(it) }

    /**
     * Returns fresh-enough weather, downloading only when the cache is older than
     * 30 minutes (or when [force] is set after the user changes a setting).
     */
    suspend fun refresh(context: Context, force: Boolean = false): Weather? {
        val prefs = Prefs(context)
        val cached = cached(context)
        val now = System.currentTimeMillis()

        if (!force && cached != null && cached.fahrenheit == prefs.fahrenheit &&
            now - cached.fetchedAt < REFRESH_INTERVAL_MS
        ) return cached
        if (!force && now - lastAttempt < RETRY_AFTER_FAILURE_MS) return cached
        lastAttempt = now

        val place = prefs.manualPlace
            ?: LocationProvider.approximatePlace(context)?.let { found ->
                // Keep the previous town name if the geocoder couldn't name this spot.
                val named = if (found.name.isBlank()) {
                    found.copy(name = prefs.lastPlace?.takeIf { it.lat == found.lat && it.lon == found.lon }?.name ?: "")
                } else found
                named.also { prefs.lastPlace = it }
            }
            ?: prefs.lastPlace
            ?: return cached

        val weather = fetch(place, prefs.fahrenheit) ?: return cached
        prefs.weatherJson = weather.toJson()
        return weather
    }

    /** Looks up a city by name with Open-Meteo's geocoding API. */
    suspend fun searchCity(name: String): Place? {
        val q = URLEncoder.encode(name.trim(), "UTF-8")
        val lang = Locale.getDefault().language.ifBlank { "en" }
        val body = httpGet(
            "https://geocoding-api.open-meteo.com/v1/search?name=$q&count=1&language=$lang&format=json",
        ) ?: return null
        return runCatching {
            val r = JSONObject(body).getJSONArray("results").getJSONObject(0)
            Place(name = r.getString("name"), lat = r.getDouble("latitude"), lon = r.getDouble("longitude"))
        }.getOrNull()
    }

    private suspend fun fetch(place: Place, fahrenheit: Boolean): Weather? {
        val url = buildString {
            append("https://api.open-meteo.com/v1/forecast")
            append(String.format(Locale.US, "?latitude=%.2f&longitude=%.2f", place.lat, place.lon))
            append("&current=temperature_2m,weather_code,is_day")
            append("&daily=temperature_2m_max,temperature_2m_min")
            append("&timezone=auto&forecast_days=1")
            if (fahrenheit) append("&temperature_unit=fahrenheit")
        }
        val body = httpGet(url) ?: return null
        return runCatching {
            val o = JSONObject(body)
            val current = o.getJSONObject("current")
            val daily = o.getJSONObject("daily")
            Weather(
                temperature = current.getDouble("temperature_2m"),
                high = daily.getJSONArray("temperature_2m_max").getDouble(0),
                low = daily.getJSONArray("temperature_2m_min").getDouble(0),
                code = current.getInt("weather_code"),
                isDay = current.getInt("is_day") == 1,
                place = place.name,
                fahrenheit = fahrenheit,
                fetchedAt = System.currentTimeMillis(),
            )
        }.getOrNull()
    }

    private suspend fun httpGet(url: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(url).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                if (conn.responseCode != 200) return@runCatching null
                conn.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }
}
