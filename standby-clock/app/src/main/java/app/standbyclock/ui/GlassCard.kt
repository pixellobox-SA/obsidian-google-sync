package app.standbyclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Frosted-glass card: a faint translucent fill that fades downward, and a hairline
 * border that catches "light" at the top edge. On a black screen this reads as glass
 * without the cost of a real-time blur.
 */
@Composable
fun GlassCard(
    palette: Palette,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(palette.glass.copy(alpha = 0.075f), palette.glass.copy(alpha = 0.025f)),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(palette.glassEdge.copy(alpha = 0.20f), palette.glassEdge.copy(alpha = 0.03f)),
                ),
                shape = shape,
            )
            .padding(28.dp),
        content = content,
    )
}
