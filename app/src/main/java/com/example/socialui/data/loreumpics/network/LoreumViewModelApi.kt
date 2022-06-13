package com.example.socialui.data.loreumpics.network

import com.example.socialui.data.loreumpics.data.LoreumResponseItem
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface LoreumViewModelApi {

    @GET("v2/list?limit=100")
    suspend fun getImages(): Response<List<LoreumResponseItem>>

    companion object {
        val BASE_URL = "https://picsum.photos/300/"

        operator fun invoke(): LoreumViewModelApi {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(LoreumViewModelApi::class.java)
        }
    }
}