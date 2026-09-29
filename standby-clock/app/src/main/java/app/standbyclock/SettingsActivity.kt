package app.standbyclock

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.standbyclock.ui.SettingsScreen

/** The app icon opens this: setup help, a preview button and a few options. */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(
                onOpenScreenSaverSettings = ::openScreenSaverSettings,
                onPreview = { startActivity(Intent(this, PreviewActivity::class.java)) },
            )
        }
    }

    private fun openScreenSaverSettings() {
        val intents = listOf(Settings.ACTION_DREAM_SETTINGS, Settings.ACTION_DISPLAY_SETTINGS)
        for (action in intents) {
            try {
                startActivity(Intent(action))
                return
            } catch (_: ActivityNotFoundException) {
                // Try the next one.
            }
        }
    }
}
