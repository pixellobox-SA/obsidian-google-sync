package app.standbyclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.dreams.DreamService
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.standbyclock.data.Prefs
import app.standbyclock.ui.DisplayMode
import app.standbyclock.ui.StandbyScreen

/**
 * The screen saver ("Daydream"). Android launches it by itself while the phone is
 * charging or docked, so there is no background service watching the charger.
 *
 * DreamService isn't a LifecycleOwner, so we provide a minimal lifecycle for Compose.
 */
class StandbyDreamService : DreamService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    /** Belt and braces: leave as soon as the charger is pulled out. */
    private val unplugReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = finish()
    }
    private var receiverRegistered = false

    override fun onCreate() {
        super.onCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false // any touch wakes the phone and closes the clock
        isFullscreen = true
        isScreenBright = true

        val settings = Prefs(this).snapshot()
        val view = ComposeView(this).apply {
            // Owners must be in place before the view is attached, or Compose can't start.
            setViewTreeLifecycleOwner(this@StandbyDreamService)
            setViewTreeSavedStateRegistryOwner(this@StandbyDreamService)
            setContent {
                StandbyScreen(settings) { mode -> applyMode(mode) }
            }
        }
        window.decorView.setViewTreeLifecycleOwner(this)
        window.decorView.setViewTreeSavedStateRegistryOwner(this)
        setContentView(view)
        window.goImmersive()

        registerReceiver(unplugReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
        receiverRegistered = true
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDreamingStopped() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        if (receiverRegistered) {
            unregisterReceiver(unplugReceiver)
            receiverRegistered = false
        }
        super.onDetachedFromWindow()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    private fun applyMode(mode: DisplayMode) {
        // Let the system dim the screen too when it's night or when we're hiding.
        isScreenBright = mode == DisplayMode.NORMAL
        window?.applyDisplayMode(mode)
    }
}
