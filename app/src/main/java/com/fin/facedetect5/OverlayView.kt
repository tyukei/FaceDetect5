package com.fin.facedetect5

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val framePaint: Paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private var frameRect: Rect? = null

    fun drawFrameAroundFace(boundingBox: Rect) {
        frameRect = boundingBox
        invalidate()
    }

    fun clearFrame() {
        frameRect = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the frame around the detected face
        frameRect?.let { rect ->
            canvas.drawRect(rect, framePaint)
        }
    }
}
