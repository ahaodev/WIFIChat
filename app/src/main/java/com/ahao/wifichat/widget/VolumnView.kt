package com.ahao.wifichat.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.ahao.wifichat.R

/** 圆环形音量/进度指示器 */
class VolumnView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    private val outerWidth = 15f
    private val innerWidth = 30f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ling)
    private val oval by lazy { RectF() }

    /** 进度，0 ~ 100 */
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 100f)
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f
        val r1 = cx - outerWidth / 2
        val r2 = cx - outerWidth / 2 - innerWidth / 2
        val r3 = cx - outerWidth / 2 - innerWidth

        paint.strokeWidth = outerWidth
        paint.color = Color.parseColor("#454547")
        canvas.drawCircle(cx, cy, r1, paint)

        paint.color = Color.parseColor("#747476")
        paint.strokeWidth = innerWidth
        canvas.drawCircle(cx, cy, r2, paint)

        paint.color = Color.parseColor("#464648")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, r3, paint)

        canvas.drawBitmap(bitmap, cx - bitmap.width / 2, cx - bitmap.height / 2, paint)

        paint.color = Color.WHITE
        paint.strokeWidth = 0f
        paint.textSize = 40f
        val text = "铃声"
        val textWidth = paint.measureText(text)
        canvas.drawText(text, cx - textWidth / 2, cx + bitmap.height / 2 + 40, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = innerWidth
        paint.color = Color.WHITE
        oval.set(cx - r2, cy - r2, cx + r2, cy + r2)
        canvas.drawArc(oval, 270f, 360 * progress / 100, false, paint)
        super.onDraw(canvas)
    }
}
