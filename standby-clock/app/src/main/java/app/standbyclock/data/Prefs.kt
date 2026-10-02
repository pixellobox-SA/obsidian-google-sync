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
    val background: BackgroundMode,
    val userName: String,
    val message: String,
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

    var background: BackgroundMode
        get() = runCatching { BackgroundMode.valueOf(sp.getString(KEY_BACKGROUND, null)!!) }
            .getOrDefault(BackgroundMode.WALLPAPER)
        set(v) = sp.edit().putString(KEY_BACKGROUND, v.name).apply()

    /** Shown in the greeting above the time. */
    var userName: String
        get() = sp.getString(KEY_NAME, "") ?: ""
        set(v) = sp.edit().putString(KEY_NAME, v.trim()).apply()

    /** Custom line above the time; empty means an automatic "Good morning" style greeting. */
    var message: String
        get() = sp.getString(KEY_MESSAGE, "") ?: ""
        set(v) = sp.edit().putString(KEY_MESSAGE, v.trim()).apply()

    /** Turn on Do Not Disturb while the clock is up. */
    var dnd: Boolean
        get() = sp.getBoolean(KEY_DND, false)
        set(v) = sp.edit().putBoolean(KEY_DND, v).apply()

    /** True while we are the ones holding Do Not Disturb on; [dndPrevious] is what to restore. */
    var dndActive: Boolean
        get() = sp.getBoolean(KEY_DND_ACTIVE, false)
        set(v) = sp.edit().putBoolean(KEY_DND_ACTIVE, v).apply()

    var dndPrevious: Int
        get() = sp.getInt(KEY_DND_PREVIOUS, 1)
        set(v) = sp.edit().putInt(KEY_DND_PREVIOUS, v).apply()

    var todosJson: String?
        get() = sp.getString(KEY_TODOS, null)
        set(v) = sp.edit().putString(KEY_TODOS, v).apply()

    var shortcutsJson: String?
        get() = sp.getString(KEY_SHORTCUTS, null)
        set(v) = sp.edit().putString(KEY_SHORTCUTS, v).apply()

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
        background = background,
        userName = userName,
        message = message,
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
        const val KEY_BACKGROUND = "background"
        const val KEY_NAME = "user_name"
        const val KEY_MESSAGE = "message"
        const val KEY_DND = "dnd"
        const val KEY_DND_ACTIVE = "dnd_active"
        const val KEY_DND_PREVIOUS = "dnd_previous"
        const val KEY_TODOS = "todos"
        const val KEY_SHORTCUTS = "shortcuts"
        const val KEY_WEATHER = "weather_cache"
        val FAHRENHEIT_COUNTRIES = setOf("US", "LR", "MM", "BS", "BZ", "KY", "PW")
    }
}

data class Place(val name: String, val lat: Double, val lon: Double)
