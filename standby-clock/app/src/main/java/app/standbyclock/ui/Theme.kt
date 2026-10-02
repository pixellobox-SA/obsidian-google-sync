package app.standbyclock.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.standbyclock.R

val Inter = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

/** Whites and greys plus one warm accent; night swaps everything to a soft, light beige. */
data class Palette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val accent: Color,
    /** Fill and hairline border of the widgets. */
    val widget: Color,
    val widgetEdge: Color,
    /** How much the background picture is darkened, so text stays readable. */
    val scrim: Float,
)

private val Day = Palette(
    primary = Color(0xFFF5F5F7),
    secondary = Color(0xFFD0D0D6),
    tertiary = Color(0xFF9A9AA2),
    accent = Color(0xFFE3C48A),
    widget = Color(0x73000000),
    widgetEdge = Color(0x24FFFFFF),
    scrim = 0.25f,
)

private val Night = Palette(
    primary = Color(0xFFEDE3C4),
    secondary = Color(0xFFCFC3A0),
    tertiary = Color(0xFF948A6E),
    accent = Color(0xFFE9D9A6),
    widget = Color(0x99000000),
    widgetEdge = Color(0x2EEDE3C4),
    scrim = 0.6f,
)

private val colorAnim = tween<Color>(durationMillis = 2500)

@Composable
fun animatedPalette(night: Boolean): Palette {
    val target = if (night) Night else Day
    val primary by animateColorAsState(target.primary, colorAnim, label = "primary")
    val secondary by animateColorAsState(target.secondary, colorAnim, label = "secondary")
    val tertiary by animateColorAsState(target.tertiary, colorAnim, label = "tertiary")
    val accent by animateColorAsState(target.accent, colorAnim, label = "accent")
    val widget by animateColorAsState(target.widget, colorAnim, label = "widget")
    val widgetEdge by animateColorAsState(target.widgetEdge, colorAnim, label = "widgetEdge")
    val scrim by animateFloatAsState(target.scrim, tween(2500), label = "scrim")
    return Palette(primary, secondary, tertiary, accent, widget, widgetEdge, scrim)
}
