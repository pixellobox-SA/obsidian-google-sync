package app.standbyclock.ui

import android.Manifest
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
fun SettingsScreen(onOpenScreenSaverSettings: () -> Unit, onPreview: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    var use24h by remember { mutableStateOf(prefs.use24h) }
    var fahrenheit by remember { mutableStateOf(prefs.fahrenheit) }
    var night by remember { mutableStateOf(prefs.nightMode) }
    var nightStart by remember { mutableStateOf(prefs.nightStartHour) }
    var nightEnd by remember { mutableStateOf(prefs.nightEndHour) }
    var landscapeOnly by remember { mutableStateOf(prefs.landscapeOnly) }
    var manualPlace by remember { mutableStateOf(prefs.manualPlace) }
    var hasLocation by remember { mutableStateOf(LocationProvider.hasPermission(context)) }
    var weather by remember { mutableStateOf(WeatherRepository.cached(context)) }
    var cityInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    fun refreshWeather(force: Boolean) = scope.launch {
        WeatherRepository.refresh(context, force)?.let { weather = it }
    }

    // Opening the app is a good moment to look up location + weather in the foreground.
    LaunchedEffect(Unit) { refreshWeather(force = false) }

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
                "Time and weather while your phone charges on its side.",
                color = Color(0xFF9C9CA3),
            )

            Section("Turn it on") {
                Text(
                    "1. Select Open screen saver settings.\n" +
                        "2. Pick Standby clock.\n" +
                        "3. Under When to start, choose While charging.\n" +
                        "4. Charge the phone lying on its side.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Button(onClick = onOpenScreenSaverSettings) { Text("Open screen saver settings") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onPreview) { Text("Preview") }
                }
            }

            Section("Clock") {
                SwitchRow("24-hour clock", checked = use24h) { use24h = it; prefs.use24h = it }
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
                SwitchRow("Dim and turn red at night", checked = night) { night = it; prefs.nightMode = it }
                if (night) {
                    HourStepper("Starts at", nightStart) { nightStart = it.mod(24); prefs.nightStartHour = it }
                    HourStepper("Ends at", nightEnd) { nightEnd = it.mod(24); prefs.nightEndHour = it }
                }
            }

            Section("Display") {
                SwitchRow("Only show in landscape", checked = landscapeOnly) {
                    landscapeOnly = it; prefs.landscapeOnly = it
                }
                Text(
                    "When on, the screen stays black while the phone is upright.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9C9CA3),
                )
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

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
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

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun HourStepper(label: String, hour: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        TextButton(onClick = { onChange(hour - 1) }) { Text("−") }
        Text("%02d:00".format(hour))
        TextButton(onClick = { onChange(hour + 1) }) { Text("+") }
    }
}
