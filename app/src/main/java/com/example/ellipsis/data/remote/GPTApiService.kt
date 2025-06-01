package com.example.ellipsis.data.remote

import com.example.ellipsis.data.model.MusicResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GPTApiService {
    @Multipart
    @POST("recommend-music")
    suspend fun recommendMusic(
        @Part file: MultipartBody.Part,
        @Part("hashtags") hashtags: RequestBody,
        @Part("labels") labels: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("datetime") datetime: RequestBody?
    ): MusicResponse
}