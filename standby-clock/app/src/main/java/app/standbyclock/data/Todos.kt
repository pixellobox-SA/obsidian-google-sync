package app.standbyclock.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Todo(val text: String, val done: Boolean = false)

/** The to-do list, edited in the app and ticked off on the clock. Stored on the phone. */
object Todos {
    fun load(context: Context): List<Todo> {
        val json = Prefs(context).todosJson ?: return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Todo(o.getString("text"), o.optBoolean("done"))
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, todos: List<Todo>) {
        val arr = JSONArray()
        todos.forEach { arr.put(JSONObject().put("text", it.text).put("done", it.done)) }
        Prefs(context).todosJson = arr.toString()
    }
}
