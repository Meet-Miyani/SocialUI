package com.example.socialui.ui.adapters

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialui.R
import com.example.socialui.data.loreumpics.data.UnsplashItem
import com.example.socialui.databinding.FeedsHolderBinding

class FeedsAdapter : ListAdapter<UnsplashItem, FeedsAdapter.FeedsHolder>(Companion) {

    class FeedsHolder(val binding: FeedsHolderBinding) : RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<UnsplashItem>() {
        override fun areItemsTheSame(oldItem: UnsplashItem, newItem: UnsplashItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UnsplashItem, newItem: UnsplashItem): Boolean {
            return oldItem.id == newItem.id

        }

        private const val TAG = "Feeds_Adapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsHolder {
        val binding = FeedsHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedsHolder(binding)
    }


    override fun onBindViewHolder(holder: FeedsHolder, position: Int) {
        val feed = currentList[position]
        with(holder.binding) {
            Glide.with(feedImage).load(feed.urls?.regular).sizeMultiplier(0.5f).into(feedImage)
            adjustImageHolder(feed.width.toDouble() / feed.height.toDouble())

            //User Setting
            feed.user?.let {
                Glide.with(feedUserProfile).load(it.profile_image!!.small).into(feedUserProfile)
                feedUserName.text = it.first_name
                tvUserNameAndCaption.text = "${it.first_name} Sample Caption.."
            }

            // Likes and Comments
            tvLikesCountFeedPost.text = getRandomLikesValue().toString()
            tvCommentsCountFeedPost.text = getRandomCommentValue().toString()

            btnLikeFeedPost.setOnClickListener {
               if (!feed.liked){
                   btnLikeFeedPost.setImageResource(R.drawable.ic_like_filled_icon)
                   feed.liked = true
               } else{
                   btnLikeFeedPost.setImageResource(R.drawable.ic_like_new_icon)
                   feed.liked = false
               }
            }

            feedImage.setOnClickListener(object : DoubleClickListener(){
                override fun onDoubleClick(v: View) {
                    btnLikeFeedPost.performClick()
                }
            })

        }
    }

    private fun getRandomLikesValue(): Int {
        return (0..10000).random()
    }

    private fun getRandomCommentValue(): Int {
        return (0..1000).random()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun FeedsHolderBinding.adjustImageHolder(aspectRatio: Double) {
        ConstraintSet().apply {
            var ratio = aspectRatio
            when {
                // 1:1
                aspectRatio == 1.0 -> {
                    Log.d(TAG, "adjustImageHolder: 1:1")
                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_1_1_feed_mask)
                }

                // 9:16
                aspectRatio < 0.6 -> {
                    Log.d(TAG, "adjustImageHolder: 9:16")
                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_3_4_feed_mask)
                    ratio = 3.0 / 4.0
                }

                // 2:3 , 3:4
                aspectRatio >= 0.6 && aspectRatio < 1.0 -> {
                    Log.d(TAG, "adjustImageHolder: 2:3 , 3:4")
                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_3_4_feed_mask)
                }

                // 3:2 , 4:3
                aspectRatio > 1.0 && aspectRatio <= 1.5 -> {
                    Log.d(TAG, "adjustImageHolder: 3:2 , 4:3")
                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_4_3_feed_mask)
                    ratio = 4.0 / 3.0
                }

                // 16:9 ..
                aspectRatio > 1.5 && aspectRatio <= 2.0 -> {
                    Log.d(TAG, "adjustImageHolder: 3:2 , 4:3")
                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_16_9_feed_mask)
                }

                else -> {
                    Log.d(TAG, "adjustImageHolder: UNKNOWN")
                }


//                1.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 1:1")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_1_1_feed_mask)
//                }
//                4.0 / 3.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 4:3")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_4_3_feed_mask)
//                }
//                3.0 / 4.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 3:4")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_3_4_feed_mask)
//                }
//                16.0 / 9.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 16:9")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_16_9_feed_mask)
//                }
//                9.0 / 16.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 9:16")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_3_4_feed_mask)
//                    ratio = 3.0 / 4.0
//                }
//                3.0 / 2.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 3:2")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_4_3_feed_mask)
//                }
//                2.0 / 3.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 2:3")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_3_4_feed_mask)
//                }
//                994.0 / 663.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 2:3")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_4_3_feed_mask)
//                }
//                5374.0 / 8060.0 -> {
//                    Log.d(TAG, "adjustImageHolder: 2:3")
//                    feedImage.shape = feedImage.context.getDrawable(R.drawable.ic_3_4_feed_mask)
//                }
//                else -> {
//                    Log.d(TAG, "adjustImageHolder: Unknown")
//                }
            }

            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(feedImage.id, ratio.toString())
                applyTo(root)
            }
        }
    }
}

class FeedItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
            outRect.bottom = 240
        }
    }
}

abstract class DoubleClickListener : View.OnClickListener {
    private var lastClickTime: Long = 0
    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        }
        lastClickTime = clickTime
    }
    abstract fun onDoubleClick(v: View)
    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}