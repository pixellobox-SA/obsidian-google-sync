package app.standbyclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import app.standbyclock.data.Prefs
import app.standbyclock.ui.StandbyScreen

/**
 * The full-screen clock. Opened automatically by [ChargeWatcherService], or by the
 * Preview button. Tap anywhere to close it.
 */
class ClockActivity : ComponentActivity() {

    /** Closes the clock when the charger is removed or the phone is turned upright. */
    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ClockState.showing = true
        showOverLockScreen()
        window.goImmersive()

        val filter = IntentFilter().apply {
            addAction(ACTION_CLOSE)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        ContextCompat.registerReceiver(this, closeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        val settings = Prefs(this).snapshot()
        setContent {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            // Don't pop straight back up while the phone is still on the charger.
                            ClockState.dismissed = true
                            finish()
                        },
                    ),
            ) {
                StandbyScreen(settings)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(closeReceiver)
        ClockState.showing = false
        super.onDestroy()
    }

    companion object {
        const val ACTION_CLOSE = "app.standbyclock.CLOSE_CLOCK"
    }
}

/** Shared between the watcher service and the clock (same process). */
object ClockState {
    @Volatile
    var showing = false

    /** Set when the user taps the clock away; cleared when the phone leaves the stand. */
    @Volatile
    var dismissed = false
}
