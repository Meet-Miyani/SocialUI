package com.example.viewslib.portershape

import android.content.Context
import com.example.viewslib.portershape.PorterImageView
import android.graphics.drawable.Drawable
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.example.viewslib.R
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet

class PorterShapeImageView : PorterImageView {
    var shape: Drawable? = null
    var thisMatrix: Matrix? = null
    private var drawMatrix: Matrix? = null

    constructor(context: Context) : super(context) {
        setup(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setup(context, attrs, defStyle)
    }

    private fun setup(context: Context, attrs: AttributeSet?, defStyle: Int) {
        if (attrs != null) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.ShaderImageView, defStyle, 0)
            shape = typedArray.getDrawable(R.styleable.ShaderImageView_siShape)
            typedArray.recycle()
        }
        thisMatrix = Matrix()
    }

    override fun paintMaskCanvas(maskCanvas: Canvas?, maskPaint: Paint?, width: Int, height: Int) {
        if (shape != null) {
            if (shape is BitmapDrawable) {
                configureBitmapBounds(width, height)
                if (drawMatrix != null) {
                    val drawableSaveCount = maskCanvas!!.saveCount
                    maskCanvas.save()
                    maskCanvas.concat(thisMatrix)
                    shape!!.draw(maskCanvas)
                    maskCanvas.restoreToCount(drawableSaveCount)
                    return
                }
            }
            shape!!.setBounds(0, 0, width, height)
            shape!!.draw(maskCanvas!!)
        }
    }

    private fun configureBitmapBounds(viewWidth: Int, viewHeight: Int) {
        drawMatrix = null
        val drawableWidth = shape!!.intrinsicWidth
        val drawableHeight = shape!!.intrinsicHeight
        val fits = viewWidth == drawableWidth && viewHeight == drawableHeight
        if (drawableWidth > 0 && drawableHeight > 0 && !fits) {
            shape!!.setBounds(0, 0, drawableWidth, drawableHeight)
            val widthRatio = viewWidth.toFloat() / drawableWidth.toFloat()
            val heightRatio = viewHeight.toFloat() / drawableHeight.toFloat()
            val scale = Math.min(widthRatio, heightRatio)
            val dx: Float = ((viewWidth - drawableWidth * scale) * 0.5f + 0.5f)
            val dy: Float = ((viewHeight - drawableHeight * scale) * 0.5f + 0.5f)
            thisMatrix!!.setScale(scale, scale)
            thisMatrix!!.postTranslate(dx, dy)
        }
    }
}