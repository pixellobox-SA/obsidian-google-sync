package app.standbyclock.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.standbyclock.data.Weather
import app.standbyclock.data.WeatherRepository
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * The current time, updated once per minute by the system's own TIME_TICK broadcast
 * (plus manual time / time-zone changes). No per-second timers.
 */
@Composable
fun rememberMinuteClock(): State<LocalDateTime> {
    val context = LocalContext.current
    val now = remember { mutableStateOf(LocalDateTime.now()) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                now.value = LocalDateTime.now()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        // Only protected system broadcasts, so no export flag is required.
        context.registerReceiver(receiver, filter)
        now.value = LocalDateTime.now()
        onDispose { context.unregisterReceiver(receiver) }
    }
    return now
}

/** Cached weather straight away, then a refresh whenever the 30-minute window is up. */
@Composable
fun rememberWeather(): State<Weather?> {
    val context = LocalContext.current.applicationContext
    val weather = remember { mutableStateOf(WeatherRepository.cached(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            WeatherRepository.refresh(context)?.let { weather.value = it }
            val dueIn = weather.value?.let {
                it.fetchedAt + WeatherRepository.REFRESH_INTERVAL_MS - System.currentTimeMillis()
            } ?: 0L
            delay(if (dueIn > 0) dueIn + 1_000 else 5 * 60 * 1000L)
        }
    }
    return weather
}

data class BatteryInfo(val percent: Int, val charging: Boolean)

/** Battery level from the system's own battery broadcast; it only fires when something changes. */
@Composable
fun rememberBattery(): State<BatteryInfo?> {
    val context = LocalContext.current
    val battery = remember { mutableStateOf<BatteryInfo?>(null) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
                if (level >= 0 && scale > 0) {
                    battery.value = BatteryInfo(
                        percent = level * 100 / scale,
                        charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL,
                    )
                }
            }
        }
        // Sticky broadcast: the current value is delivered straight away.
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }
    return battery
}

/** True when [hour] falls inside the night window, which may wrap past midnight. */
fun isNightHour(hour: Int, start: Int, end: Int): Boolean = when {
    start == end -> false
    start < end -> hour in start until end
    else -> hour >= start || hour < end
}
