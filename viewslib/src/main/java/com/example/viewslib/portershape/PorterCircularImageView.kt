package com.example.viewslib.portershape

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet

class PorterCircularImageView : PorterImageView {
    private val rect = RectF()

    constructor(context: Context?) : super(context!!) {
        setup()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        setup()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        setup()
    }

    private fun setup() {
        setSquare(true)
    }

    override fun paintMaskCanvas(maskCanvas: Canvas?, maskPaint: Paint?, width: Int, height: Int) {
        val radius = Math.min(width, height) / 2f
        rect[0f, 0f, width.toFloat()] = height.toFloat()
        maskCanvas!!.drawCircle(rect.centerX(), rect.centerY(), radius, maskPaint!!)
    }
}