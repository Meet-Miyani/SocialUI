/*
 * Copyright 2021 vivekverma
 *
 * Licensed under the MIT License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://github.com/virtualvivek/BlurShadowImageView/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.viewslib.blurshadow

import android.content.Context
import android.content.res.Resources
import kotlin.jvm.JvmOverloads
import android.widget.RelativeLayout
import android.view.Gravity
import android.graphics.*
import android.widget.ImageView.ScaleType
import android.graphics.drawable.BitmapDrawable
import android.widget.LinearLayout
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.example.viewslib.R
import com.example.viewslib.blurshadow.helpers.FadingImageView
import com.example.viewslib.blurshadow.helpers.RoundImageView

/**
 * ================================================
 * virtualvivek7@gmail.com
 * -version：4.0
 * -updated ：1 Sep 2021
 * Developed by：
 * Vivek Verma
 * ================================================
 */
class BlurShadowImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var imageRound = dpToPx(10)
    private var shadowOffset = dpToPx(50)
    var mInvalidat = false
    private var blurredImage: Bitmap? = null

//
//    private val ANIMATION_DURATION = 200L
//    private val ANIMATION_DURATION_SHORT = 100L
//
//    private var _isAnimating = false
//    private var _isActionUpPerformed = false
//
//    private var shouldFlex = true
//
//    private val _debugPath by lazy {
//        DebugPath(this)
//    }
//    private val _shineProvider by lazy {
//        ShineProvider(this)
//    }
//    //Will be available in next versions
//    private var isShineEnabled = false
//
//    var flexibility = 5f
//        set(value) {
//            if (value !in 1f..10f) {
//                throw IllegalArgumentException("Flexibility must be between [1f..10f].")
//            }
//            field = value
//        }
//
//    var isDebugPathEnabled = false
//
//    init {
//        isClickable = true
//        init(attrs)
//    }
//
//    fun enableFlex(flag:Boolean){
//        shouldFlex = flag
//    }
//
//    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
//        if (shouldFlex)
//            processTouchEvent(event)
//        return super.dispatchTouchEvent(event)
//    }
//
//    override fun dispatchDraw(canvas: Canvas?) {
//        super.dispatchDraw(canvas)
//        if (isDebugPathEnabled)
//            _debugPath.onDispatchDraw(canvas)
//        if (isShineEnabled)
//            _shineProvider.onDispatchDraw(canvas)
//    }
//
//    private fun processTouchEvent(event: MotionEvent) {
//        val verticalRotation = calculateRotation((event.x * flexibility * 2) / width)
//        val horizontalRotation = -calculateRotation((event.y * flexibility * 2) / height)
//
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                animator {
//                    rotationY(verticalRotation)
//                    rotationX(horizontalRotation)
//                    duration = ANIMATION_DURATION_SHORT
//                    withStartAction {
//                        _isActionUpPerformed = false
//                        _isAnimating = true
//                    }
//                    withEndAction {
//                        if (_isActionUpPerformed) {
//                            animateToOriginalPosition()
//                        } else {
//                            _isAnimating = false
//                        }
//                    }
//                }
//            }
//            MotionEvent.ACTION_MOVE -> {
//                rotationY = verticalRotation
//                rotationX = horizontalRotation
//            }
//            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
//                _isActionUpPerformed = true
//                if (!_isAnimating) {
//                    animateToOriginalPosition()
//                }
//            }
//        }
//    }
//
//    private fun init(attrs: AttributeSet?) {
//        context.obtainStyledAttributes(attrs, R.styleable.BlurShadowImageView).apply {
//            if (hasValue(R.styleable.BlurShadowImageView_flexibility)) {
//                flexibility = getFloat(R.styleable.BlurShadowImageView_flexibility, flexibility)
//            }
//            recycle()
//        }
//    }
//
//    private fun animator(body: ViewPropertyAnimator.() -> Unit) {
//        animate().apply {
//            interpolator = FastOutSlowInInterpolator()
//            body()
//            start()
//        }
//    }
//
//    private fun animateToOriginalPosition() {
//        animator {
//            rotationX(0f)
//            rotationY(0f)
//            duration = ANIMATION_DURATION
//        }
//    }
//
//    private fun calculateRotation(value: Float): Float {
//        var tempValue = when {
//            value < 0 -> 1f
//            value > flexibility * 2 -> flexibility * 2
//            else -> value
//        }
//        tempValue -= flexibility
//        return tempValue
//    }



    private fun initView(context: Context, attrs: AttributeSet?) {
        val roundImageView = RoundImageView(context)
        val blurImageView = FadingImageView(context)
        var imageresource: Int
        val imageScaleTypeIndex: Int
        gravity = Gravity.CENTER
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        imageresource = -1
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.BlurShadowImageView)
            if (a.hasValue(R.styleable.BlurShadowImageView_v_imageSrc)) {
                imageresource = a.getResourceId(R.styleable.BlurShadowImageView_v_imageSrc, -1)
            }
            imageRound =
                a.getDimensionPixelSize(R.styleable.BlurShadowImageView_v_imageRound, imageRound)
            shadowOffset = a.getDimensionPixelSize(
                R.styleable.BlurShadowImageView_v_shadowOffset,
                shadowOffset
            )
            imageScaleTypeIndex = a.getInt(R.styleable.BlurShadowImageView_android_scaleType, -1)
            if (imageScaleTypeIndex >= 0) {
                val types = ScaleType.values()
                val scaleType = types[imageScaleTypeIndex]
                roundImageView.scaleType = scaleType
            } else {
                roundImageView.scaleType = ScaleType.CENTER_CROP
            }
            a.recycle()
        } else {
            val density = context.resources.displayMetrics.density
            imageRound = (imageRound * density).toInt()
            imageresource = -1
        }

        //---- Layer ImageView ---------------------------------------------------------------------
        if (imageresource == -1) {
            roundImageView.setImageResource(android.R.color.transparent)
        } else {
            roundImageView.setImageResource(imageresource)
        }


        //---- Layer BlurView ----------------------------------------------------------------------
        blurImageView.scaleType = ScaleType.CENTER_CROP
        //Blurring techinique without renderscript --------------
        if (imageresource == -1) {
            val image = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
            image.eraseColor(Color.LTGRAY)
            blurImageView.setImageBitmap(image)
        } else {
            val bitmap = BitmapFactory.decodeResource(resources, imageresource)
            if (!isInEditMode) {
                blurredImage = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
                blurImageView.setImageBitmap(blurredImage)
            } else {
                val bitmapDrawable = BitmapDrawable(context.resources, bitmap)
                bitmapDrawable.setColorFilter(-0x666667, PorterDuff.Mode.ADD)
                blurImageView.setImageDrawable(bitmapDrawable)
            }
        }

        // Setting Layour For BlurView --
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        blurImageView.layoutParams = lp

        // Setting Layout for RoundImageView --
        val lp2 = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp2.addRule(CENTER_HORIZONTAL, TRUE)
        lp2.setMargins(dpToPx(15), 0, dpToPx(15), shadowOffset)
        roundImageView.layoutParams = lp2
        addView(blurImageView)
        addView(roundImageView)
        viewTreeObserver.addOnGlobalLayoutListener {
            var N = childCount
            for (i in 0 until N) {
                N = childCount
            }
            (getChildAt(1) as RoundImageView).setRound(imageRound)
            mInvalidat = true
        }
    }

    fun setImageResource(resId: Int) {
        //Setting RoundedImageView layer
        (getChildAt(1) as RoundImageView).setImageResource(resId)

        //Setting FadedBlurredImageView layer
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        blurredImage = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        (getChildAt(0) as FadingImageView).setImageBitmap(blurredImage)
        invalidate()
        mInvalidat = true
    }

    fun setImageDrawable(drawable: Drawable) {
        //Setting RoundedImageView layer
        (getChildAt(1) as RoundImageView).setImageDrawable(drawable)

        //Setting FadedBlurredImageView layer
        val bitmap = (drawable as BitmapDrawable).bitmap
        blurredImage = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        (getChildAt(0) as FadingImageView).setImageBitmap(blurredImage)
        invalidate()
        mInvalidat = true
    }

    fun setImageBitmap(bitmap: Bitmap?) {
        //Setting RoundedImageView layer
        (getChildAt(1) as RoundImageView).setImageBitmap(bitmap)

        //Setting FadedBlurredImageView layer
        blurredImage = Bitmap.createScaledBitmap(bitmap!!, 8, 8, true)
        (getChildAt(0) as FadingImageView).setImageBitmap(blurredImage)
        invalidate()
        mInvalidat = true
    }

    fun setImageRadius(radius_: Int) {
        var radius_ = radius_
        if (radius_ > getChildAt(1).width / 2 || radius_ > getChildAt(1).height / 2) {
            radius_ = if (getChildAt(1).width > getChildAt(1).height) {
                getChildAt(1).height / 2
            } else {
                getChildAt(1).width / 2
            }
        }
        imageRound = radius_
        (getChildAt(1) as RoundImageView).setRound(imageRound)
        invalidate()
        mInvalidat = true
    }

    companion object {
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }

    init {
        initView(context, attrs)
    }
}