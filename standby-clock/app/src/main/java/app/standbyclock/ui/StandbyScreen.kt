package app.standbyclock.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.standbyclock.data.BackgroundMode
import app.standbyclock.data.BackgroundPhoto
import app.standbyclock.data.ClockSettings

/**
 * The StandBy screen: a full-screen background picture, the big time and date on the
 * left, and an analogue clock widget above a weather widget on the right.
 * Screen brightness is never touched: night mode only changes the colours.
 */
@Composable
fun StandbyScreen(settings: ClockSettings) {
    val now by rememberMinuteClock()
    val weather by rememberWeather()
    val night = settings.nightMode && isNightHour(now.hour, settings.nightStartHour, settings.nightEndHour)
    val palette = animatedPalette(night)

    // Gentle fade-in when the clock opens.
    val fadeIn = remember { Animatable(0f) }
    LaunchedEffect(Unit) { fadeIn.animateTo(1f, tween(1200)) }

    // Nudge everything by a few pixels each minute so nothing burns into an OLED panel.
    val shiftX by animateDpAsState((((now.minute % 5) - 2) * 3).dp, tween(2000), label = "shiftX")
    val shiftY by animateDpAsState(((((now.minute / 5) % 3) - 1) * 3).dp, tween(2000), label = "shiftY")

    Box(Modifier.fillMaxSize()) {
        Background(settings.background, palette)

        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = fadeIn.value }
                .offset(shiftX, shiftY)
                .padding(horizontal = 40.dp, vertical = 28.dp),
        ) {
            // Two square widgets stacked on the right, together as tall as the screen allows.
            val gap = 16.dp
            val widget = minOf((maxHeight - gap) / 2, maxWidth * 0.3f)

            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                TimeAndDate(now, settings.use24h, palette, Modifier.weight(1f).fillMaxHeight())
                Spacer(Modifier.width(28.dp))
                Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                    AnalogClockWidget(palette, Modifier.size(widget))
                    WeatherWidget(weather, palette, Modifier.size(widget))
                }
            }
        }
    }
}

/** Wallpaper, photo or black, darkened just enough for the text to stay readable. */
@Composable
private fun Background(mode: BackgroundMode, palette: Palette) {
    val context = LocalContext.current
    val photo by produceState<ImageBitmap?>(null, mode) {
        value = if (mode == BackgroundMode.PHOTO) BackgroundPhoto.load(context) else null
    }

    when {
        mode == BackgroundMode.BLACK -> Box(Modifier.fillMaxSize().background(Color.Black))
        mode == BackgroundMode.PHOTO && photo != null -> Image(
            bitmap = photo!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        mode == BackgroundMode.PHOTO -> Box(Modifier.fillMaxSize().background(Color.Black))
        // WALLPAPER: the window is see-through and the system draws the wallpaper behind it.
        else -> Unit
    }

    if (mode != BackgroundMode.BLACK) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = palette.scrim)))
        // Extra shade on the left, behind the big time.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.35f),
                        0.6f to Color.Transparent,
                    ),
                ),
        )
    }
}
