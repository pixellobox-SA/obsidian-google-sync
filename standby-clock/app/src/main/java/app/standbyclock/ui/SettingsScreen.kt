package app.standbyclock.ui

import android.Manifest
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import app.standbyclock.data.BackgroundMode
import app.standbyclock.data.BackgroundPhoto
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.standbyclock.data.Dnd
import app.standbyclock.data.LocationProvider
import app.standbyclock.data.Prefs
import app.standbyclock.data.WeatherRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val SettingsColors = darkColorScheme(
    primary = Color(0xFF8FB0FF),
    onPrimary = Color(0xFF0B1530),
    background = Color.Black,
    surface = Color(0xFF111114),
    surfaceVariant = Color(0xFF16161A),
)

@Composable
fun SettingsScreen(
    resumeTick: Int,
    onPreview: () -> Unit,
    onAllowOverlay: () -> Unit,
    onAllowBackground: () -> Unit,
    onAutoStartChanged: () -> Unit,
    onAllowDnd: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    var use24h by remember { mutableStateOf(prefs.use24h) }
    var fahrenheit by remember { mutableStateOf(prefs.fahrenheit) }
    var night by remember { mutableStateOf(prefs.nightMode) }
    var nightStart by remember { mutableStateOf(prefs.nightStartHour) }
    var nightEnd by remember { mutableStateOf(prefs.nightEndHour) }
    var autoStart by remember { mutableStateOf(prefs.autoStart) }
    var background by remember { mutableStateOf(prefs.background) }
    var userName by remember { mutableStateOf(prefs.userName) }
    var greeting by remember { mutableStateOf(prefs.message) }
    var dnd by remember { mutableStateOf(prefs.dnd) }
    val hasDndAccess = remember(resumeTick) { Dnd.hasAccess(context) }
    var manualPlace by remember { mutableStateOf(prefs.manualPlace) }
    var hasLocation by remember { mutableStateOf(LocationProvider.hasPermission(context)) }
    // Re-checked every time the user comes back from the system Settings app.
    val canOverlay = remember(resumeTick) {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Settings.canDrawOverlays(context)
    }
    val canRunInBackground = remember(resumeTick) {
        context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
    }
    var weather by remember { mutableStateOf(WeatherRepository.cached(context)) }
    var cityInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    fun refreshWeather(force: Boolean) = scope.launch {
        WeatherRepository.refresh(context, force)?.let { weather = it }
    }

    // Opening the app is a good moment to look up location + weather in the foreground.
    LaunchedEffect(Unit) { refreshWeather(force = false) }

    // Android's photo picker: no storage permission needed, only the chosen photo is shared.
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                if (BackgroundPhoto.save(context, uri)) {
                    prefs.background = BackgroundMode.PHOTO
                    background = BackgroundMode.PHOTO
                    message = null
                } else {
                    message = "Couldn't use that photo."
                }
            }
        }
    }
    fun pickPhoto() = photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasLocation = granted
        if (granted) refreshWeather(force = true)
    }

    MaterialTheme(colorScheme = SettingsColors) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Standby Clock", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "Time, weather, calendar and more while your phone charges wirelessly on its side.",
                color = Color(0xFF9C9CA3),
            )

            Section("Automatic start") {
                SwitchRow("Open when charging wirelessly on its side", checked = autoStart) {
                    autoStart = it; prefs.autoStart = it; onAutoStartChanged()
                }
                if (autoStart) {
                    Spacer(Modifier.height(8.dp))
                    PermissionRow(
                        done = canOverlay,
                        doneText = "Can open over the lock screen",
                        todoText = "Needed to open by itself: allow Display over other apps.",
                        buttonText = "Allow",
                        onClick = onAllowOverlay,
                    )
                    PermissionRow(
                        done = canRunInBackground,
                        doneText = "Allowed to keep watching in the background",
                        todoText = "Recommended so the phone doesn't stop it: allow background use.",
                        buttonText = "Allow",
                        onClick = onAllowBackground,
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onPreview) { Text("Preview") }
            }

            Section("Greeting") {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it; prefs.userName = it },
                    label = { Text("Your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = greeting,
                    onValueChange = { greeting = it; prefs.message = it },
                    label = { Text("Message above the time (optional)") },
                    placeholder = { Text("Hello ${userName.ifBlank { "there" }}, time for work!") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Leave the message empty for an automatic \"Good morning\" / \"Good evening\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9C9CA3),
                )
            }

            Section("Do Not Disturb") {
                SwitchRow("Turn on while the clock is showing", checked = dnd) { dnd = it; prefs.dnd = it }
                if (dnd) {
                    PermissionRow(
                        done = hasDndAccess,
                        doneText = "Allowed to change Do Not Disturb",
                        todoText = "Needed: allow Do Not Disturb access for Standby Clock.",
                        buttonText = "Allow",
                        onClick = onAllowDnd,
                    )
                    Text(
                        "Uses Priority mode, so alarms still ring. Switched back off when you lift the phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9C9CA3),
                    )
                }
            }

            Section("To-do list") { TodoEditor() }

            Section("Quick actions") { ShortcutsEditor() }

            Section("Clock") {
                SwitchRow("24-hour clock", checked = use24h) { use24h = it; prefs.use24h = it }
            }

            Section("Background") {
                RadioRow("Phone wallpaper", BackgroundMode.WALLPAPER, background) {
                    background = BackgroundMode.WALLPAPER; prefs.background = it
                }
                RadioRow("A photo", BackgroundMode.PHOTO, background) {
                    if (BackgroundPhoto.exists(context)) {
                        background = BackgroundMode.PHOTO; prefs.background = it
                    } else {
                        pickPhoto()
                    }
                }
                if (background == BackgroundMode.PHOTO) {
                    TextButton(onClick = { pickPhoto() }) { Text("Choose another photo") }
                }
                RadioRow("Black", BackgroundMode.BLACK, background) {
                    background = BackgroundMode.BLACK; prefs.background = it
                }
            }

            Section("Weather") {
                Text(
                    weather?.let {
                        val place = it.place.ifBlank { "your area" }
                        "Now ${it.temperature.roundToInt()}° and ${conditionName(it.code).lowercase()} in $place"
                    } ?: "No weather yet",
                    style = MaterialTheme.typography.bodyMedium,
                )
                SwitchRow("Show °F instead of °C", checked = fahrenheit) {
                    fahrenheit = it; prefs.fahrenheit = it; refreshWeather(force = true)
                }

                val place = manualPlace
                when {
                    place != null -> {
                        Text("Location: ${place.name} (set by hand)", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {
                            prefs.manualPlace = null; manualPlace = null; refreshWeather(force = true)
                        }) { Text("Use approximate location instead") }
                    }
                    hasLocation -> Text("Location: approximate (about 1 km)", style = MaterialTheme.typography.bodyMedium)
                    else -> Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }) { Text("Allow approximate location") }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        label = { Text("Or type a city") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(enabled = cityInput.isNotBlank(), onClick = {
                        scope.launch {
                            val found = WeatherRepository.searchCity(cityInput)
                            if (found == null) {
                                message = "Couldn't find that city."
                            } else {
                                prefs.manualPlace = found
                                manualPlace = found
                                cityInput = ""
                                message = null
                                refreshWeather(force = true)
                            }
                        }
                    }) { Text("Set") }
                }
                message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            Section("Night mode") {
                SwitchRow("Red/amber colours at night", checked = night) { night = it; prefs.nightMode = it }
                if (night) {
                    HourStepper("Starts at", nightStart) { nightStart = it.mod(24); prefs.nightStartHour = it }
                    HourStepper("Ends at", nightEnd) { nightEnd = it.mod(24); prefs.nightEndHour = it }
                }
            }

            Text(
                "No ads, no tracking, no account. Weather comes from Open-Meteo (open-meteo.com); " +
                    "only a rounded location is sent to it.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6E6E76),
            )
        }
    }
}
