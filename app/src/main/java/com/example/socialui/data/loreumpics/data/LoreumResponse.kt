package com.example.socialui.data.loreumpics.data

data class LoreumViewModelResponse(
    val loreumResponse: List<LoreumResponseItem?>? = null
)

data class LoreumResponseItem(
    val author: String? = null,
    val width: Int? = null,
    val download_url: String? = null,
    val id: String? = null,
    val url: String? = null,
    val height: Int? = null
)

