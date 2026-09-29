package app.standbyclock

import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.standbyclock.ui.DisplayMode

/** Edge-to-edge, no status or navigation bars, drawn under the camera cut-out. */
fun Window.goImmersive() {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    WindowCompat.getInsetsController(this, decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        attributes = attributes.apply {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
}

/** Normal = the phone's own brightness; night and hidden = as dim as the panel allows. */
fun Window.applyDisplayMode(mode: DisplayMode) {
    attributes = attributes.apply {
        screenBrightness = when (mode) {
            DisplayMode.NORMAL -> WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            DisplayMode.NIGHT -> 0.02f
            DisplayMode.HIDDEN -> 0.01f
        }
    }
}
