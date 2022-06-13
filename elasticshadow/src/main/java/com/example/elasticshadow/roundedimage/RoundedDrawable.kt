/*
* Copyright (C) 2017 Vincent Mi
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.elasticshadow.roundedimage

import android.graphics.drawable.Drawable
import android.graphics.Shader.TileMode
import android.content.res.ColorStateList
import android.graphics.*
import android.widget.ImageView.ScaleType
import androidx.annotation.ColorInt
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import java.lang.Float
import java.util.HashSet

class RoundedDrawable(val sourceBitmap: Bitmap) : Drawable() {
    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int
    private val mBitmapHeight: Int
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()
    private val mSquareCornersRect = RectF()
    var tileModeX = TileMode.CLAMP
        private set
    var tileModeY = TileMode.CLAMP
        private set
    private var mRebuildShader = true

    /**
     * @return the corner radius.
     */
    var cornerRadius = 0f
        private set

    // [ topLeft, topRight, bottomLeft, bottomRight ]
    private val mCornersRounded = booleanArrayOf(true, true, true, true)
    var isOval = false
        private set
    var borderWidth = 0f
        private set
    var borderColors = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
        private set
    var scaleType = ScaleType.FIT_CENTER
        private set

    override fun isStateful(): Boolean {
        return borderColors.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = borderColors.getColorForState(state, 0)
        return if (mBorderPaint.color != newColor) {
            mBorderPaint.color = newColor
            true
        } else {
            super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: kotlin.Float
        var dx: kotlin.Float
        var dy: kotlin.Float
        when (scaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setTranslate(
                    ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f),
                    ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f)
                )
            }
            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.reset()
                dx = 0f
                dy = 0f
                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(
                    (dx + 0.5f).toInt() + borderWidth / 2,
                    (dy + 0.5f).toInt() + borderWidth / 2
                )
            }
            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()
                scale = if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    1.0f
                } else {
                    Math.min(
                        mBounds.width() / mBitmapWidth.toFloat(),
                        mBounds.height() / mBitmapHeight.toFloat()
                    )
                }
                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f)
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f)
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }
        mDrawableRect.set(mBorderRect)
        mRebuildShader = true
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mBounds.set(bounds)
        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (mRebuildShader) {
            val bitmapShader = BitmapShader(sourceBitmap, tileModeX, tileModeY)
            if (tileModeX == TileMode.CLAMP && tileModeY == TileMode.CLAMP) {
                bitmapShader.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint.shader = bitmapShader
            mRebuildShader = false
        }
        if (isOval) {
            if (borderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (any(mCornersRounded)) {
                val radius = cornerRadius
                if (borderWidth > 0) {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    canvas.drawRoundRect(mBorderRect, radius, radius, mBorderPaint)
                    redrawBitmapForSquareCorners(canvas)
                    redrawBorderForSquareCorners(canvas)
                } else {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    redrawBitmapForSquareCorners(canvas)
                }
            } else {
                canvas.drawRect(mDrawableRect, mBitmapPaint)
                if (borderWidth > 0) {
                    canvas.drawRect(mBorderRect, mBorderPaint)
                }
            }
        }
    }

    private fun redrawBitmapForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }
        if (cornerRadius == 0f) {
            return  // no round corners
        }
        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = cornerRadius
        if (!mCornersRounded[Corner.TOP_LEFT]) {
            mSquareCornersRect[left, top, left + radius] = top + radius
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            mSquareCornersRect[right - radius, top, right] = radius
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            mSquareCornersRect[right - radius, bottom - radius, right] = bottom
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            mSquareCornersRect[left, bottom - radius, left + radius] = bottom
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
    }

    private fun redrawBorderForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }
        if (cornerRadius == 0f) {
            return  // no round corners
        }
        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = cornerRadius
        val offset = borderWidth / 2
        if (!mCornersRounded[Corner.TOP_LEFT]) {
            canvas.drawLine(left - offset, top, left + radius, top, mBorderPaint)
            canvas.drawLine(left, top - offset, left, top + radius, mBorderPaint)
        }
        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            canvas.drawLine(right - radius - offset, top, right, top, mBorderPaint)
            canvas.drawLine(right, top - offset, right, top + radius, mBorderPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            canvas.drawLine(right - radius - offset, bottom, right + offset, bottom, mBorderPaint)
            canvas.drawLine(right, bottom - radius, right, bottom, mBorderPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            canvas.drawLine(left - offset, bottom, left + radius, bottom, mBorderPaint)
            canvas.drawLine(left, bottom - radius, left, bottom, mBorderPaint)
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getAlpha(): Int {
        return mBitmapPaint.alpha
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return mBitmapPaint.colorFilter
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    /**
     * @param corner the specific corner to get radius of.
     * @return the corner radius of the specified corner.
     */
    fun getCornerRadius(@Corner corner: Int): kotlin.Float {
        return if (mCornersRounded[corner]) cornerRadius else 0f
    }

    /**
     * Sets all corners to the specified radius.
     *
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(radius: kotlin.Float): RoundedDrawable {
        setCornerRadius(radius, radius, radius, radius)
        return this
    }

    /**
     * Sets the corner radius of one specific corner.
     *
     * @param corner the corner.
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(@Corner corner: Int, radius: kotlin.Float): RoundedDrawable {
        require(!(radius != 0f && cornerRadius != 0f && cornerRadius != radius)) { "Multiple nonzero corner radii not yet supported." }
        if (radius == 0f) {
            if (only(corner, mCornersRounded)) {
                cornerRadius = 0f
            }
            mCornersRounded[corner] = false
        } else {
            if (cornerRadius == 0f) {
                cornerRadius = radius
            }
            mCornersRounded[corner] = true
        }
        return this
    }

    /**
     * Sets the corner radii of all the corners.
     *
     * @param topLeft top left corner radius.
     * @param topRight top right corner radius
     * @param bottomRight bototm right corner radius.
     * @param bottomLeft bottom left corner radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(
        topLeft: kotlin.Float, topRight: kotlin.Float, bottomRight: kotlin.Float,
        bottomLeft: kotlin.Float
    ): RoundedDrawable {
        val radiusSet: MutableSet<kotlin.Float> = HashSet(4)
        radiusSet.add(topLeft)
        radiusSet.add(topRight)
        radiusSet.add(bottomRight)
        radiusSet.add(bottomLeft)
        radiusSet.remove(0f)
        require(radiusSet.size <= 1) { "Multiple nonzero corner radii not yet supported." }
        if (!radiusSet.isEmpty()) {
            val radius = radiusSet.iterator().next()
            require(!(Float.isInfinite(radius) || Float.isNaN(radius) || radius < 0)) { "Invalid radius value: $radius" }
            cornerRadius = radius
        } else {
            cornerRadius = 0f
        }
        mCornersRounded[Corner.TOP_LEFT] = topLeft > 0
        mCornersRounded[Corner.TOP_RIGHT] = topRight > 0
        mCornersRounded[Corner.BOTTOM_RIGHT] = bottomRight > 0
        mCornersRounded[Corner.BOTTOM_LEFT] = bottomLeft > 0
        return this
    }

    fun setBorderWidth(width: kotlin.Float): RoundedDrawable {
        borderWidth = width
        mBorderPaint.strokeWidth = borderWidth
        return this
    }

    val borderColor: Int
        get() = borderColors.defaultColor

    fun setBorderColor(@ColorInt color: Int): RoundedDrawable {
        return setBorderColor(ColorStateList.valueOf(color))
    }

    fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
        borderColors = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        return this
    }

    fun setOval(oval: Boolean): RoundedDrawable {
        isOval = oval
        return this
    }

    fun setScaleType(scaleType: ScaleType?): RoundedDrawable {
        var scaleType = scaleType
        if (scaleType == null) {
            scaleType = ScaleType.FIT_CENTER
        }
        if (this.scaleType != scaleType) {
            this.scaleType = scaleType
            updateShaderMatrix()
        }
        return this
    }

    fun setTileModeX(tileModeX: TileMode): RoundedDrawable {
        if (this.tileModeX != tileModeX) {
            this.tileModeX = tileModeX
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun setTileModeY(tileModeY: TileMode): RoundedDrawable {
        if (this.tileModeY != tileModeY) {
            this.tileModeY = tileModeY
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    fun toBitmap(): Bitmap? {
        return drawableToBitmap(this)
    }

    companion object {
        const val TAG = "RoundedDrawable"
        const val DEFAULT_BORDER_COLOR = Color.BLACK
        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
            return bitmap?.let { RoundedDrawable(it) }
        }

        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is RoundedDrawable) {
                    // just return if it's already a RoundedDrawable
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val cs = drawable.mutate().constantState
                    val ld = (cs?.newDrawable() ?: drawable) as LayerDrawable
                    val num = ld.numberOfLayers

                    // loop through layers to and change to RoundedDrawables if possible
                    for (i in 0 until num) {
                        val d = ld.getDrawable(i)
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d))
                    }
                    return ld
                }

                // try to get a bitmap from the drawable and
                val bm = drawableToBitmap(drawable)
                if (bm != null) {
                    return RoundedDrawable(bm)
                }
            }
            return drawable
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            var bitmap: Bitmap?
            val width = Math.max(drawable.intrinsicWidth, 2)
            val height = Math.max(drawable.intrinsicHeight, 2)
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.w(TAG, "Failed to create bitmap from drawable!")
                bitmap = null
            }
            return bitmap
        }

        private fun only(index: Int, booleans: BooleanArray): Boolean {
            var i = 0
            val len = booleans.size
            while (i < len) {
                if (booleans[i] != (i == index)) {
                    return false
                }
                i++
            }
            return true
        }

        private fun any(booleans: BooleanArray): Boolean {
            for (b in booleans) {
                if (b) {
                    return true
                }
            }
            return false
        }

        private fun all(booleans: BooleanArray): Boolean {
            for (b in booleans) {
                if (b) {
                    return false
                }
            }
            return true
        }
    }

    init {
        mBitmapWidth = sourceBitmap.width
        mBitmapHeight = sourceBitmap.height
        mBitmapRect[0f, 0f, mBitmapWidth.toFloat()] = mBitmapHeight.toFloat()
        mBitmapPaint = Paint()
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true
        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        mBorderPaint.strokeWidth = borderWidth
    }
}