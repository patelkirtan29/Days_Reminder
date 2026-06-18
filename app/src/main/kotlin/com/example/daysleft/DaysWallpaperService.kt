package com.example.daysleft

import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DaysWallpaperService : WallpaperService() {

    // CONFIGURE THESE:
    private val startDate = LocalDate.of(2026, 1, 1)
    private val endDate   = LocalDate.of(2026, 12, 31)

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

                val total = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
                val passed = ChronoUnit.DAYS.between(startDate, LocalDate.now())
                    .toInt().coerceIn(0, total)

                val cols = 14
                val rows = (total + cols - 1) / cols
                val margin = 60f
                val w = canvas.width - margin * 2
                val cell = w / cols
                val gap = cell * 0.18f
                val box = cell - gap
                val top = canvas.height * 0.25f

                val filled = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4CAF50")
                }
                val empty = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#333333")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }

                for (i in 0 until total) {
                    val r = i / cols
                    val c = i % cols
                    val x = margin + c * cell
                    val y = top + r * cell
                    val rect = RectF(x, y, x + box, y + box)
                    canvas.drawRoundRect(rect, 8f, 8f,
                        if (i < passed) filled else empty)
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