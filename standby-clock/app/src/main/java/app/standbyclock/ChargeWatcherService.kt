package app.standbyclock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.ContextCompat
import app.standbyclock.data.Prefs
import kotlin.math.abs

/**
 * Waits for the phone to be charging **wirelessly** and standing **on its side**,
 * then opens [ClockActivity] on top of the lock screen.
 *
 * - Listens to the battery broadcast to tell wireless charging from cable charging.
 * - Uses the accelerometer (only while charging wirelessly) to tell landscape from
 *   portrait or lying flat, even while the screen is off.
 * - Closes the clock when the phone leaves the charger or is turned upright.
 */
class ChargeWatcherService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var wireless = false
    private var sensing = false

    /** Debounced orientation: only acted on after it has been steady for a moment. */
    private var stableLandscape = false
    private var candidate: Boolean? = null
    private var candidateSince = 0L

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            onWirelessChanged(plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // Battery changes are a protected system broadcast. The sticky value arrives straight away.
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Every startForegroundService() call must be answered with startForeground().
        startInForeground()
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(batteryReceiver)
        stopSensing()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun onWirelessChanged(nowWireless: Boolean) {
        if (nowWireless == wireless) return
        wireless = nowWireless
        ClockState.dismissed = false
        if (wireless) {
            startSensing()
        } else {
            stopSensing()
            closeClock()
        }
    }

    private fun startSensing() {
        val sensor = accelerometer ?: return
        if (sensing) return
        sensing = true
        stableLandscape = false
        candidate = null
        // Keep the CPU awake so orientation updates still arrive with the screen off.
        // Only held while on the wireless charger, so it costs no battery.
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StandbyClock:watch")
            .apply { acquire(12 * 60 * 60 * 1000L) }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopSensing() {
        if (!sensing) return
        sensing = false
        sensorManager.unregisterListener(this)
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = abs(event.values[0]) // gravity along the long side when the phone is on its side
        val y = abs(event.values[1]) // gravity along the long side when upright
        val reading = when {
            x > 6f && x > y + 2f -> true // standing on its side
            y > x || x < 4f -> false // upright, or lying flat
            else -> return // in between: keep the current state
        }

        val now = SystemClock.elapsedRealtime()
        if (reading != candidate) {
            candidate = reading
            candidateSince = now
            return
        }
        if (reading == stableLandscape || now - candidateSince < STEADY_MS) return

        stableLandscape = reading
        if (stableLandscape) {
            openClock()
        } else {
            ClockState.dismissed = false
            closeClock()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun openClock() {
        if (!Prefs(this).autoStart || ClockState.showing || ClockState.dismissed) return
        // Android 10+ only lets background apps open screens with "Display over other apps".
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(this)) return
        startActivity(
            Intent(this, ClockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun closeClock() {
        if (ClockState.showing) {
            sendBroadcast(Intent(ClockActivity.ACTION_CLOSE).setPackage(packageName))
        }
    }

    private fun startInForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Waiting for the charger", NotificationManager.IMPORTANCE_MIN)
                .apply { setShowBadge(false) },
        )
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, SettingsActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Standby clock is ready")
            .setContentText("Opens when the phone charges wirelessly on its side.")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "watcher"
        private const val NOTIFICATION_ID = 1
        private const val STEADY_MS = 1_500L

        /** Starts or stops the watcher to match the "Start automatically" setting. */
        fun sync(context: Context) {
            val intent = Intent(context, ChargeWatcherService::class.java)
            if (Prefs(context).autoStart) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.stopService(intent)
            }
        }
    }
}

/** Starts the watcher again after the phone restarts or the app is updated. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED ->
                // Opening the app also starts it, so a refusal here is not fatal.
                runCatching { ChargeWatcherService.sync(context) }
        }
    }
}
