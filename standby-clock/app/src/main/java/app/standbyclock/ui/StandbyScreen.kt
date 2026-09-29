package app.standbyclock.ui

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import app.standbyclock.data.ClockSettings

/** What the host window should do with screen brightness. */
enum class DisplayMode { NORMAL, NIGHT, HIDDEN }

/**
 * The whole StandBy screen: a big clock card and a small weather card on pure black.
 * Shared by the screen saver and the in-app preview.
 */
@Composable
fun StandbyScreen(settings: ClockSettings, onDisplayMode: (DisplayMode) -> Unit) {
    val now by rememberMinuteClock()
    val weather by rememberWeather()

    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hidden = settings.landscapeOnly && !landscape
    val night = settings.nightMode && isNightHour(now.hour, settings.nightStartHour, settings.nightEndHour)
    val mode = when {
        hidden -> DisplayMode.HIDDEN
        night -> DisplayMode.NIGHT
        else -> DisplayMode.NORMAL
    }
    LaunchedEffect(mode) { onDisplayMode(mode) }

    val palette = animatedPalette(night)

    // Gentle fade-in when the screen saver starts.
    val fadeIn = remember { Animatable(0f) }
    LaunchedEffect(Unit) { fadeIn.animateTo(1f, tween(1200)) }

    // Nudge everything by a few pixels each minute so nothing burns into an OLED panel.
    val shiftX by animateDpAsState((((now.minute % 5) - 2) * 3).dp, tween(2000), label = "shiftX")
    val shiftY by animateDpAsState(((((now.minute / 5) % 3) - 1) * 3).dp, tween(2000), label = "shiftY")

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        // In portrait with "landscape only" on, stay completely black.
        if (hidden) return@Box

        val content = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fadeIn.value }
            .offset(shiftX, shiftY)
            .padding(horizontal = 28.dp, vertical = 24.dp)

        AmbientGlow(palette.accent, Modifier.fillMaxSize().graphicsLayer { alpha = fadeIn.value })

        if (landscape) {
            Row(content, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ClockCard(now, settings.use24h, palette, Modifier.weight(1.75f).fillMaxHeight())
                WeatherCard(weather, palette, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(content, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                ClockCard(now, settings.use24h, palette, Modifier.weight(1.3f).fillMaxWidth())
                WeatherCard(weather, palette, Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}

/** Two very faint pools of accent light behind the cards, giving the glass some depth. */
@Composable
private fun AmbientGlow(accent: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun glow(center: Offset, radius: Float, alpha: Float) = drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = alpha), Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
        glow(Offset(w * 0.3f, h * 0.3f), w * 0.42f, 0.09f)
        glow(Offset(w * 0.85f, h * 0.8f), w * 0.28f, 0.06f)
    }
}
