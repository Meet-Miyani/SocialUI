package com.example.viewslib.portershape

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.graphics.*
import com.example.viewslib.R
import android.util.AttributeSet
import android.util.Log
import java.lang.Exception

abstract class PorterImageView : AppCompatImageView {
    private var maskCanvas: Canvas? = null
    private var maskBitmap: Bitmap? = null
    private var maskPaint: Paint? = null
    private var drawableCanvas: Canvas? = null
    private var drawableBitmap: Bitmap? = null
    private var drawablePaint: Paint? = null
    private var invalidated = true
    private var square = false

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

    fun setSquare(square: Boolean) {
        this.square = square
    }

    private fun setup(context: Context, attrs: AttributeSet?, defStyle: Int) {
        if (attrs != null) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.ShaderImageView, defStyle, 0)
            square = typedArray.getBoolean(R.styleable.ShaderImageView_siSquare, false)
            typedArray.recycle()
        }
        if (scaleType == ScaleType.FIT_CENTER) {
            scaleType = ScaleType.CENTER_CROP
        }
        maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskPaint!!.color = Color.BLACK
    }

    override fun invalidate() {
        invalidated = true
        super.invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createMaskCanvas(w, h, oldw, oldh)
    }

    private fun createMaskCanvas(width: Int, height: Int, oldw: Int, oldh: Int) {
        val sizeChanged = width != oldw || height != oldh
        val isValid = width > 0 && height > 0
        if (isValid && (maskCanvas == null || sizeChanged)) {
            maskCanvas = Canvas()
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            maskCanvas!!.setBitmap(maskBitmap)
            maskPaint!!.reset()
            paintMaskCanvas(maskCanvas, maskPaint, width, height)
            drawableCanvas = Canvas()
            drawableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            drawableCanvas!!.setBitmap(drawableBitmap)
            drawablePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            invalidated = true
        }
    }

    protected abstract fun paintMaskCanvas(
        maskCanvas: Canvas?,
        maskPaint: Paint?,
        width: Int,
        height: Int
    )

    override fun onDraw(canvas: Canvas) {
        if (!isInEditMode) {
            val saveCount = canvas.saveLayer(
                0.0f,
                0.0f,
                width.toFloat(),
                height.toFloat(),
                null,
                Canvas.ALL_SAVE_FLAG
            )
            try {
                if (invalidated) {
                    val drawable = drawable
                    if (drawable != null) {
                        invalidated = false
                        val imageMatrix = imageMatrix
                        if (imageMatrix == null) { // && mPaddingTop == 0 && mPaddingLeft == 0) {
                            drawable.draw(drawableCanvas!!)
                        } else {
                            val drawableSaveCount = drawableCanvas!!.saveCount
                            drawableCanvas!!.save()
                            drawableCanvas!!.concat(imageMatrix)
                            drawable.draw(drawableCanvas!!)
                            drawableCanvas!!.restoreToCount(drawableSaveCount)
                        }
                        drawablePaint!!.reset()
                        drawablePaint!!.isFilterBitmap = false
                        drawablePaint!!.xfermode = PORTER_DUFF_XFERMODE
                        drawableCanvas!!.drawBitmap(maskBitmap!!, 0.0f, 0.0f, drawablePaint)
                    }
                }
                if (!invalidated) {
                    drawablePaint!!.xfermode = null
                    canvas.drawBitmap(drawableBitmap!!, 0.0f, 0.0f, drawablePaint)
                }
            } catch (e: Exception) {
                val log = "Exception occured while drawing $id"
                Log.e(TAG, log, e)
            } finally {
                canvas.restoreToCount(saveCount)
            }
        } else {
            super.onDraw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (square) {
            val width = measuredWidth
            val height = measuredHeight
            val dimen = Math.min(width, height)
            setMeasuredDimension(dimen, dimen)
        }
    }

    companion object {
        private val TAG = PorterImageView::class.java.simpleName
        private val PORTER_DUFF_XFERMODE = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
}