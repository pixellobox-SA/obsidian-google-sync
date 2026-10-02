package app.standbyclock.data

import android.app.NotificationManager
import android.content.Context

/**
 * Do Not Disturb while the clock is up. Uses Android's "Priority only" mode, so alarms
 * and anything the user has marked as priority still come through. The previous mode is
 * remembered and put back when the phone leaves the charger.
 */
object Dnd {
    private fun nm(context: Context) = context.getSystemService(NotificationManager::class.java)

    /** The user has granted "Do Not Disturb access" to this app in Settings. */
    fun hasAccess(context: Context) = nm(context).isNotificationPolicyAccessGranted

    fun enable(context: Context) {
        val prefs = Prefs(context)
        if (!prefs.dnd || prefs.dndActive || !hasAccess(context)) return
        val nm = nm(context)
        prefs.dndPrevious = nm.currentInterruptionFilter
        runCatching { nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY) }
            .onSuccess { prefs.dndActive = true }
    }

    fun restore(context: Context) {
        val prefs = Prefs(context)
        if (!prefs.dndActive) return
        prefs.dndActive = false
        if (!hasAccess(context)) return
        val previous = prefs.dndPrevious.takeIf { it != NotificationManager.INTERRUPTION_FILTER_UNKNOWN }
            ?: NotificationManager.INTERRUPTION_FILTER_ALL
        runCatching { nm(context).setInterruptionFilter(previous) }
    }
}
