package app.standbyclock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import app.standbyclock.data.Weather
import kotlin.math.roundToInt

/** Soft shadow so white text stays readable on a bright wallpaper. */
internal val TextShadow = Shadow(Color.Black.copy(alpha = 0.45f), Offset(0f, 2f), blurRadius = 18f)

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

internal fun label(size: TextUnit, color: Color, weight: FontWeight) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size,
    color = color,
    shadow = TextShadow,
)
