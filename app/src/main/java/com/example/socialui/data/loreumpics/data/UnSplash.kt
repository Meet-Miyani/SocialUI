package com.example.socialui.data.loreumpics.data

data class UnSplashResponse(
    val list: List<UnsplashItem>? = null
)

data class UnsplashItem(
    var id: String = "",
    var width: Int = 0,
    var height: Int = 0,
    var likes: Int = 0,
    var urls: Urls? = null,
    var user: User? = null,
    var liked: Boolean = false
)

data class Urls(
    var raw: String? = null,
    var full: String? = null,
    var regular: String? = null,
    var small: String? = null,
    var thumb: String? = null,
    var small_s3: String? = null,
)

data class User(
    var id: String = "",
    var first_name: String = "",
    var last_name: String = "",
    var profile_image: UserProfile? = null,
)

data class UserProfile(
    var small: String = "",
    var medium: String = "",
    var large: String = "",
)
