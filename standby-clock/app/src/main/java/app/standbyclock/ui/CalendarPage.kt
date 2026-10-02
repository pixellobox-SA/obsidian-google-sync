package app.standbyclock.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import app.standbyclock.data.Todo
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import java.time.format.TextStyle as DateStyle

/** Page to the left of the main one: this month's calendar and the to-do list. */
@Composable
fun CalendarPage(
    today: LocalDate,
    todos: List<Todo>,
    palette: Palette,
    onToggleTodo: (Int) -> Unit,
) {
    Row(
        Modifier.fillMaxSize().padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        CalendarWidget(today, palette, Modifier.weight(1.1f).fillMaxHeight())
        TodoWidget(todos, palette, onToggleTodo, Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun CalendarWidget(today: LocalDate, palette: Palette, modifier: Modifier) {
    val locale = Locale.getDefault()
    val month = YearMonth.from(today)
    val firstDay = remember(locale) { WeekFields.of(locale).firstDayOfWeek }
    val days = remember(firstDay) { List(7) { firstDay.plus(it.toLong()) } }
    val blanks = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
    val rows = (blanks + month.lengthOfMonth() + 6) / 7

    Widget(palette, modifier, contentPadding = PaddingValues(20.dp)) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val cellW = maxWidth / 7
            val cellH = maxHeight / (rows + 2.2f)
            val font = with(density) { minOf(cellH * 0.48f, cellW * 0.42f).toSp() }

            Column(Modifier.fillMaxSize()) {
                BasicText(
                    text = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)),
                    modifier = Modifier.fillMaxWidth().height(cellH * 1.2f),
                    style = label(font * 1.15f, palette.primary, FontWeight.SemiBold).copy(textAlign = TextAlign.Center),
                )
                Row {
                    days.forEach { d ->
                        Cell(cellW, cellH) {
                            BasicText(
                                d.getDisplayName(DateStyle.SHORT, locale).take(2),
                                style = label(font * 0.95f, if (d == DayOfWeek.SUNDAY) palette.accent else palette.primary, FontWeight.Medium),
                            )
                        }
                    }
                }
                for (r in 0 until rows) {
                    Row {
                        for (c in 0 until 7) {
                            val day = r * 7 + c - blanks + 1
                            Cell(cellW, cellH) {
                                if (day in 1..month.lengthOfMonth()) {
                                    val isToday = day == today.dayOfMonth
                                    val sunday = days[c] == DayOfWeek.SUNDAY
                                    Box(
                                        Modifier
                                            .size(minOf(cellW, cellH) * 0.92f)
                                            .then(if (isToday) Modifier.border(1.5.dp, palette.primary, CircleShape) else Modifier),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            "$day",
                                            style = label(font, if (sunday) palette.accent else palette.primary, FontWeight.Light),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(w: Dp, h: Dp, content: @Composable () -> Unit) {
    Box(Modifier.size(w, h), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun TodoWidget(todos: List<Todo>, palette: Palette, onToggle: (Int) -> Unit, modifier: Modifier) {
    Widget(palette, modifier, contentPadding = PaddingValues(24.dp)) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val unit = maxHeight / 10
            val font = with(density) { (unit * 0.48f).toSp() }

            Column(Modifier.fillMaxSize()) {
                BasicText("To Do:", style = label(font * 1.7f, palette.primary, FontWeight.SemiBold))
                Spacer(Modifier.height(unit * 0.3f))
                if (todos.isEmpty()) {
                    BasicText(
                        "Add items in the Standby Clock app.",
                        style = label(font * 0.8f, palette.secondary, FontWeight.Normal),
                    )
                }
                LazyColumn {
                    itemsIndexed(todos) { i, todo ->
                        TodoRow(todo, unit, font, palette) { onToggle(i) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoRow(
    todo: Todo,
    unit: Dp,
    font: TextUnit,
    palette: Palette,
    onToggle: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(unit * 1.25f)
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tick circle: empty, or filled with a check mark when done.
        Canvas(Modifier.size(unit * 0.8f)) {
            val r = size.minDimension / 2
            if (todo.done) {
                drawCircle(palette.accent, r)
                val s = size.minDimension
                drawLine(palette.widget.copy(alpha = 1f), Offset(s * 0.28f, s * 0.52f), Offset(s * 0.44f, s * 0.68f), s * 0.09f, StrokeCap.Round)
                drawLine(palette.widget.copy(alpha = 1f), Offset(s * 0.44f, s * 0.68f), Offset(s * 0.74f, s * 0.34f), s * 0.09f, StrokeCap.Round)
            } else {
                drawCircle(palette.primary, r - 2.dp.toPx() / 2, style = Stroke(2.dp.toPx()))
            }
        }
        Spacer(Modifier.width(unit * 0.5f))
        Column(Modifier.weight(1f)) {
            BasicText(
                todo.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = label(font, if (todo.done) palette.tertiary else palette.primary, FontWeight.Normal)
                    .copy(textDecoration = if (todo.done) TextDecoration.LineThrough else null),
            )
            // Dashed "writing line" under each item.
            Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                drawLine(
                    palette.tertiary,
                    Offset(0f, size.height / 2),
                    Offset(size.width, size.height / 2),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx())),
                )
            }
        }
    }
}
