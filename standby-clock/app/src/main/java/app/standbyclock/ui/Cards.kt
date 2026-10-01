package app.standbyclock.ui

import android.text.format.DateFormat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.standbyclock.data.Weather
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Soft shadow so white text stays readable on a bright wallpaper. */
private val TextShadow = Shadow(Color.Black.copy(alpha = 0.45f), Offset(0f, 2f), blurRadius = 18f)

/** The big left-aligned time with the date underneath. Fills the space it is given. */
@Composable
fun TimeAndDate(now: LocalDateTime, use24h: Boolean, palette: Palette, modifier: Modifier = Modifier) {
    val locale = Locale.getDefault()
    val timeFormat = remember(use24h) { DateTimeFormatter.ofPattern(if (use24h) "HH:mm" else "h:mm") }
    val amPmFormat = remember(locale) { DateTimeFormatter.ofPattern("a", locale) }
    val dateFormat = remember(locale) {
        runCatching {
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEEMMMMd"), locale)
        }.getOrElse { DateTimeFormatter.ofPattern("EEEE, MMMM d", locale) }
    }

    // Sits a little below the middle, like StandBy.
    BoxWithConstraints(modifier, contentAlignment = BiasAlignment(-1f, 0.3f)) {
        val digits = with(LocalDensity.current) {
            minOf(maxWidth / (if (use24h) 2.75f else 3.2f), maxHeight * 0.5f).toSp()
        }
        Column(horizontalAlignment = Alignment.Start) {
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
                            lineHeight = digits,
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
                        modifier = Modifier.padding(bottom = with(LocalDensity.current) { (digits * 0.14f).toDp() }),
                        style = label(digits * 0.16f, palette.secondary, FontWeight.Medium),
                    )
                }
            }
            BasicText(
                text = now.format(dateFormat),
                maxLines = 1,
                modifier = Modifier.padding(start = with(LocalDensity.current) { (digits * 0.04f).toDp() }),
                style = label(digits * 0.15f, palette.primary, FontWeight.Medium),
            )
        }
    }
}

/** Analogue clock in a widget panel. */
@Composable
fun AnalogClockWidget(palette: Palette, modifier: Modifier = Modifier) {
    Widget(palette, modifier) {
        AnalogClock(palette, Modifier.fillMaxSize())
    }
}

/** Town, icon, temperature, condition and high/low in a widget panel. */
@Composable
fun WeatherWidget(weather: Weather?, palette: Palette, modifier: Modifier = Modifier) {
    Widget(palette, modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val base = minOf(maxWidth, maxHeight)
            val small = with(density) { (base * 0.095f).toSp() }
            val big = with(density) { (base * 0.30f).toSp() }

            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                BasicText(
                    text = weather?.place?.takeIf { it.isNotBlank() } ?: "Weather",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = label(small, palette.primary, FontWeight.Medium),
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (weather != null) {
                        WeatherIcon(weather.code, weather.isDay, palette.primary, Modifier.size(base * 0.26f))
                        Spacer(Modifier.width(base * 0.06f))
                    }
                    BasicText(
                        text = weather?.let { "${it.temperature.roundToInt()}°" } ?: "—°",
                        maxLines = 1,
                        style = label(big, palette.primary, FontWeight.Light).copy(fontFeatureSettings = "tnum"),
                    )
                }

                Column {
                    BasicText(
                        text = weather?.let { conditionName(it.code) } ?: "No weather yet",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = label(small, palette.primary, FontWeight.Medium),
                    )
                    Spacer(Modifier.height(base * 0.02f))
                    BasicText(
                        text = weather?.let { "H:${it.high.roundToInt()}°  L:${it.low.roundToInt()}°" }
                            ?: "Open the app to set a location",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = label(small, palette.secondary, FontWeight.Normal),
                    )
                }
            }
        }
    }
}

private fun label(size: TextUnit, color: Color, weight: FontWeight) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size,
    color = color,
    shadow = TextShadow,
)
