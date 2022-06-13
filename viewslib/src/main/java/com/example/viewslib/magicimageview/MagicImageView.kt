package com.example.viewslib.magicimageview

import android.animation.Animator
import androidx.appcompat.widget.AppCompatImageView
import android.graphics.Bitmap
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class MagicImageView : AppCompatImageView {
    private var nextBitmap: Bitmap? = null
    private var animatedBitmap: Bitmap? = null
    private var isAnimating = false
    private val src = Rect()
    private val dest = Rect()
    private val mov = Rect()
    private val viewRect = Rect()
    private val loc = IntArray(2)
    private var valueAnimator: ValueAnimator? = null
    private var magicAnimationCompleteListener: MagicAnimationCompleteListener? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            getLocationOnScreen(loc)
            src[loc[0], loc[1], loc[0] + width] = loc[1] + height
            viewRect[left, top, right] = bottom
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (nextBitmap == null) {
            nextBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val originalCanvas = Canvas(nextBitmap!!)
            super.onDraw(originalCanvas)
        }
        if (isAnimating) {
            canvas.drawBitmap(nextBitmap!!, null, viewRect, null)
            canvas.drawBitmap(animatedBitmap!!, null, mov, null)
        } else {
            canvas.drawBitmap(nextBitmap!!, null, viewRect, null)
        }
    }

    fun animate(dst: View, bitmap: Bitmap?) {
        if (valueAnimator != null && valueAnimator!!.isRunning) valueAnimator!!.cancel()
        val pos = IntArray(2)
        dst.getLocationOnScreen(pos)
        dest[pos[0], pos[1], pos[0] + dst.width] = pos[1] + dst.height
        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator?.let { it.addUpdateListener(AnimatorUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Float
            val left = ((dest.left - src.left) * `val`).toInt()
            val top = ((dest.top - src.top) * `val`).toInt()
            val r = dest.left - src.left + dst.width
            val b = dest.top - src.top + dst.height
            val right = viewRect.right - ((viewRect.right - r) * `val`).toInt()
            val bottom = viewRect.bottom - ((viewRect.bottom - b) * `val`).toInt()
            mov[left, top, right] = bottom
            invalidate()
        }) }
        valueAnimator?.let { it.addListener(object : AnimatorListenerAdapter() {
            private var isCancelled = false
            override fun onAnimationCancel(animation: Animator) {
                isAnimating = false
                isCancelled = true
                nextBitmap = animatedBitmap
            }

            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
                if (magicAnimationCompleteListener != null && !isCancelled) {
                    magicAnimationCompleteListener!!.onComplete(animatedBitmap)
                }
            }

            override fun onAnimationStart(animation: Animator) {
                animatedBitmap = nextBitmap
                nextBitmap = bitmap
                isAnimating = true
            }

            override fun onAnimationPause(animation: Animator) {
                isAnimating = false
            }

            override fun onAnimationResume(animation: Animator) {
                isAnimating = true
            }
        })
            it.duration = 200
            it.interpolator = DecelerateInterpolator()
        it.start()
        }
    }

    fun setMagicAnimationCompleteListener(magicAnimationCompleteListener: MagicAnimationCompleteListener?) {
        this.magicAnimationCompleteListener = magicAnimationCompleteListener
    }
}