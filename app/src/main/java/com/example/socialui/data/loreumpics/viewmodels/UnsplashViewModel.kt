package com.example.socialui.data.loreumpics.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialui.data.loreumpics.data.UnsplashItem
import com.example.socialui.data.loreumpics.network.UnsplashApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnsplashViewModel : ViewModel() {
    val key = "wxf_IpefnR3QxJGFVkMIiTTjwa4-QcSQqUz6N0mPhB4"
    val unsplashImages: LiveData<List<UnsplashItem>> = MutableLiveData()

    init {
        viewModelScope.launch {
            unsplashImages as MutableLiveData
            unsplashImages.value = getUnsplashImages()
        }
    }

    private suspend fun getUnsplashImages(): List<UnsplashItem> {
        return withContext(Dispatchers.IO) {
            UnsplashApi().getImages(key,15).body()!!
        }
    }

}