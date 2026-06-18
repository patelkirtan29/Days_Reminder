package com.example.daysleft

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var trackers: MutableList<Tracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
            setBackgroundColor(0xFF111111.toInt())
        }

        val title = TextView(this).apply {
            text = "My Trackers"
            textSize = 24f
            setTextColor(0xFFEEEEEE.toInt())
        }
        root.addView(title)

        val addBtn = Button(this).apply {
            text = "+ New Tracker"
            setOnClickListener { showEditDialog(null) }
        }
        root.addView(addBtn)

        val scroll = ScrollView(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(container)
        root.addView(scroll)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        trackers = Storage.load(this)
        renderList()
    }

    private fun renderList() {
        container.removeAllViews()
        if (trackers.isEmpty()) {
            val empty = TextView(this).apply {
                text = "\nNo trackers yet. Tap “+ New Tracker”."
                setTextColor(0xFF888888.toInt())
            }
            container.addView(empty)
            return
        }
        for (t in trackers) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(30, 30, 30, 30)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.topMargin = 24
                layoutParams = lp
                setBackgroundColor(0xFF1C1C1C.toInt())
            }
            val total = (t.endEpochDay - t.startEpochDay + 1).coerceAtLeast(1)
            val passed = ChronoUnit.DAYS.between(
                LocalDate.ofEpochDay(t.startEpochDay), LocalDate.now()
            ).coerceIn(0, total)
            val name = TextView(this).apply {
                text = t.name
                textSize = 18f
                setTextColor(0xFFFFFFFF.toInt())
            }
            val sub = TextView(this).apply {
                text = "$passed / $total days"
                setTextColor(0xFF4CAF50.toInt())
            }
            card.addView(name)
            card.addView(sub)
            card.setOnClickListener {
                startActivity(Intent(this, TrackerActivity::class.java)
                    .putExtra("id", t.id))
            }
            card.setOnLongClickListener {
                AlertDialog.Builder(this)
                    .setTitle(t.name)
                    .setItems(arrayOf("Edit", "Delete")) { _, which ->
                        if (which == 0) showEditDialog(t)
                        else {
                            trackers.remove(t)
                            Storage.save(this, trackers)
                            renderList()
                        }
                    }.show()
                true
            }
            container.addView(card)
        }
    }

    private fun showEditDialog(existing: Tracker?) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 0)
        }
        val nameIn = EditText(this).apply {
            hint = "Name (e.g. 2026)"
            setText(existing?.name ?: "")
        }
        val today = LocalDate.now()
        val start = existing?.let { LocalDate.ofEpochDay(it.startEpochDay) } ?: today
        val end = existing?.let { LocalDate.ofEpochDay(it.endEpochDay) }
            ?: today.plusDays(100)
        val startIn = EditText(this).apply {
            hint = "Start (YYYY-MM-DD)"
            setText(start.toString())
        }
        val endIn = EditText(this).apply {
            hint = "End (YYYY-MM-DD)"
            setText(end.toString())
        }
        box.addView(nameIn); box.addView(startIn); box.addView(endIn)

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "New Tracker" else "Edit Tracker")
            .setView(box)
            .setPositiveButton("Save") { _, _ ->
                try {
                    val s = LocalDate.parse(startIn.text.toString().trim())
                    val e = LocalDate.parse(endIn.text.toString().trim())
                    val nm = nameIn.text.toString().trim().ifEmpty { "Tracker" }
                    if (existing == null) {
                        trackers.add(Tracker(name = nm,
                            startEpochDay = s.toEpochDay(),
                            endEpochDay = e.toEpochDay()))
                    } else {
                        existing.name = nm
                        existing.startEpochDay = s.toEpochDay()
                        existing.endEpochDay = e.toEpochDay()
                    }
                    Storage.save(this, trackers)
                    renderList()
                } catch (ex: Exception) {
                    Toast.makeText(this, "Use date format YYYY-MM-DD",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}