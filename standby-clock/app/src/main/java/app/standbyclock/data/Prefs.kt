package app.standbyclock.data

import android.content.Context
import android.text.format.DateFormat
import java.util.Locale

/** User choices, read once when the clock starts. */
data class ClockSettings(
    val use24h: Boolean,
    val fahrenheit: Boolean,
    val nightMode: Boolean,
    val nightStartHour: Int,
    val nightEndHour: Int,
)

/** Tiny wrapper around SharedPreferences. Everything stays on the phone. */
class Prefs(context: Context) {
    private val appContext = context.applicationContext
    private val sp = appContext.getSharedPreferences("standby_clock", Context.MODE_PRIVATE)

    var use24h: Boolean
        get() = sp.getBoolean(KEY_24H, DateFormat.is24HourFormat(appContext))
        set(v) = sp.edit().putBoolean(KEY_24H, v).apply()

    var fahrenheit: Boolean
        get() = sp.getBoolean(KEY_FAHRENHEIT, Locale.getDefault().country in FAHRENHEIT_COUNTRIES)
        set(v) = sp.edit().putBoolean(KEY_FAHRENHEIT, v).apply()

    var nightMode: Boolean
        get() = sp.getBoolean(KEY_NIGHT, true)
        set(v) = sp.edit().putBoolean(KEY_NIGHT, v).apply()

    var nightStartHour: Int
        get() = sp.getInt(KEY_NIGHT_START, 22)
        set(v) = sp.edit().putInt(KEY_NIGHT_START, v.mod(24)).apply()

    var nightEndHour: Int
        get() = sp.getInt(KEY_NIGHT_END, 7)
        set(v) = sp.edit().putInt(KEY_NIGHT_END, v.mod(24)).apply()

    /** Open the clock by itself when charging wirelessly in landscape. */
    var autoStart: Boolean
        get() = sp.getBoolean(KEY_AUTO_START, true)
        set(v) = sp.edit().putBoolean(KEY_AUTO_START, v).apply()

    /** A city picked by hand. When set, the phone's location is not used at all. */
    var manualPlace: Place?
        get() = readPlace("manual")
        set(v) = writePlace("manual", v)

    /** Last approximate location we saw, used when a fresh one isn't available. */
    var lastPlace: Place?
        get() = readPlace("last")
        set(v) = writePlace("last", v)

    var weatherJson: String?
        get() = sp.getString(KEY_WEATHER, null)
        set(v) = sp.edit().putString(KEY_WEATHER, v).apply()

    fun snapshot() = ClockSettings(
        use24h = use24h,
        fahrenheit = fahrenheit,
        nightMode = nightMode,
        nightStartHour = nightStartHour,
        nightEndHour = nightEndHour,
    )

    private fun readPlace(prefix: String): Place? {
        if (!sp.contains("${prefix}_lat")) return null
        return Place(
            name = sp.getString("${prefix}_name", "") ?: "",
            lat = sp.getFloat("${prefix}_lat", 0f).toDouble(),
            lon = sp.getFloat("${prefix}_lon", 0f).toDouble(),
        )
    }

    private fun writePlace(prefix: String, place: Place?) {
        val e = sp.edit()
        if (place == null) {
            e.remove("${prefix}_name").remove("${prefix}_lat").remove("${prefix}_lon")
        } else {
            e.putString("${prefix}_name", place.name)
                .putFloat("${prefix}_lat", place.lat.toFloat())
                .putFloat("${prefix}_lon", place.lon.toFloat())
        }
        e.apply()
    }

    private companion object {
        const val KEY_24H = "use_24h"
        const val KEY_FAHRENHEIT = "fahrenheit"
        const val KEY_NIGHT = "night_mode"
        const val KEY_NIGHT_START = "night_start"
        const val KEY_NIGHT_END = "night_end"
        const val KEY_AUTO_START = "auto_start"
        const val KEY_WEATHER = "weather_cache"
        val FAHRENHEIT_COUNTRIES = setOf("US", "LR", "MM", "BS", "BZ", "KY", "PW")
    }
}

data class Place(val name: String, val lat: Double, val lon: Double)
