package com.example.daysleft

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Tracker(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var startEpochDay: Long,
    var endEpochDay: Long
)

object Storage {
    private const val PREF = "daysleft_prefs"
    private const val KEY = "trackers"

    fun load(ctx: Context): MutableList<Tracker> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Tracker>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Tracker(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    startEpochDay = o.getLong("start"),
                    endEpochDay = o.getLong("end")
                )
            )
        }
        return list
    }

    fun save(ctx: Context, list: List<Tracker>) {
        val arr = JSONArray()
        for (t in list) {
            arr.put(
                JSONObject()
                    .put("id", t.id)
                    .put("name", t.name)
                    .put("start", t.startEpochDay)
                    .put("end", t.endEpochDay)
            )
        }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }
}