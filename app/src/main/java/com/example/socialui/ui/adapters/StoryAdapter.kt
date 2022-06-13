package com.example.socialui.ui.adapters

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialui.R
import com.example.socialui.data.models.Story
import com.example.socialui.databinding.StoryHolderBinding

class StoryAdapter : ListAdapter<Story, StoryAdapter.StoryHolder>(Companion) {

    class StoryHolder(val binding: StoryHolderBinding) : RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem.profileName == newItem.profileName

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryHolder {
        val binding = StoryHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryHolder(binding)
    }


    override fun onBindViewHolder(holder: StoryHolder, position: Int) {
        val story = currentList[position]
        with(holder.binding){
            profileNameTextView.text = story.profileName
            profileStoryImageView.setImageResource(R.drawable.test)
            Glide.with(profileImageView).load(story.profilePhoto).into(profileImageView)
        }
    }
}

class StoryItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount-1){
            outRect.right = 48
        }
    }
}