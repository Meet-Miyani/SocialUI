package com.example.socialui.data.loreumpics.network

import com.example.socialui.data.loreumpics.data.UnsplashItem
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApi {


    @GET("/photos")
    suspend fun getImages(
        @Query("client_id") client_key: String,
        @Query("per_page") perPage: Int
    ): Response<List<UnsplashItem>>

    companion object {
        val BASE_URL = "https://api.unsplash.com/"

        operator fun invoke(): UnsplashApi {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(UnsplashApi::class.java)
        }
    }

}


//@GET("v2/list?limit=100")
//suspend fun getImages(): Response<List<LoreumResponseItem>>
//
//companion object {
//    val BASE_URL = "https://picsum.photos/300/"
//
//    operator fun invoke(): LoreumViewModelApi {
//        return Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create())
//            .baseUrl(BASE_URL)
//            .build()
//            .create(LoreumViewModelApi::class.java)
//    }
//}