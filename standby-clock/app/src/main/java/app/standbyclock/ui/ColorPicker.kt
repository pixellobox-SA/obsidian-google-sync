package app.standbyclock.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import android.graphics.Color as AndroidColor

/**
 * Colour wheel (hue around the edge, softer tints towards the middle) plus a
 * brightness slider from almost black to full colour, with a live preview.
 * [onPicked] is called when a touch or slide finishes, not on every movement.
 */
@Composable
fun ColorPicker(initial: Int, onPicked: (Int) -> Unit) {
    val start = remember(initial) { FloatArray(3).also { AndroidColor.colorToHSV(initial, it) } }
    var hue by remember { mutableFloatStateOf(start[0]) }
    var sat by remember { mutableFloatStateOf(start[1]) }
    var value by remember { mutableFloatStateOf(start[2].coerceAtLeast(0.05f)) }

    fun current() = AndroidColor.HSVToColor(floatArrayOf(hue, sat, value))
    fun pick(pos: Offset, size: IntSize) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = min(size.width, size.height) / 2f
        val d = pos - c
        hue = ((Math.toDegrees(atan2(d.y, d.x).toDouble()) + 360) % 360).toFloat()
        sat = (hypot(d.x, d.y) / r).coerceIn(0f, 1f)
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(
                Modifier
                    .size(200.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { pick(it, size); onPicked(current()) }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = { onPicked(current()) }) { change, _ ->
                            pick(change.position, size)
                        }
                    },
            ) {
                val r = size.minDimension / 2
                // Hue around the circle (0° = red at 3 o'clock, going clockwise like atan2).
                drawCircle(
                    Brush.sweepGradient(
                        listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
                        center,
                    ),
                    r,
                )
                // Fade to white in the middle: less saturated, softer tints.
                drawCircle(Brush.radialGradient(listOf(Color.White, Color.Transparent), center, r), r)
                // Preview the brightness on the wheel itself.
                drawCircle(Color.Black.copy(alpha = 1f - value), r)
                // Selection ring.
                val a = Math.toRadians(hue.toDouble())
                val p = center + Offset((cos(a) * sat * r).toFloat(), (sin(a) * sat * r).toFloat())
                drawCircle(Color.White, 10.dp.toPx(), p, style = Stroke(3.dp.toPx()))
                drawCircle(Color.Black, 12.dp.toPx(), p, style = Stroke(1.dp.toPx()))
            }
            Spacer(Modifier.width(20.dp))
            Box(
                Modifier
                    .size(64.dp)
                    .background(Color(current()), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Brightness",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
        )
        Slider(
            value = value,
            onValueChange = { value = it },
            onValueChangeFinished = { onPicked(current()) },
            valueRange = 0.05f..1f,
        )
    }
}
