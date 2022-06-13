package com.example.socialui.data.loreumpics.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialui.data.loreumpics.network.LoreumViewModelApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoreumImageViewModel : ViewModel() {
    val loreumImages: LiveData<List<String>?> = MutableLiveData()

    init {
        viewModelScope.launch {
            loreumImages as MutableLiveData
            loreumImages.value = getLoreumImages()

        }
    }

    private suspend fun getLoreumImages(): List<String> {
        val imageList = ArrayList<String>()
        return withContext(Dispatchers.IO) {
            for (loreum in LoreumViewModelApi().getImages().body()!!) {
                if (!loreum.download_url.isNullOrEmpty())
                    imageList.add(loreum.download_url)
            }
            imageList
        }
    }
}