package app.standbyclock.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.standbyclock.data.AppInfo
import app.standbyclock.data.Shortcut
import app.standbyclock.data.Shortcuts
import app.standbyclock.data.Todo
import app.standbyclock.data.Todos
import kotlinx.coroutines.launch

/** Add, tick and remove to-do items. Ticking also works straight on the clock. */
@Composable
fun TodoEditor() {
    val context = LocalContext.current
    var todos by remember { mutableStateOf(Todos.load(context)) }
    var input by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    fun update(list: List<Todo>) {
        todos = list
        Todos.save(context, list)
    }

    todos.forEachIndexed { i, todo ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = todo.done, onCheckedChange = { done ->
                update(todos.toMutableList().also { it[i] = todo.copy(done = done) })
            })
            Text(
                todo.text,
                Modifier.weight(1f),
                textDecoration = if (todo.done) TextDecoration.LineThrough else null,
            )
            TextButton(onClick = { update(todos.filterIndexed { j, _ -> j != i }) }) { Text("Remove") }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("New item") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(enabled = input.isNotBlank(), onClick = {
            update(todos + Todo(input.trim()))
            input = ""
            focusManager.clearFocus()
        }) { Text("Add") }
    }
    if (todos.any { it.done }) {
        TextButton(onClick = { update(todos.filterNot { it.done }) }) { Text("Clear finished items") }
    }
}

/** Six "Quick actions" tiles, each an app or a contact. */
@Composable
fun ShortcutsEditor() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var slots by remember { mutableStateOf(Shortcuts.load(context)) }
    var pendingSlot by remember { mutableStateOf<Int?>(null) }
    var appPickerFor by remember { mutableStateOf<Int?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    fun set(slot: Int, shortcut: Shortcut?) {
        slots = slots.toMutableList().also { it[slot] = shortcut }
        Shortcuts.save(context, slots)
    }

    // Android's contact picker: shares only the contact the user taps, no contacts permission.
    val contactPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        val slot = pendingSlot ?: return@rememberLauncherForActivityResult
        pendingSlot = null
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val contact = Shortcuts.fromPickedContact(context, uri)
            if (contact != null) set(slot, contact) else message = "Couldn't read that contact."
        }
    }

    Text(
        "Tap a tile on the clock to open the app, or the contact's card to call or message.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF9C9CA3),
    )
    Spacer(Modifier.height(4.dp))
    slots.forEachIndexed { i, s ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${i + 1}. ${s?.label ?: "Empty"}",
                Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (s == null) Color(0xFF6E6E76) else Color.White,
            )
            TextButton(onClick = { appPickerFor = i }) { Text("App") }
            TextButton(onClick = {
                pendingSlot = i
                contactPicker.launch(null)
            }) { Text("Contact") }
            if (s != null) TextButton(onClick = { set(i, null) }) { Text("✕") }
        }
    }
    message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    appPickerFor?.let { slot ->
        AppPickerDialog(
            onPick = { app ->
                set(slot, Shortcut.App(app.packageName, app.label))
                appPickerFor = null
            },
            onDismiss = { appPickerFor = null },
        )
    }
}

@Composable
private fun AppPickerDialog(onPick: (AppInfo) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val apps by produceState<List<AppInfo>?>(null) { value = Shortcuts.launchableApps(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Choose an app") },
        text = {
            val list = apps
            if (list == null) {
                Text("Loading…")
            } else {
                LazyColumn(Modifier.height(420.dp)) {
                    items(list, key = { it.packageName }) { app ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onPick(app) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AppIcon(app.packageName)
                            Spacer(Modifier.width(12.dp))
                            Text(app.label)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun AppIcon(packageName: String) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(null, packageName) { value = Shortcuts.appIcon(context, packageName) }
    Box(Modifier.size(36.dp)) {
        icon?.let { Image(it, contentDescription = null, modifier = Modifier.size(36.dp)) }
    }
}
