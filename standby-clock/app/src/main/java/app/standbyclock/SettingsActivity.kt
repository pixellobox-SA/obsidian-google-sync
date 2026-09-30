package app.standbyclock

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableIntStateOf
import app.standbyclock.ui.SettingsScreen

/** The app icon opens this: setup, a preview button and a few options. */
class SettingsActivity : ComponentActivity() {

    /** Bumped on every resume so the screen re-checks permissions granted in Settings. */
    private val resumeTick = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(
                resumeTick = resumeTick.intValue,
                onPreview = { startActivity(Intent(this, ClockActivity::class.java)) },
                onAllowOverlay = {
                    open(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri()))
                },
                onAllowBackground = {
                    open(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri()),
                        fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                    )
                },
                onAutoStartChanged = { ChargeWatcherService.sync(this) },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick.intValue++
        // Make sure the watcher is running (it may have been stopped by the system).
        ChargeWatcherService.sync(this)
    }

    private fun packageUri() = Uri.parse("package:$packageName")

    private fun open(intent: Intent, fallback: Intent? = null) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            fallback?.let { runCatching { startActivity(it) } }
        }
    }
}
