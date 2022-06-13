package com.example.viewslib.elasticanimviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewPropertyAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.viewslib.R
import com.example.viewslib.elasticanimviews.helpers.DebugPath
import com.example.viewslib.elasticanimviews.helpers.FastOutSlowInInterpolator
import com.example.viewslib.elasticanimviews.helpers.ShineProvider

class ElasticConstraintLayout(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    private val ANIMATION_DURATION = 200L
    private val ANIMATION_DURATION_SHORT = 100L

    private var _isAnimating = false
    private var _isActionUpPerformed = false

    private var shouldFlex = true

    private val _debugPath by lazy {
        DebugPath(this)
    }
    private val _shineProvider by lazy {
        ShineProvider(this)
    }

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
        isClickable = true
        init(attrs)
    }

    fun enableFlex(flag:Boolean){
        shouldFlex = flag
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (shouldFlex)
            processTouchEvent(event)
        return super.dispatchTouchEvent(event)
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


    private fun init(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ElasticStyle).apply {
            if (hasValue(R.styleable.ElasticStyle_flexibility)) {
                flexibility = getFloat(R.styleable.ElasticStyle_flexibility, flexibility)
            }
            if (hasValue(R.styleable.ElasticStyle_flexEnabled)) {
                shouldFlex = getBoolean(R.styleable.ElasticStyle_flexEnabled, shouldFlex)
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

}