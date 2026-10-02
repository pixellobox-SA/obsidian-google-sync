package app.standbyclock.data

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** One "Quick actions" tile: opens an app, or a contact's card (call, message, WhatsApp…). */
sealed interface Shortcut {
    val label: String

    data class App(val packageName: String, override val label: String) : Shortcut

    /** [uri] is a contact lookup URI; [photo] a picture copied into app storage, if any. */
    data class Contact(val uri: String, override val label: String, val photo: String?) : Shortcut
}

data class AppInfo(val packageName: String, val label: String)

object Shortcuts {
    const val SLOTS = 6

    fun load(context: Context): List<Shortcut?> {
        val json = Prefs(context).shortcutsJson
        val parsed = runCatching {
            val arr = JSONArray(json ?: "[]")
            List(arr.length()) { i -> arr.optJSONObject(i)?.let(::fromJson) }
        }.getOrDefault(emptyList())
        return List(SLOTS) { parsed.getOrNull(it) }
    }

    fun save(context: Context, slots: List<Shortcut?>) {
        val arr = JSONArray()
        slots.forEach { arr.put(it?.let(::toJson) ?: JSONObject.NULL) }
        Prefs(context).shortcutsJson = arr.toString()
        // Tidy up photos of contacts that are no longer on a tile.
        val kept = slots.mapNotNull { (it as? Shortcut.Contact)?.photo }.toSet()
        context.filesDir.listFiles { f -> f.name.startsWith("contact_") && f.name !in kept }
            ?.forEach { it.delete() }
    }

    /** Apps that have a launcher icon, sorted by name. */
    suspend fun launchableApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(main, 0)
            .map { AppInfo(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    suspend fun appIcon(context: Context, packageName: String): ImageBitmap? = withContext(Dispatchers.IO) {
        runCatching {
            context.packageManager.getApplicationIcon(packageName).toBitmap(192, 192).asImageBitmap()
        }.getOrNull()
    }

    suspend fun contactPhoto(context: Context, contact: Shortcut.Contact): ImageBitmap? =
        withContext(Dispatchers.IO) {
            val name = contact.photo ?: return@withContext null
            runCatching { BitmapFactory.decodeFile(File(context.filesDir, name).path)?.asImageBitmap() }.getOrNull()
        }

    /**
     * Reads name and photo from a contact chosen in Android's contact picker. The picker
     * grants access to that one contact only, so no contacts permission is needed.
     */
    suspend fun fromPickedContact(context: Context, picked: Uri): Shortcut.Contact? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val cols = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            )
            resolver.query(picked, cols, null, null, null)?.use { c ->
                if (!c.moveToFirst()) return@use null
                val id = c.getLong(0)
                val lookup = ContactsContract.Contacts.getLookupUri(id, c.getString(1))
                val name = c.getString(2) ?: "Contact"
                // Copy the photo while we still have access to it.
                val photoName = "contact_$id.jpg"
                val copied = runCatching {
                    ContactsContract.Contacts.openContactPhotoInputStream(resolver, picked, true)?.use { input ->
                        File(context.filesDir, photoName).outputStream().use { input.copyTo(it) }
                        true
                    } ?: false
                }.getOrDefault(false)
                Shortcut.Contact(lookup.toString(), name, photoName.takeIf { copied })
            }
        }.getOrNull()
    }

    /** What to open for a tile. */
    fun intentFor(context: Context, shortcut: Shortcut): Intent? = when (shortcut) {
        is Shortcut.App -> context.packageManager.getLaunchIntentForPackage(shortcut.packageName)
        is Shortcut.Contact -> Intent(Intent.ACTION_VIEW, Uri.parse(shortcut.uri))
    }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun toJson(s: Shortcut): JSONObject = when (s) {
        is Shortcut.App -> JSONObject().put("type", "app").put("pkg", s.packageName).put("label", s.label)
        is Shortcut.Contact -> JSONObject().put("type", "contact").put("uri", s.uri).put("label", s.label)
            .put("photo", s.photo ?: JSONObject.NULL)
    }

    private fun fromJson(o: JSONObject): Shortcut? = when (o.optString("type")) {
        "app" -> Shortcut.App(o.getString("pkg"), o.getString("label"))
        "contact" -> Shortcut.Contact(
            o.getString("uri"),
            o.getString("label"),
            o.optString("photo").takeIf { it.isNotEmpty() && it != "null" },
        )
        else -> null
    }
}
