package com.example.ellipsis.presentation.main

import android.content.Context
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.ellipsis.data.remote.GPTApiService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val api: GPTApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(uri).allowHardware(false).build()
                val result = loader.execute(request)
                val bmp = (result as? SuccessResult)?.drawable?.toBitmap()
                if (bmp != null) {
                    _uiState.value = _uiState.value.copy(bitmap = bmp)
                }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val exif = ExifInterface(inputStream)
                    val date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: ""

                    val latLong = FloatArray(2)
                    var lat: Double? = null
                    var lon: Double? = null
                    var location = ""
                    if (exif.getLatLong(latLong)) {
                        lat = latLong[0].toDouble()
                        lon = latLong[1].toDouble()

                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            location = addresses[0].locality ?: addresses[0].adminArea ?: ""
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        photoDate = date,
                        latitude = lat,
                        longitude = lon,
                        locationName = location
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "이미지 처리 오류", e)
            }
        }
    }

    fun recommendMusic(context: Context, uri: Uri, hashtags: String) {
        val bmp = _uiState.value.bitmap ?: return
        viewModelScope.launch {
            try {
                val image = InputImage.fromBitmap(bmp, 0)
                val detector = ObjectDetection.getClient(
                    ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build()
                )

                val results = withContext(Dispatchers.IO) {
                    detector.process(image).await()
                }

                val labels = results.flatMap { it.labels.map { label -> label.text } }.distinct()
                val fileBody = context.contentResolver.openInputStream(uri)?.readBytes()
                    ?.toRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", "image.jpg", fileBody!!)

                val response = api.recommendMusic(
                    file = filePart,
                    hashtags = hashtags.toRequestBody("text/plain".toMediaTypeOrNull()),
                    labels = labels.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull()),
                    latitude = _uiState.value.latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    longitude = _uiState.value.longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    datetime = _uiState.value.photoDate.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                )

                _uiState.value = _uiState.value.copy(
                    detectedObjects = labels,
                    recommendedSongs = response.recommendations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
                Log.e("MainViewModel", "API 실패", e)
            }
        }
    }
}