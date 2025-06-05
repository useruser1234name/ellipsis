package com.example.ellipsis.data.remote

import com.example.ellipsis.data.model.MusicResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GPTApiService {
    @Multipart
    @POST("recommend-music")
    suspend fun recommendMusic(
        @Part files: List<MultipartBody.Part>, // 서버는 files 배열을 받음
        @Part("hashtags") hashtags: RequestBody,
        @Part("exif_data") exifData: RequestBody = "{}".toRequestBody("text/plain".toMediaTypeOrNull()), // 서버 필수 필드
        @Part("camera_info") cameraInfo: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part("altitude") altitude: RequestBody? = null,
        @Part("location_source") locationSource: RequestBody? = "gps".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("datetime") datetime: RequestBody?,
        @Part("timezone") timezone: RequestBody? = null,
        @Part("image_count") imageCount: RequestBody = "1".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("total_file_size") totalFileSize: RequestBody = "0".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("app_version") appVersion: RequestBody = "1.0.0".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("device_model") deviceModel: RequestBody? = null,
        @Part("android_version") androidVersion: RequestBody? = null,
        @Part("additional_context") additionalContext: RequestBody? = null
    ): MusicResponse
}