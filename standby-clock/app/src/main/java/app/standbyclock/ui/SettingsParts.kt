package app.standbyclock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.standbyclock.data.BackgroundMode

/** Small building blocks shared by the settings screens. */

@Composable
internal fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

/** One background option. [onSelect] receives the mode this row stands for. */
@Composable
internal fun RadioRow(
    label: String,
    mode: BackgroundMode,
    current: BackgroundMode,
    onSelect: (BackgroundMode) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable { onSelect(mode) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = mode == current, onClick = { onSelect(mode) })
        Text(label)
    }
}

@Composable
internal fun PermissionRow(
    done: Boolean,
    doneText: String,
    todoText: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (done) "✓ $doneText" else todoText,
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (done) Color(0xFF9C9CA3) else Color.White,
        )
        if (!done) {
            Spacer(Modifier.width(8.dp))
            Button(onClick = onClick) { Text(buttonText) }
        }
    }
}

@Composable
internal fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
internal fun HourStepper(label: String, hour: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        TextButton(onClick = { onChange(hour - 1) }) { Text("−") }
        Text("%02d:00".format(hour))
        TextButton(onClick = { onChange(hour + 1) }) { Text("+") }
    }
}
