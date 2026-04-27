package com.zaizoor.notifierpro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class DetectionEvent(
    val title: String,
    val body: String,
    val timestamp: String
)

object HistoryManager {
    private const val FILE_NAME = "history_v2.json"

    fun addLog(context: Context, title: String, body: String) {
        try {
            val events = getHistory(context).toMutableList()
            val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
            val event = DetectionEvent(title, body, sdf.format(Date()))
            
            // Add to start of list (newest first)
            events.add(0, event)
            
            // Keep only last 50 events
            val limitedEvents = if (events.size > 50) events.take(50) else events
            
            saveHistory(context, limitedEvents)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getHistory(context: Context): List<DetectionEvent> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        
        return try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<DetectionEvent>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(DetectionEvent(
                    obj.getString("title"),
                    obj.getString("body"),
                    obj.getString("timestamp")
                ))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHistory(context: Context, events: List<DetectionEvent>) {
        val jsonArray = JSONArray()
        events.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("body", it.body)
            obj.put("timestamp", it.timestamp)
            jsonArray.put(obj)
        }
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(jsonArray.toString())
    }
}
