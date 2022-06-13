package com.example.socialui.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialui.data.loreumpics.viewmodels.UnsplashViewModel
import com.example.socialui.data.models.Story
import com.example.socialui.databinding.FragmentHomeBinding
import com.example.socialui.ui.adapters.FeedItemDecoration
import com.example.socialui.ui.adapters.FeedsAdapter
import com.example.socialui.ui.adapters.StoryAdapter
import com.example.socialui.ui.adapters.StoryItemDecoration


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var feedsAdapter: FeedsAdapter

    private lateinit var unsplashViewModel: UnsplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStoriesRecyclerView()
        setupFeedsRecyclerView()
        storyAdapter.submitList(dummyData())

        unsplashViewModel = ViewModelProvider(this)[UnsplashViewModel::class.java]

        unsplashViewModel.unsplashImages.observe(viewLifecycleOwner) {
            Log.d(TAG, "onViewCreated: $it")
            feedsAdapter.submitList(it)
        }

    }


    private fun setupStoriesRecyclerView() = binding.storiesRecyclerView.apply {
        storyAdapter = StoryAdapter()
        adapter = storyAdapter
        layoutManager = LinearLayoutManager(this.context, RecyclerView.HORIZONTAL, false)
        addItemDecoration(StoryItemDecoration())
    }

    private fun setupFeedsRecyclerView() = binding.postFeedRecyclerView.apply {
        feedsAdapter = FeedsAdapter()
        adapter = feedsAdapter
        layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        addItemDecoration(FeedItemDecoration())
    }

    private fun dummyData() = arrayListOf<Story>(
        Story(profilePhoto = "https://reqres.in/img/faces/7-image.jpg", profileName = "Meet"),
        Story(profilePhoto = "https://reqres.in/img/faces/8-image.jpg", profileName = "Parth"),
        Story(profilePhoto = "https://reqres.in/img/faces/9-image.jpg", profileName = "Jay"),
        Story(profilePhoto = "https://reqres.in/img/faces/10-image.jpg", profileName = "Neha"),
        Story(profilePhoto = "https://reqres.in/img/faces/11-image.jpg", profileName = "Jay"),
        Story(profilePhoto = "https://reqres.in/img/faces/12-image.jpg", profileName = "Harsh"),
        Story(profilePhoto = "https://reqres.in/img/faces/1-image.jpg", profileName = "Kishan"),
        Story(profilePhoto = "https://reqres.in/img/faces/2-image.jpg", profileName = "Zarna"),
        Story(profilePhoto = "https://reqres.in/img/faces/3-image.jpg", profileName = "Srushti"),
        Story(profilePhoto = "https://reqres.in/img/faces/4-image.jpg", profileName = "Dhaval"),
        Story(profilePhoto = "https://reqres.in/img/faces/5-image.jpg", profileName = "Darshit"),
    )

    companion object {
        private const val TAG = "Home_Fragment"
    }
}