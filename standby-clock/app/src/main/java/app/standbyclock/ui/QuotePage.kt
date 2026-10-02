package app.standbyclock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.standbyclock.data.Quote
import app.standbyclock.data.Shortcut
import app.standbyclock.data.Shortcuts
import kotlin.math.sqrt

/** Page to the right of the main one: quote of the week and quick actions. */
@Composable
fun QuotePage(
    quote: Quote,
    shortcuts: List<Shortcut?>,
    palette: Palette,
    onLaunch: (Shortcut) -> Unit,
) {
    Row(
        Modifier.fillMaxSize().padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        QuoteWidget(quote, palette, Modifier.weight(0.85f).fillMaxHeight())
        QuickActions(shortcuts, palette, onLaunch, Modifier.weight(2f).fillMaxHeight())
    }
}

@Composable
private fun QuoteWidget(quote: Quote, palette: Palette, modifier: Modifier) {
    Widget(palette, modifier, contentPadding = PaddingValues(22.dp)) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Longer quotes get smaller type so they always fit.
            val fit = sqrt(70.0 / quote.text.length).toFloat().coerceIn(0.55f, 1.25f)
            val font = with(LocalDensity.current) { (minOf(maxHeight, maxWidth * 1.3f) * 0.1f * fit).toSp() }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BasicText(
                    text = "“${quote.text}”",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = font,
                        lineHeight = font * 1.12f,
                        color = palette.primary,
                        textAlign = TextAlign.Center,
                        shadow = TextShadow,
                    ),
                )
                Spacer(Modifier.height(18.dp))
                BasicText(
                    text = quote.author,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontSize = font * 0.5f,
                        color = palette.secondary,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

@Composable
private fun QuickActions(
    shortcuts: List<Shortcut?>,
    palette: Palette,
    onLaunch: (Shortcut) -> Unit,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val title = with(density) { (maxHeight * 0.075f).toSp() }
        val titleH = maxHeight * 0.14f
        val gap = 16.dp
        val tileH = (maxHeight - titleH - gap) / 2

        Column {
            Box(Modifier.height(titleH), contentAlignment = Alignment.CenterStart) {
                BasicText("Quick Actions:", style = label(title, palette.primary, FontWeight.Medium))
            }
            shortcuts.chunked(3).forEachIndexed { row, items ->
                if (row > 0) Spacer(Modifier.height(gap))
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    items.forEach { s ->
                        ShortcutTile(s, palette, tileH, onLaunch, Modifier.weight(1f).height(tileH))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutTile(
    shortcut: Shortcut?,
    palette: Palette,
    tileH: Dp,
    onLaunch: (Shortcut) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(null, shortcut) {
        value = when (shortcut) {
            is Shortcut.App -> Shortcuts.appIcon(context, shortcut.packageName)
            is Shortcut.Contact -> Shortcuts.contactPhoto(context, shortcut)
            null -> null
        }
    }
    val font = with(LocalDensity.current) { (tileH * 0.11f).toSp() }
    val iconSize = tileH * 0.46f

    Widget(
        palette,
        modifier.then(if (shortcut != null) Modifier.clickable { onLaunch(shortcut) } else Modifier),
        contentPadding = PaddingValues(10.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            when {
                shortcut == null -> BasicText("+", style = label(font * 2f, palette.tertiary, FontWeight.Light))
                image != null -> Image(
                    bitmap = image!!,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(iconSize)
                        .then(if (shortcut is Shortcut.Contact) Modifier.clip(CircleShape) else Modifier),
                )
                else -> Initials(shortcut.label, iconSize, palette)
            }
            if (shortcut != null) {
                BasicText(
                    shortcut.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    style = label(font, palette.primary, FontWeight.Medium).copy(textAlign = TextAlign.Center),
                )
            }
        }
    }
}

/** Round badge with initials, for contacts without a photo. */
@Composable
private fun Initials(name: String, size: Dp, palette: Palette) {
    val letters = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
    val font = with(LocalDensity.current) { (size * 0.38f).toSp() }
    Box(
        Modifier.size(size).clip(CircleShape).background(palette.accent.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(letters, style = label(font, palette.primary, FontWeight.Medium))
    }
}
