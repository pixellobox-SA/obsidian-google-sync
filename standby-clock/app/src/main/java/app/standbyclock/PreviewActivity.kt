package app.standbyclock

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.standbyclock.data.Prefs
import app.standbyclock.ui.StandbyScreen

/** Shows the clock full-screen on demand. Tap anywhere to close. */
class PreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.goImmersive()

        val settings = Prefs(this).snapshot()
        setContent {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { finish() },
                    ),
            ) {
                StandbyScreen(settings) { mode -> window.applyDisplayMode(mode) }
            }
        }
    }
}
