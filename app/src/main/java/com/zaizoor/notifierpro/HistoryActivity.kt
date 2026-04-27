package com.zaizoor.notifierpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        
        val events = HistoryManager.getHistory(this)
        rvHistory.adapter = HistoryAdapter(events)
    }

    class HistoryAdapter(private val events: List<DetectionEvent>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(android.R.id.text1)
            val tvBody: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Using a standard android list item layout for monochrome simplicity
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val event = events[position]
            holder.tvTitle.text = "${event.timestamp} • ${event.title}"
            holder.tvBody.text = event.body
            
            // Royal Monochrome Styling
            holder.tvTitle.setTextColor(android.graphics.Color.BLACK)
            holder.tvTitle.textSize = 14f
            holder.tvBody.setTextColor(android.graphics.Color.GRAY)
            holder.tvBody.textSize = 12f
        }

        override fun getItemCount() = events.size
    }
}
