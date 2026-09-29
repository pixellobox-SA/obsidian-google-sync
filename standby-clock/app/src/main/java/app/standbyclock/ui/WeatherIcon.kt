package app.standbyclock.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.cos
import kotlin.math.sin

enum class Sky { CLEAR, PARTLY, CLOUDY, FOG, DRIZZLE, RAIN, SNOW, THUNDER }

/** WMO weather codes as used by Open-Meteo. */
fun skyFor(code: Int): Sky = when (code) {
    0 -> Sky.CLEAR
    1, 2 -> Sky.PARTLY
    3 -> Sky.CLOUDY
    45, 48 -> Sky.FOG
    51, 53, 55, 56, 57 -> Sky.DRIZZLE
    61, 63, 65, 66, 67, 80, 81, 82 -> Sky.RAIN
    71, 73, 75, 77, 85, 86 -> Sky.SNOW
    95, 96, 99 -> Sky.THUNDER
    else -> Sky.CLOUDY
}

fun conditionName(code: Int): String = when (code) {
    0 -> "Clear"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61 -> "Light rain"
    63 -> "Rain"
    65 -> "Heavy rain"
    66, 67 -> "Freezing rain"
    71 -> "Light snow"
    73 -> "Snow"
    75 -> "Heavy snow"
    77 -> "Snow grains"
    80, 81 -> "Showers"
    82 -> "Heavy showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm, hail"
    else -> "—"
}

/** Thin line-art weather icon drawn on a Canvas (no icon library needed). */
@Composable
fun WeatherIcon(code: Int, isDay: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val stroke = Stroke(width = s * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (skyFor(code)) {
            Sky.CLEAR -> {
                if (isDay) sun(Offset(s / 2, s / 2), s * 0.2f, color, stroke)
                else moon(Offset(s / 2, s / 2), s * 0.28f, color, stroke)
            }

            Sky.PARTLY -> {
                val cloud = cloudPath(s * 0.22f, s * 0.36f, s * 0.72f, s * 0.48f)
                clipPath(cloud, ClipOp.Difference) {
                    if (isDay) sun(Offset(s * 0.36f, s * 0.34f), s * 0.13f, color, stroke)
                    else moon(Offset(s * 0.36f, s * 0.34f), s * 0.19f, color, stroke)
                }
                drawPath(cloud, color, style = stroke)
            }

            Sky.CLOUDY -> drawPath(cloudPath(s * 0.1f, s * 0.24f, s * 0.8f, s * 0.54f), color, style = stroke)

            Sky.FOG -> {
                drawPath(cloudPath(s * 0.14f, s * 0.12f, s * 0.72f, s * 0.48f), color, style = stroke)
                for (i in 0..1) {
                    val y = s * (0.74f + i * 0.13f)
                    drawLine(color, Offset(s * (0.2f + i * 0.08f), y), Offset(s * (0.8f - i * 0.08f), y), stroke.width, StrokeCap.Round)
                }
            }

            Sky.DRIZZLE, Sky.RAIN, Sky.SNOW, Sky.THUNDER -> {
                drawPath(cloudPath(s * 0.12f, s * 0.1f, s * 0.76f, s * 0.5f), color, style = stroke)
                precipitation(skyFor(code), s, color, stroke)
            }
        }
    }
}

private fun DrawScope.precipitation(sky: Sky, s: Float, color: Color, stroke: Stroke) {
    val xs = listOf(0.34f, 0.5f, 0.66f)
    when (sky) {
        Sky.RAIN -> xs.forEach { x ->
            drawLine(color, Offset(s * x, s * 0.7f), Offset(s * (x - 0.06f), s * 0.88f), stroke.width, StrokeCap.Round)
        }
        Sky.DRIZZLE -> xs.forEach { x ->
            drawLine(color, Offset(s * x, s * 0.72f), Offset(s * (x - 0.02f), s * 0.78f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(s * (x - 0.04f), s * 0.86f), Offset(s * (x - 0.06f), s * 0.92f), stroke.width, StrokeCap.Round)
        }
        Sky.SNOW -> xs.forEachIndexed { i, x ->
            val y = if (i == 1) 0.86f else 0.76f
            drawCircle(color, radius = s * 0.035f, center = Offset(s * x, s * y))
        }
        Sky.THUNDER -> {
            val bolt = Path().apply {
                moveTo(s * 0.54f, s * 0.62f)
                lineTo(s * 0.44f, s * 0.78f)
                lineTo(s * 0.56f, s * 0.78f)
                lineTo(s * 0.46f, s * 0.95f)
            }
            drawPath(bolt, color, style = stroke)
        }
        else -> Unit
    }
}

private fun DrawScope.sun(center: Offset, r: Float, color: Color, stroke: Stroke) {
    drawCircle(color, radius = r, center = center, style = stroke)
    for (i in 0 until 8) {
        val a = Math.toRadians(i * 45.0)
        val dx = cos(a).toFloat()
        val dy = sin(a).toFloat()
        drawLine(
            color,
            Offset(center.x + dx * r * 1.55f, center.y + dy * r * 1.55f),
            Offset(center.x + dx * r * 2.0f, center.y + dy * r * 2.0f),
            stroke.width,
            StrokeCap.Round,
        )
    }
}

private fun DrawScope.moon(center: Offset, r: Float, color: Color, stroke: Stroke) {
    val disc = Path().apply { addOval(Rect(center, r)) }
    val bite = Path().apply { addOval(Rect(Offset(center.x + r * 0.55f, center.y - r * 0.4f), r * 0.85f)) }
    val crescent = Path().apply { op(disc, bite, PathOperation.Difference) }
    drawPath(crescent, color, style = stroke)
}

/** A cloud outline: a pill-shaped base merged with two round puffs. */
private fun cloudPath(left: Float, top: Float, w: Float, h: Float): Path {
    val base = Path().apply {
        addRoundRect(RoundRect(left, top + h * 0.45f, left + w, top + h, CornerRadius(h * 0.275f)))
    }
    val small = Path().apply { addOval(Rect(Offset(left + w * 0.34f, top + h * 0.52f), h * 0.3f)) }
    val big = Path().apply { addOval(Rect(Offset(left + w * 0.6f, top + h * 0.42f), h * 0.4f)) }
    val merged = Path().apply { op(base, small, PathOperation.Union) }
    return Path().apply { op(merged, big, PathOperation.Union) }
}
