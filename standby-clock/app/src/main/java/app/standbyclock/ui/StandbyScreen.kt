package app.standbyclock.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import app.standbyclock.data.Quotes
import app.standbyclock.data.Shortcut
import app.standbyclock.data.Shortcuts
import app.standbyclock.data.Todos
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.standbyclock.data.BackgroundMode
import app.standbyclock.data.BackgroundPhoto
import app.standbyclock.data.ClockSettings

/**
 * The StandBy screen: a full-screen background with three swipeable pages on top:
 * calendar + to-do (left), the clock (middle, shown first) and quote + quick actions
 * (right). Pages that aren't on screen aren't drawn. Screen brightness is never touched.
 */
@Composable
fun StandbyScreen(settings: ClockSettings, onLaunch: (Shortcut) -> Unit, onOpenClock: () -> Unit) {
    val context = LocalContext.current
    val now by rememberMinuteClock()
    val weather by rememberWeather()

    // Night colours follow the schedule until the moon button is tapped.
    var nightOverride by remember { mutableStateOf<Boolean?>(null) }
    val scheduledNight = settings.nightMode && isNightHour(now.hour, settings.nightStartHour, settings.nightEndHour)
    val night = nightOverride ?: scheduledNight
    val palette = animatedPalette(night, settings.dayColor, settings.nightTone)

    var todos by remember { mutableStateOf(Todos.load(context)) }
    val shortcuts = remember { Shortcuts.load(context) }
    val today = now.toLocalDate()
    val quote = remember(today) { Quotes.ofTheWeek(today) }

    // Gentle fade-in when the clock opens.
    val fadeIn = remember { Animatable(0f) }
    LaunchedEffect(Unit) { fadeIn.animateTo(1f, tween(1200)) }

    // Nudge everything by a few pixels each minute so nothing burns into an OLED panel.
    val shiftX by animateDpAsState((((now.minute % 5) - 2) * 3).dp, tween(2000), label = "shiftX")
    val shiftY by animateDpAsState(((((now.minute / 5) % 3) - 1) * 3).dp, tween(2000), label = "shiftY")

    val pager = rememberPagerState(initialPage = 1) { 3 }

    Box(Modifier.fillMaxSize()) {
        Background(settings.background, Color(settings.backgroundColor), palette)

        HorizontalPager(
            state = pager,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = fadeIn.value }
                .offset(shiftX, shiftY),
        ) { page ->
            Box(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 28.dp)) {
                when (page) {
                    0 -> CalendarPage(today, todos, palette) { i ->
                        todos = todos.toMutableList().also { it[i] = it[i].copy(done = !it[i].done) }
                        Todos.save(context, todos)
                    }
                    1 -> MainPage(now, weather, settings, palette, night, onOpenClock) { nightOverride = !night }
                    else -> QuotePage(quote, shortcuts, palette, onLaunch)
                }
            }
        }

        PageDots(pager.currentPage, 3, palette, Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp))
    }
}

@Composable
private fun PageDots(current: Int, count: Int, palette: Palette, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { i ->
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (i == current) palette.primary else palette.tertiary.copy(alpha = 0.6f)),
            )
        }
    }
}

/** Wallpaper, photo, colour or black, darkened just enough for the text to stay readable. */
@Composable
private fun Background(mode: BackgroundMode, color: Color, palette: Palette) {
    val context = LocalContext.current
    val photo by produceState<ImageBitmap?>(null, mode) {
        value = if (mode == BackgroundMode.PHOTO) BackgroundPhoto.load(context) else null
    }

    when {
        mode == BackgroundMode.BLACK -> Box(Modifier.fillMaxSize().background(Color.Black))
        // A soft top-to-bottom fade of the chosen colour gives it a little depth.
        mode == BackgroundMode.COLOR -> Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(color, lerp(color, Color.Black, 0.35f))),
            ),
        )
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
        // A chosen colour already has the brightness the user wants, so darken it less.
        val scrim = if (mode == BackgroundMode.COLOR) palette.scrim * 0.5f else palette.scrim
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = scrim)))
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
