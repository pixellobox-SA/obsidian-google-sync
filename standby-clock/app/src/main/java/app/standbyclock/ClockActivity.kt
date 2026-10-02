package app.standbyclock

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import app.standbyclock.data.Dnd
import app.standbyclock.data.Prefs
import app.standbyclock.data.Shortcut
import app.standbyclock.data.Shortcuts
import app.standbyclock.ui.StandbyScreen

/**
 * The full-screen clock with its three swipeable pages. Opened automatically by
 * [ChargeWatcherService], or by the Preview button. Closes when the phone leaves the
 * charger or is turned upright; the back gesture closes it too.
 */
class ClockActivity : ComponentActivity() {

    /** True when the system (unplug / turned upright) closed us rather than the user. */
    private var closedBySystem = false

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            closedBySystem = true
            finish()
        }
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

        // Quiet the phone while it sits on the charger (restored when it's lifted off).
        if (intent.getBooleanExtra(EXTRA_AUTO, false)) Dnd.enable(this)

        val settings = Prefs(this).snapshot()
        setContent {
            StandbyScreen(settings, onLaunch = ::launch)
        }
    }

    /** Opens a quick action. If the phone is locked, asks the user to unlock first. */
    private fun launch(shortcut: Shortcut) {
        val target = Shortcuts.intentFor(this, shortcut) ?: return
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (keyguard.isKeyguardLocked) {
            keyguard.requestDismissKeyguard(
                this,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() = start(target)
                },
            )
        } else {
            start(target)
        }
    }

    private fun start(target: Intent) {
        runCatching { startActivity(target) }
    }

    override fun onDestroy() {
        unregisterReceiver(closeReceiver)
        ClockState.showing = false
        // Closed by the user (back gesture): don't pop straight back up while still docked.
        if (!closedBySystem && !isChangingConfigurations) ClockState.dismissed = true
        super.onDestroy()
    }

    companion object {
        const val ACTION_CLOSE = "app.standbyclock.CLOSE_CLOCK"

        /** Set when the watcher opened the clock (as opposed to the Preview button). */
        const val EXTRA_AUTO = "auto"
    }
}

/** Shared between the watcher service and the clock (same process). */
object ClockState {
    @Volatile
    var showing = false

    /** Set when the user closes the clock; cleared when the phone leaves the stand. */
    @Volatile
    var dismissed = false
}
