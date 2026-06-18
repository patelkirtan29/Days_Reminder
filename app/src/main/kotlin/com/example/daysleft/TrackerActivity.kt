package com.example.daysleft

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class TrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra("id")
        val t = Storage.load(this).find { it.id == id }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(0xFF111111.toInt())
        }

        if (t == null) {
            root.addView(TextView(this).apply {
                text = "Tracker not found"; setTextColor(0xFFFFFFFF.toInt())
            })
            setContentView(root); return
        }

        val total = (t.endEpochDay - t.startEpochDay + 1).coerceAtLeast(1)
        val passed = ChronoUnit.DAYS.between(
            LocalDate.ofEpochDay(t.startEpochDay), LocalDate.now()
        ).coerceIn(0, total)

        root.addView(TextView(this).apply {
            text = t.name; textSize = 26f; setTextColor(0xFFFFFFFF.toInt())
        })
        root.addView(TextView(this).apply {
            text = "$passed of $total days  •  ${total - passed} left"
            textSize = 16f; setTextColor(0xFF4CAF50.toInt())
            setPadding(0, 10, 0, 30)
        })

        val grid = GridView(this).apply {
            startEpochDay = t.startEpochDay
            endEpochDay = t.endEpochDay
        }
        val scroll = ScrollView(this)
        scroll.addView(grid)
        root.addView(scroll)

        setContentView(root)
    }
}