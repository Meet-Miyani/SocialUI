package com.example.elasticshadow.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewPropertyAnimator
import android.view.ViewTreeObserver
import androidx.transition.TransitionManager
import com.example.elasticshadow.R
import com.example.elasticshadow.roundedimage.RoundedImageView
import io.armcha.elasticview.DebugPath
import io.armcha.elasticview.FastOutSlowInInterpolator
import io.armcha.elasticview.ShineProvider
import io.github.armcha.coloredshadow.BlurShadow
import io.github.armcha.coloredshadow.dpToPx
import kotlin.properties.Delegates

open class ElasticShadowView(context: Context?, val attrs: AttributeSet? = null) :
    RoundedImageView(context, attrs) {

    companion object {
        private const val DEFAULT_RADIUS = 0.5f
        private const val DEFAULT_COLOR = -1
        private const val BRIGHTNESS = -25f
        private const val SATURATION = 1.3f
        private const val TOP_OFFSET = 2f
        private const val PADDING = 20f
        internal const val DOWNSCALE_FACTOR = 0.25f
    }

    var radiusOffset by Delegates.vetoable(DEFAULT_RADIUS) { _, _, newValue ->
        newValue > 0F || newValue <= 1
    }

    var shadowColor = DEFAULT_COLOR

    var shadowEnabled = true


    ///////////////// ELASTIC PART \\\\\\\\\\\\\\\\\\\\\\\\\\

    private val ANIMATION_DURATION = 200L
    private val ANIMATION_DURATION_SHORT = 100L

    private var _isAnimating = false
    private var _isActionUpPerformed = false

    var shouldFlex = true

    private val _debugPath by lazy {
        DebugPath(this)
    }
    private val _shineProvider by lazy {
        ShineProvider(this)
    }

    //Will be available in next versions
    private var isShineEnabled = false

    var flexibility = 5f
        set(value) {
            if (value !in 1f..10f) {
                throw IllegalArgumentException("Flexibility must be between [1f..10f].")
            }
            field = value
        }

    var isDebugPathEnabled = false

    init {
        initBlur()
        initElastic()
    }

    private fun initBlur() {
        BlurShadow.init(context.applicationContext)
        cropToPadding = true
        super.setScaleType(ScaleType.CENTER_CROP)
        val padding = dpToPx(PADDING)
        setPadding(padding, padding, padding, padding)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ElasticShadowView, 0, 0)
        shadowColor = typedArray.getColor(R.styleable.ElasticShadowView_shadowColor, DEFAULT_COLOR)
        radiusOffset =
            typedArray.getFloat(R.styleable.ElasticShadowView_radiusOffset, DEFAULT_RADIUS)
        shadowEnabled = typedArray.getBoolean(R.styleable.ElasticShadowView_shadowEnabled,shadowEnabled)
        typedArray.recycle()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        setBlurShadow { super.setImageBitmap(bm) }
    }

    override fun setImageResource(resId: Int) {
        setBlurShadow { super.setImageResource(resId) }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        setBlurShadow { super.setImageDrawable(drawable) }
    }

    override fun setScaleType(scaleType: ScaleType) {
        super.setScaleType(ScaleType.CENTER_CROP)
    }

    override fun invalidate() {
        setBlurShadow { super.invalidate() }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        post {
            setBlurShadow { super.onSizeChanged(w, h, oldw, oldh) }
        }
    }

    fun ratioChanged(){
        setBlurShadow()
    }

    fun setImageResource(resId: Int, withShadow: Boolean) {
        if (withShadow) {
            setImageResource(resId)
        } else {
            background = null
            super.setImageResource(resId)
        }
    }

    fun setImageBitmap(bm: Bitmap?, withShadow: Boolean) {
        if (withShadow) {
            setImageBitmap(bm)
        } else {
            background = null
            super.setImageBitmap(bm)
        }
    }

    fun setImageDrawable(drawable: Drawable?, withShadow: Boolean) {
        if (withShadow) {
            setImageDrawable(drawable)
        } else {
            background = null
            super.setImageDrawable(drawable)
        }
    }

    private fun setBlurShadow(setImage: () -> Unit = {}) {
        background = null
        if (height != 0 || measuredHeight != 0) {
            setImage()
            makeBlurShadow()
        } else {
            val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    setImage()
                    makeBlurShadow()
                    return false
                }
            }
            viewTreeObserver.addOnPreDrawListener(preDrawListener)
        }
    }

    private fun makeBlurShadow() {
        if (shadowEnabled) {
            var radius = resources.getInteger(R.integer.radius).toFloat()
            radius *= 2 * radiusOffset
            val blur = BlurShadow.blur(this, width, height - dpToPx(TOP_OFFSET), radius)
            //brightness -255..255 -25 is default
            val colorMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, BRIGHTNESS,
                    0f, 1f, 0f, 0f, BRIGHTNESS,
                    0f, 0f, 1f, 0f, BRIGHTNESS,
                    0f, 0f, 0f, 1f, 0f
                )
            ).apply { setSaturation(SATURATION) }

            background = BitmapDrawable(resources, blur).apply {
                this.colorFilter = ColorMatrixColorFilter(colorMatrix)
                applyShadowColor(this)
            }
        }

        //super.setImageDrawable(null)
    }

    private fun applyShadowColor(bitmapDrawable: BitmapDrawable) {
        if (shadowColor != DEFAULT_COLOR) {
            bitmapDrawable.colorFilter = PorterDuffColorFilter(shadowColor, PorterDuff.Mode.SRC_IN)
        }
    }


    ///////////////// ELASTIC PART \\\\\\\\\\\\\\\\\\\\\\\\\

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (shouldFlex)
            processTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (isDebugPathEnabled)
            _debugPath.onDispatchDraw(canvas)
        if (isShineEnabled)
            _shineProvider.onDispatchDraw(canvas)
    }

    private fun initElastic() {
        isClickable = true
        context.obtainStyledAttributes(attrs, R.styleable.ElasticShadowView).apply {
            if (hasValue(R.styleable.ElasticShadowView_flexibility)) {
                flexibility = getFloat(R.styleable.ElasticShadowView_flexibility, flexibility)
            }
            if (hasValue(R.styleable.ElasticShadowView_flexEnabled)) {
                shouldFlex = getBoolean(R.styleable.ElasticShadowView_flexEnabled, shouldFlex)
            }
            recycle()
        }
    }

    private fun animator(body: ViewPropertyAnimator.() -> Unit) {
        animate().apply {
            interpolator = FastOutSlowInInterpolator()
            body()
            start()
        }
    }

    private fun animateToOriginalPosition() {
        animator {
            rotationX(0f)
            rotationY(0f)
            duration = ANIMATION_DURATION
        }
    }

    private fun calculateRotation(value: Float): Float {
        var tempValue = when {
            value < 0 -> 1f
            value > flexibility * 2 -> flexibility * 2
            else -> value
        }
        tempValue -= flexibility
        return tempValue
    }

    private fun processTouchEvent(event: MotionEvent) {
        val verticalRotation = calculateRotation((event.x * flexibility * 2) / width)
        val horizontalRotation = -calculateRotation((event.y * flexibility * 2) / height)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                animator {
                    rotationY(verticalRotation)
                    rotationX(horizontalRotation)
                    duration = ANIMATION_DURATION_SHORT
                    withStartAction {
                        _isActionUpPerformed = false
                        _isAnimating = true
                    }
                    withEndAction {
                        if (_isActionUpPerformed) {
                            animateToOriginalPosition()
                        } else {
                            _isAnimating = false
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                rotationY = verticalRotation
                rotationX = horizontalRotation
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                _isActionUpPerformed = true
                if (!_isAnimating) {
                    animateToOriginalPosition()
                }
            }
        }
    }

}