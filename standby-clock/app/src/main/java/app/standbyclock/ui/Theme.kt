package app.standbyclock.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.standbyclock.R

val Inter = FontFamily(
    Font(R.font.inter_extralight, FontWeight.ExtraLight),
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
)

/** Muted greys and whites plus one accent; night swaps everything to dim red/amber. */
data class Palette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val accent: Color,
    val glass: Color,
    val glassEdge: Color,
)

private val Day = Palette(
    primary = Color(0xFFF2F2F4),
    secondary = Color(0xFF9C9CA3),
    tertiary = Color(0xFF5E5E66),
    accent = Color(0xFF8FB0FF),
    glass = Color(0xFFFFFFFF),
    glassEdge = Color(0xFFFFFFFF),
)

private val Night = Palette(
    primary = Color(0xFFC8442A),
    secondary = Color(0xFF8C3020),
    tertiary = Color(0xFF5C2016),
    accent = Color(0xFFC9702A),
    glass = Color(0xFFFF4A2A),
    glassEdge = Color(0xFFFF6A3A),
)

private val colorAnim = tween<Color>(durationMillis = 2500)

@Composable
fun animatedPalette(night: Boolean): Palette {
    val target = if (night) Night else Day
    val primary by animateColorAsState(target.primary, colorAnim, label = "primary")
    val secondary by animateColorAsState(target.secondary, colorAnim, label = "secondary")
    val tertiary by animateColorAsState(target.tertiary, colorAnim, label = "tertiary")
    val accent by animateColorAsState(target.accent, colorAnim, label = "accent")
    val glass by animateColorAsState(target.glass, colorAnim, label = "glass")
    val glassEdge by animateColorAsState(target.glassEdge, colorAnim, label = "glassEdge")
    return Palette(primary, secondary, tertiary, accent, glass, glassEdge)
}
