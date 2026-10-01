package app.standbyclock.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * A working analogue clock face: 12/3/6/9 numerals, hour ticks, and hour, minute and
 * second hands. Only this small canvas redraws each second.
 */
@Composable
fun AnalogClock(palette: Palette, modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = LocalTime.now()
            delay(1_000 - System.currentTimeMillis() % 1_000)
        }
    }
    val measurer = rememberTextMeasurer()

    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = center

        // Hour ticks, skipping the four numeral positions.
        for (i in 0 until 12) {
            if (i % 3 == 0) continue
            val a = i * 30.0
            drawLine(
                palette.tertiary,
                c + polar(a, r * 0.80f),
                c + polar(a, r * 0.90f),
                strokeWidth = r * 0.025f,
                cap = StrokeCap.Round,
            )
        }

        // Numerals 12, 3, 6, 9.
        val style = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = (r * 0.22f).toSp(),
            color = palette.secondary,
        )
        listOf(12 to 0.0, 3 to 90.0, 6 to 180.0, 9 to 270.0).forEach { (n, a) ->
            val layout = measurer.measure(n.toString(), style)
            val p = c + polar(a, r * 0.80f)
            drawText(
                layout,
                topLeft = Offset(p.x - layout.size.width / 2f, p.y - layout.size.height / 2f),
            )
        }

        val h = time.hour % 12 + time.minute / 60.0
        val m = time.minute + time.second / 60.0
        hand(h * 30.0, r * 0.48f, r * 0.075f, palette.accent)
        hand(m * 6.0, r * 0.70f, r * 0.05f, palette.accent)
        hand(time.second * 6.0, r * 0.78f, r * 0.018f, palette.primary, tail = r * 0.14f)
        drawCircle(palette.accent, radius = r * 0.065f, center = c)
        drawCircle(Color.Black, radius = r * 0.028f, center = c)
    }
}

private fun DrawScope.hand(degrees: Double, length: Float, width: Float, color: Color, tail: Float = 0f) {
    drawLine(
        color,
        center + polar(degrees + 180.0, tail),
        center + polar(degrees, length),
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}

/** Offset for an angle measured clockwise from 12 o'clock. */
private fun polar(degrees: Double, radius: Float): Offset {
    val a = Math.toRadians(degrees)
    return Offset((sin(a) * radius).toFloat(), (-cos(a) * radius).toFloat())
}
