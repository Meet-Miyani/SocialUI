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

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import android.graphics.drawable.Drawable
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.graphics.Shader.TileMode
import kotlin.jvm.JvmOverloads
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import com.example.elasticshadow.R
import java.lang.Exception

open class RoundedImageView : AppCompatImageView {
    private val mCornerRadii: FloatArray =
        floatArrayOf(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)
    private var mBackgroundDrawable: Drawable? = null
    var borderColors: ColorStateList? = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        private set
    var borderWidth = DEFAULT_BORDER_WIDTH
        private set
    private var mColorFilter: ColorFilter? = null
    private var mColorMod = false
    private var mDrawable: Drawable? = null
    private var mHasColorFilter = false
    private var mIsOval = false
    private var mMutateBackground = false
    private var mResource = 0
    private var mBackgroundResource = 0
    private var mScaleType: ScaleType? = null
    private var mTileModeX: TileMode? = DEFAULT_TILE_MODE
    private var mTileModeY: TileMode? = DEFAULT_TILE_MODE

    constructor(context: Context?) : super(context!!) {}

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int = 0) : super(
        context!!,
        attrs,
        defStyle
    ) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ElasticShadowView, defStyle, 0)
        val index = a.getInt(R.styleable.ElasticShadowView_android_scaleType, -1)
        scaleType = if (index >= 0) {
            SCALE_TYPES[index]
        } else {
            // default scaletype to FIT_CENTER
            ScaleType.FIT_CENTER
        }
        var cornerRadiusOverride =
            a.getDimensionPixelSize(R.styleable.ElasticShadowView_corner_radius, -1).toFloat()
        mCornerRadii[Corner.TOP_LEFT] =
            a.getDimensionPixelSize(R.styleable.ElasticShadowView_corner_radius_top_left, -1)
                .toFloat()
        mCornerRadii[Corner.TOP_RIGHT] =
            a.getDimensionPixelSize(R.styleable.ElasticShadowView_corner_radius_top_right, -1)
                .toFloat()
        mCornerRadii[Corner.BOTTOM_RIGHT] =
            a.getDimensionPixelSize(R.styleable.ElasticShadowView_corner_radius_bottom_right, -1)
                .toFloat()
        mCornerRadii[Corner.BOTTOM_LEFT] =
            a.getDimensionPixelSize(R.styleable.ElasticShadowView_corner_radius_bottom_left, -1)
                .toFloat()
        var any = false
        var i = 0
        val len = mCornerRadii.size
        while (i < len) {
            if (mCornerRadii[i] < 0) {
                mCornerRadii[i] = 0f
            } else {
                any = true
            }
            i++
        }
        if (!any) {
            if (cornerRadiusOverride < 0) {
                cornerRadiusOverride = DEFAULT_RADIUS
            }
            var i = 0
            val len = mCornerRadii.size
            while (i < len) {
                mCornerRadii[i] = cornerRadiusOverride
                i++
            }
        }
        borderWidth =
            a.getDimensionPixelSize(R.styleable.ElasticShadowView_border_width, -1).toFloat()
        if (borderWidth < 0) {
            borderWidth = DEFAULT_BORDER_WIDTH
        }
        borderColors = a.getColorStateList(R.styleable.ElasticShadowView_border_color)
        if (borderColors == null) {
            borderColors = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        }
        mMutateBackground = a.getBoolean(R.styleable.ElasticShadowView_mutate_background, false)
        mIsOval = a.getBoolean(R.styleable.ElasticShadowView_oval, false)
        val tileMode = a.getInt(R.styleable.ElasticShadowView_oval, TILE_MODE_UNDEFINED)
        if (tileMode != TILE_MODE_UNDEFINED) {
            tileModeX = parseTileMode(tileMode)
            tileModeY = parseTileMode(tileMode)
        }
        var tileModeX = a.getInt(R.styleable.ElasticShadowView_tile_mode_x, TILE_MODE_UNDEFINED)
        if (tileModeX != TILE_MODE_UNDEFINED) {
            tileModeX = parseTileMode(tileModeX)!!.ordinal
        }
        var tileModeY = a.getInt(R.styleable.ElasticShadowView_tile_mode_y, TILE_MODE_UNDEFINED)
        if (tileModeY != TILE_MODE_UNDEFINED) {
            tileModeY = parseTileMode(tileModeY)!!.ordinal
        }
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(true)
        if (mMutateBackground) {
            super.setBackgroundDrawable(mBackgroundDrawable)
        }
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun getScaleType(): ScaleType {
        return mScaleType!!
    }

    override fun setScaleType(scaleType: ScaleType) {
        assert(scaleType != null)
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END, ScaleType.FIT_XY -> super.setScaleType(
                    ScaleType.FIT_XY
                )
                else -> super.setScaleType(scaleType)
            }
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromDrawable(drawable)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromBitmap(bm)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        if (mResource != resId) {
            mResource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()
            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(drawable)
    }

    private fun resolveResource(): Drawable? {
        val rsrc = resources ?: return null
        var d: Drawable? = null
        if (mResource != 0) {
            try {
                d = rsrc.getDrawable(mResource,resources.newTheme())
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: $mResource", e)
                // Don't try again.
                mResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    override fun setBackground(background: Drawable?) {
        setBackgroundDrawable(background)
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        if (mBackgroundResource != resId) {
            mBackgroundResource = resId
            mBackgroundDrawable = resolveBackgroundResource()
            setBackgroundDrawable(mBackgroundDrawable)
        }
    }

    override fun setBackgroundColor(color: Int) {
        mBackgroundDrawable = ColorDrawable(color)
        setBackgroundDrawable(mBackgroundDrawable)
    }

    private fun resolveBackgroundResource(): Drawable? {
        val rsrc = resources ?: return null
        var d: Drawable? = null
        if (mBackgroundResource != 0) {
            try {
                d = rsrc.getDrawable(mBackgroundResource, resources.newTheme())
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: $mBackgroundResource", e)
                // Don't try again.
                mBackgroundResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable, mScaleType)
    }

    private fun updateBackgroundDrawableAttrs(convert: Boolean) {
        if (mMutateBackground) {
            if (convert) {
                mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable)
            }
            updateAttrs(mBackgroundDrawable, ScaleType.FIT_XY)
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        if (mColorFilter !== cf) {
            mColorFilter = cf
            mHasColorFilter = true
            mColorMod = true
            applyColorMod()
            invalidate()
        }
    }

    private fun applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable!!.mutate()
            if (mHasColorFilter) {
                mDrawable!!.colorFilter = mColorFilter
            }
            // TODO: support, eventually...
            //mDrawable.setXfermode(mXfermode);
            //mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }

    private fun updateAttrs(drawable: Drawable?, scaleType: ScaleType?) {
        if (drawable == null) {
            return
        }
        if (drawable is RoundedDrawable) {
            drawable
                .setScaleType(scaleType)
                .setBorderWidth(borderWidth)
                .setBorderColor(borderColors)
                .setOval(mIsOval)
                .setTileModeX(mTileModeX!!)
                .setTileModeY(mTileModeY!!)
            if (mCornerRadii != null) {
                drawable.setCornerRadius(
                    mCornerRadii[Corner.TOP_LEFT],
                    mCornerRadii[Corner.TOP_RIGHT],
                    mCornerRadii[Corner.BOTTOM_RIGHT],
                    mCornerRadii[Corner.BOTTOM_LEFT]
                )
            }
            applyColorMod()
        } else if (drawable is LayerDrawable) {
            // loop through layers to and set drawable attrs
            val ld = drawable
            var i = 0
            val layers = ld.numberOfLayers
            while (i < layers) {
                updateAttrs(ld.getDrawable(i), scaleType)
                i++
            }
        }
    }

    @Deprecated("")
    override fun setBackgroundDrawable(background: Drawable?) {
        mBackgroundDrawable = background
        updateBackgroundDrawableAttrs(true)
        super.setBackgroundDrawable(mBackgroundDrawable)
    }
    /**
     * @return the largest corner radius.
     */
    /**
     * Set the corner radii of all corners in px.
     *
     * @param radius the radius to set.
     */
    var cornerRadius: Float
        get() = maxCornerRadius
        set(radius) {
            setCornerRadius(radius, radius, radius, radius)
        }

    /**
     * @return the largest corner radius.
     */
    val maxCornerRadius: Float
        get() {
            var maxRadius = 0f
            for (r in mCornerRadii!!) {
                maxRadius = Math.max(r, maxRadius)
            }
            return maxRadius
        }

    /**
     * Get the corner radius of a specified corner.
     *
     * @param corner the corner.
     * @return the radius.
     */
    fun getCornerRadius(@Corner corner: Int): Float {
        return mCornerRadii!![corner]
    }

    /**
     * Set all the corner radii from a dimension resource id.
     *
     * @param resId dimension resource id of radii.
     */
    fun setCornerRadiusDimen(@DimenRes resId: Int) {
        val radius = resources.getDimension(resId)
        setCornerRadius(radius, radius, radius, radius)
    }

    /**
     * Set the corner radius of a specific corner from a dimension resource id.
     *
     * @param corner the corner to set.
     * @param resId the dimension resource id of the corner radius.
     */
    fun setCornerRadiusDimen(@Corner corner: Int, @DimenRes resId: Int) {
        setCornerRadius(corner, resources.getDimensionPixelSize(resId).toFloat())
    }

    /**
     * Set the corner radius of a specific corner in px.
     *
     * @param corner the corner to set.
     * @param radius the corner radius to set in px.
     */
    fun setCornerRadius(@Corner corner: Int, radius: Float) {
        if (mCornerRadii!![corner] == radius) {
            return
        }
        mCornerRadii[corner] = radius
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    /**
     * Set the corner radii of each corner individually. Currently only one unique nonzero value is
     * supported.
     *
     * @param topLeft radius of the top left corner in px.
     * @param topRight radius of the top right corner in px.
     * @param bottomRight radius of the bottom right corner in px.
     * @param bottomLeft radius of the bottom left corner in px.
     */
    fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        if (mCornerRadii!![Corner.TOP_LEFT] == topLeft && mCornerRadii[Corner.TOP_RIGHT] == topRight && mCornerRadii[Corner.BOTTOM_RIGHT] == bottomRight && mCornerRadii[Corner.BOTTOM_LEFT] == bottomLeft) {
            return
        }
        mCornerRadii[Corner.TOP_LEFT] = topLeft
        mCornerRadii[Corner.TOP_RIGHT] = topRight
        mCornerRadii[Corner.BOTTOM_LEFT] = bottomLeft
        mCornerRadii[Corner.BOTTOM_RIGHT] = bottomRight
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun setBorderWidth(@DimenRes resId: Int) {
        setBorderWidth(resources.getDimension(resId))
    }

    fun setBorderWidth(width: Float) {
        if (borderWidth == width) {
            return
        }
        borderWidth = width
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    @get:ColorInt
    var borderColor: Int
        get() = borderColors!!.defaultColor
        set(color) {
            setBorderColor(ColorStateList.valueOf(color))
        }

    fun setBorderColor(colors: ColorStateList?) {
        if (borderColors == colors) {
            return
        }
        borderColors = colors ?: ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        if (borderWidth > 0) {
            invalidate()
        }
    }
    /**
     * Return true if this view should be oval and always set corner radii to half the height or
     * width.
     *
     * @return if this [RoundedImageView] is set to oval.
     */
    /**
     * Set if the drawable should ignore the corner radii set and always round the source to
     * exactly half the height or width.
     *
     * @param oval if this [RoundedImageView] should be oval.
     */
    var isOval: Boolean
        get() = mIsOval
        set(oval) {
            mIsOval = oval
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    var tileModeX: TileMode?
        get() = mTileModeX
        set(tileModeX) {
            if (mTileModeX == tileModeX) {
                return
            }
            mTileModeX = tileModeX
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    var tileModeY: TileMode?
        get() = mTileModeY
        set(tileModeY) {
            if (mTileModeY == tileModeY) {
                return
            }
            mTileModeY = tileModeY
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    /**
     * If `true`, we will also round the background drawable according to the settings on this
     * ImageView.
     *
     * @return whether the background is mutated.
     */
    fun mutatesBackground(): Boolean {
        return mMutateBackground
    }

    /**
     * Set whether the [RoundedImageView] should round the background drawable according to
     * the settings in addition to the source drawable.
     *
     * @param mutate true if this view should mutate the background drawable.
     */
    fun mutateBackground(mutate: Boolean) {
        if (mMutateBackground == mutate) {
            return
        }
        mMutateBackground = mutate
        updateBackgroundDrawableAttrs(true)
        invalidate()
    }

    companion object {
        // Constants for tile mode attributes
        private const val TILE_MODE_UNDEFINED = -2
        private const val TILE_MODE_CLAMP = 0
        private const val TILE_MODE_REPEAT = 1
        private const val TILE_MODE_MIRROR = 2
        const val TAG = "RoundedImageView"
        const val DEFAULT_RADIUS = 0f
        const val DEFAULT_BORDER_WIDTH = 0f
        val DEFAULT_TILE_MODE = TileMode.CLAMP
        private val SCALE_TYPES = arrayOf(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )

        private fun parseTileMode(tileMode: Int): TileMode? {
            return when (tileMode) {
                TILE_MODE_CLAMP -> TileMode.CLAMP
                TILE_MODE_REPEAT -> TileMode.REPEAT
                TILE_MODE_MIRROR -> TileMode.MIRROR
                else -> null
            }
        }
    }
}