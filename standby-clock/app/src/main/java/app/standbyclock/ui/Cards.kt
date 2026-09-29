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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.standbyclock.data.Weather
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ClockCard(now: LocalDateTime, use24h: Boolean, palette: Palette, modifier: Modifier = Modifier) {
    val locale = Locale.getDefault()
    val timeFormat = remember(use24h) { DateTimeFormatter.ofPattern(if (use24h) "HH:mm" else "h:mm") }
    val amPmFormat = remember(locale) { DateTimeFormatter.ofPattern("a", locale) }
    val dateFormat = remember(locale) {
        runCatching {
            DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM"), locale)
        }.getOrElse { DateTimeFormatter.ofPattern("EEEE, d MMMM", locale) }
    }

    GlassCard(palette, modifier) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val digits = with(LocalDensity.current) {
                minOf(maxWidth / (if (use24h) 2.85f else 3.3f), maxHeight * 0.55f).toSp()
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    // Soft fade whenever the minute changes.
                    Crossfade(targetState = now.format(timeFormat), animationSpec = tween(900), label = "time") {
                        BasicText(
                            text = it,
                            maxLines = 1,
                            style = TextStyle(
                                fontFamily = Inter,
                                fontWeight = FontWeight.ExtraLight,
                                fontSize = digits,
                                lineHeight = digits,
                                letterSpacing = (-0.02).em,
                                fontFeatureSettings = "tnum",
                                color = palette.primary,
                            ),
                        )
                    }
                    if (!use24h) {
                        Spacer(Modifier.width(10.dp))
                        BasicText(
                            text = now.format(amPmFormat),
                            modifier = Modifier.padding(bottom = with(LocalDensity.current) { (digits * 0.12f).toDp() }),
                            style = label(digits * 0.16f, palette, FontWeight.Light).copy(color = palette.secondary),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                BasicText(
                    text = now.format(dateFormat),
                    maxLines = 1,
                    style = label(20.sp, palette, FontWeight.Light).copy(
                        color = palette.secondary,
                        letterSpacing = 0.02.em,
                    ),
                )
            }
        }
    }
}

@Composable
fun WeatherCard(weather: Weather?, palette: Palette, modifier: Modifier = Modifier) {
    GlassCard(palette, modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val tempSize = with(LocalDensity.current) { minOf(maxWidth * 0.3f, maxHeight * 0.3f, 76.dp).toSp() }
            val iconSize = with(LocalDensity.current) { (tempSize * 0.85f).toDp() }

            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                BasicText(
                    text = (weather?.place?.takeIf { it.isNotBlank() } ?: "Weather").uppercase(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = label(12.sp, palette, FontWeight.Normal).copy(
                        color = palette.tertiary,
                        letterSpacing = 0.2.em,
                    ),
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (weather != null) {
                            WeatherIcon(weather.code, weather.isDay, palette.accent, Modifier.size(iconSize))
                            Spacer(Modifier.width(12.dp))
                        }
                        BasicText(
                            text = weather?.let { "${it.temperature.roundToInt()}°" } ?: "—°",
                            maxLines = 1,
                            style = label(tempSize, palette, FontWeight.ExtraLight).copy(fontFeatureSettings = "tnum"),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        text = weather?.let { conditionName(it.code) } ?: "No weather yet",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = label(17.sp, palette, FontWeight.Light).copy(color = palette.secondary),
                    )
                }

                BasicText(
                    text = weather?.let { "H ${it.high.roundToInt()}°    L ${it.low.roundToInt()}°" }
                        ?: "Open the app to set a location",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = label(14.sp, palette, FontWeight.Normal).copy(
                        color = palette.tertiary,
                        letterSpacing = 0.04.em,
                    ),
                )
            }
        }
    }
}

private fun label(size: TextUnit, palette: Palette, weight: FontWeight) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size,
    color = palette.primary,
)
