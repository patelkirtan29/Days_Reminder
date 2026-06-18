package com.example.daysleft

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class GridView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    var startEpochDay: Long = LocalDate.now().toEpochDay()
    var endEpochDay: Long = LocalDate.now().toEpochDay()
        set(value) { field = value; invalidate() }

    private val cols = 14
    private val filled = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
    }
    private val empty = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#444444")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val todayP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    override fun onMeasure(wSpec: Int, hSpec: Int) {
        val width = MeasureSpec.getSize(wSpec)
        val total = (endEpochDay - startEpochDay + 1).coerceAtLeast(1).toInt()
        val rows = ceil(total / cols.toDouble()).toInt()
        val cell = width / cols.toFloat()
        val height = (rows * cell).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val total = (endEpochDay - startEpochDay + 1).coerceAtLeast(1).toInt()
        val passed = ChronoUnit.DAYS.between(
            LocalDate.ofEpochDay(startEpochDay), LocalDate.now()
        ).toInt().coerceIn(0, total)

        val cell = width / cols.toFloat()
        val gap = cell * 0.18f
        val box = cell - gap

        for (i in 0 until total) {
            val r = i / cols
            val c = i % cols
            val x = c * cell + gap / 2
            val y = r * cell + gap / 2
            val rect = RectF(x, y, x + box, y + box)
            when {
                i < passed -> canvas.drawRoundRect(rect, 8f, 8f, filled)
                i == passed -> canvas.drawRoundRect(rect, 8f, 8f, todayP)
                else -> canvas.drawRoundRect(rect, 8f, 8f, empty)
            }
        }
    }
}