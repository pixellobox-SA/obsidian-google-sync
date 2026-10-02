package app.standbyclock.ui

import android.text.format.DateFormat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.standbyclock.data.ClockSettings
import app.standbyclock.data.Weather
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Main page: battery, greeting, big time and date on the left; analogue clock and
 * weather widgets on the right; day/night toggle between them.
 */
@Composable
fun MainPage(
    now: LocalDateTime,
    weather: Weather?,
    settings: ClockSettings,
    palette: Palette,
    night: Boolean,
    onToggleNight: () -> Unit,
) {
    val battery by rememberBattery()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Two square widgets stacked on the right, together as tall as the screen allows.
        val gap = 16.dp
        val widget = minOf((maxHeight - gap) / 2, maxWidth * 0.28f)

        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
                val density = LocalDensity.current
                val digits = with(density) {
                    minOf(maxWidth / (if (settings.use24h) 2.7f else 3.15f), maxHeight * 0.44f).toSp()
                }
                val small = digits * 0.105f

                Column(Modifier.align(Alignment.CenterStart)) {
                    battery?.let { b ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicText("${b.percent}%", style = label(small, palette.primary, FontWeight.Normal))
                            Spacer(Modifier.width(10.dp))
                            val iconH = with(density) { (small * 0.9f).toDp() }
                            BatteryIcon(b.percent, b.charging, palette.primary, Modifier.size(iconH * 2.1f, iconH))
                        }
                        Spacer(Modifier.height(with(density) { (small * 0.4f).toDp() }))
                    }
                    BasicText(
                        text = greeting(now, settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = label(small * 1.1f, palette.primary, FontWeight.Medium),
                    )
                    BigTime(now, settings.use24h, palette, digits)
                    BasicText(
                        text = now.format(rememberDateFormat()),
                        maxLines = 1,
                        modifier = Modifier.padding(start = with(density) { (digits * 0.03f).toDp() }),
                        style = label(digits * 0.17f, palette.primary, FontWeight.Medium),
                    )
                }

                MoonToggle(
                    night = night,
                    palette = palette,
                    onToggle = onToggleNight,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp).size(48.dp),
                )
            }
            Spacer(Modifier.width(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                AnalogClockWidget(palette, Modifier.size(widget))
                WeatherWidget(weather, palette, Modifier.size(widget))
            }
        }
    }
}

@Composable
private fun BigTime(now: LocalDateTime, use24h: Boolean, palette: Palette, digits: TextUnit) {
    val timeFormat = remember(use24h) { DateTimeFormatter.ofPattern(if (use24h) "HH:mm" else "h:mm") }
    val amPmFormat = remember { DateTimeFormatter.ofPattern("a", Locale.getDefault()) }
    Row(verticalAlignment = Alignment.Bottom) {
        // Soft fade whenever the minute changes.
        Crossfade(targetState = now.format(timeFormat), animationSpec = tween(900), label = "time") {
            BasicText(
                text = it,
                maxLines = 1,
                style = TextStyle(
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = digits,
                    lineHeight = digits * 1.05f,
                    letterSpacing = (-0.03).em,
                    fontFeatureSettings = "tnum",
                    color = palette.primary,
                    shadow = TextShadow,
                ),
            )
        }
        if (!use24h) {
            Spacer(Modifier.width(10.dp))
            BasicText(
                text = now.format(amPmFormat),
                modifier = Modifier.padding(bottom = with(LocalDensity.current) { (digits * 0.16f).toDp() }),
                style = label(digits * 0.16f, palette.secondary, FontWeight.Medium),
            )
        }
    }
}

@Composable
private fun rememberDateFormat(): DateTimeFormatter {
    val locale = Locale.getDefault()
    return remember(locale) {
        runCatching {
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEEddMMMM"), locale)
        }.getOrElse { DateTimeFormatter.ofPattern("EEEE, dd MMMM", locale) }
    }
}

/** The user's own message, or "Good morning, Name" depending on the time of day. */
private fun greeting(now: LocalDateTime, settings: ClockSettings): String {
    if (settings.message.isNotBlank()) return settings.message
    val part = when (now.hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..21 -> "Good evening"
        else -> "Good night"
    }
    return if (settings.userName.isBlank()) part else "$part, ${settings.userName}"
}
