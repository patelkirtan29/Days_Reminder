package com.example.daysleft

import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class DaysWallpaperService : WallpaperService() {

    override fun onCreateEngine() = DaysEngine()

    inner class DaysEngine : WallpaperService.Engine() {
        private val handler = android.os.Handler(android.os.Looper.getMainLooper())
        private val redraw = Runnable { draw() }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) draw() else handler.removeCallbacks(redraw)
        }

        override fun onSurfaceChanged(h: SurfaceHolder?, f: Int, w: Int, ht: Int) {
            draw()
        }

        private fun draw() {
            val holder = surfaceHolder
            val canvas = holder.lockCanvas() ?: return
            try {
                canvas.drawColor(Color.parseColor("#111111"))

                // read the SAME data the app saved
                val trackers = Storage.load(applicationContext)
                val t = trackers.firstOrNull()

                val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#EEEEEE")
                    textSize = canvas.width * 0.06f
                }
                val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4CAF50")
                    textSize = canvas.width * 0.045f
                }

                if (t == null) {
                    canvas.drawText("Open DaysLeft to add a goal",
                        60f, canvas.height * 0.3f, titlePaint)
                    return
                }

                val total = (t.endEpochDay - t.startEpochDay + 1)
                    .coerceAtLeast(1).toInt()
                val passed = ChronoUnit.DAYS.between(
                    LocalDate.ofEpochDay(t.startEpochDay), LocalDate.now()
                ).toInt().coerceIn(0, total)
                val left = total - passed

                val top = canvas.height * 0.18f
                canvas.drawText(t.name, 60f, top, titlePaint)
                canvas.drawText("$left days left  •  $passed of $total",
                    60f, top + titlePaint.textSize * 1.3f, subPaint)

                val cols = 14
                val margin = 60f
                val w = canvas.width - margin * 2
                val cell = w / cols
                val gap = cell * 0.18f
                val box = cell - gap
                val gridTop = top + titlePaint.textSize * 2.4f

                val filled = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4CAF50")
                }
                val empty = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#444444")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                val todayP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FFFFFF")
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                }

                // honor manual overrides saved in the app too
                fun isFilled(i: Int): Boolean {
                    t.overrides[i]?.let { return it }
                    return i < passed
                }

                for (i in 0 until total) {
                    val r = i / cols
                    val c = i % cols
                    val x = margin + c * cell + gap / 2
                    val y = gridTop + r * cell + gap / 2
                    val rect = RectF(x, y, x + box, y + box)
                    when {
                        isFilled(i) -> canvas.drawRoundRect(rect, 8f, 8f, filled)
                        i == passed -> canvas.drawRoundRect(rect, 8f, 8f, todayP)
                        else -> canvas.drawRoundRect(rect, 8f, 8f, empty)
                    }
                }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
            scheduleNextMidnight()
        }

        private fun scheduleNextMidnight() {
            handler.removeCallbacks(redraw)
            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 2)
            }
            handler.postDelayed(redraw, cal.timeInMillis - now)
        }

        override fun onDestroy() {
            handler.removeCallbacks(redraw)
            super.onDestroy()
        }
    }
}