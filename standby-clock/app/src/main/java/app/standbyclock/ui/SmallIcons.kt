package app.standbyclock.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos

/**
 * A slow "breathing" opacity (one breath every 3 s). Updated ~15 times a second and only
 * read at draw time, so it never recomposes anything and costs next to nothing.
 */
@Composable
fun rememberBreath(enabled: Boolean): State<Float> {
    val alpha = remember { mutableFloatStateOf(1f) }
    LaunchedEffect(enabled) {
        if (!enabled) {
            alpha.floatValue = 1f
            return@LaunchedEffect
        }
        val start = System.nanoTime()
        while (true) {
            val t = (System.nanoTime() - start) / 1e9
            alpha.floatValue = 0.45f + 0.55f * ((1 + cos(2 * PI * t / 3.0)) / 2).toFloat()
            delay(66)
        }
    }
    return alpha
}

/** Battery outline with the charge level and a lightning bolt while charging. */
@Composable
fun BatteryIcon(percent: Int, charging: Boolean, color: Color, modifier: Modifier = Modifier) {
    val breath = rememberBreath(charging)
    Canvas(modifier.graphicsLayer { alpha = breath.value }) {
        val h = size.height
        val stroke = h * 0.09f
        val bodyW = size.width - h * 0.14f
        val radius = CornerRadius(h * 0.22f)
        drawRoundRect(
            color,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(bodyW - stroke, h - stroke),
            cornerRadius = radius,
            style = Stroke(stroke),
        )
        // Little nub on the right.
        drawRoundRect(
            color,
            topLeft = Offset(bodyW + h * 0.02f, h * 0.32f),
            size = Size(h * 0.1f, h * 0.36f),
            cornerRadius = CornerRadius(h * 0.05f),
        )
        // Charge level.
        val inset = stroke * 1.8f
        val fullW = bodyW - inset * 2
        drawRoundRect(
            color.copy(alpha = 0.45f),
            topLeft = Offset(inset, inset),
            size = Size(fullW * (percent.coerceIn(0, 100) / 100f), h - inset * 2),
            cornerRadius = CornerRadius(h * 0.1f),
        )
        if (charging) {
            val cx = bodyW / 2
            val bolt = Path().apply {
                moveTo(cx + h * 0.08f, h * 0.14f)
                lineTo(cx - h * 0.18f, h * 0.55f)
                lineTo(cx + h * 0.0f, h * 0.55f)
                lineTo(cx - h * 0.08f, h * 0.86f)
                lineTo(cx + h * 0.18f, h * 0.45f)
                lineTo(cx + h * 0.0f, h * 0.45f)
                close()
            }
            drawPath(bolt, color)
        }
    }
}

/** Round day/night toggle with a crescent moon; filled while night mode is on. */
@Composable
fun MoonToggle(night: Boolean, palette: Palette, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(CircleShape)
            .background(if (night) palette.accent.copy(alpha = 0.18f) else palette.widget)
            .border(1.5.dp, palette.accent, CircleShape)
            .clickable(onClick = onToggle)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val r = size.minDimension / 2
            val c = center
            val disc = Path().apply { addOval(Rect(c, r)) }
            val bite = Path().apply { addOval(Rect(Offset(c.x + r * 0.55f, c.y - r * 0.35f), r * 0.9f)) }
            val moon = Path().apply { op(disc, bite, PathOperation.Difference) }
            drawPath(moon, palette.accent)
        }
    }
}
