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
import org.json.JSONObject
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
                    val exifMap = mutableMapOf<String, String?>()

                    val allTags = listOf(
                        ExifInterface.TAG_MAKE,
                        ExifInterface.TAG_MODEL,
                        ExifInterface.TAG_DATETIME_ORIGINAL,
                        ExifInterface.TAG_EXPOSURE_TIME,
                        ExifInterface.TAG_F_NUMBER,
                        ExifInterface.TAG_ISO_SPEED_RATINGS,
                        ExifInterface.TAG_WHITE_BALANCE,
                        ExifInterface.TAG_FLASH,
                        ExifInterface.TAG_IMAGE_WIDTH,
                        ExifInterface.TAG_IMAGE_LENGTH,
                        ExifInterface.TAG_COLOR_SPACE,
                        ExifInterface.TAG_SATURATION,
                        ExifInterface.TAG_CONTRAST,
                        ExifInterface.TAG_SHARPNESS,
                        ExifInterface.TAG_GPS_LATITUDE,
                        ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_APERTURE,
                        ExifInterface.TAG_BRIGHTNESS_VALUE,
                        ExifInterface.TAG_GPS_ALTITUDE,
                        ExifInterface.TAG_GPS_PROCESSING_METHOD
                    )

                    for (tag in allTags) {
                        exifMap[tag] = exif.getAttribute(tag)
                    }

                    val latLong = FloatArray(2)
                    var lat: Double? = null
                    var lon: Double? = null
                    var location = ""

                    if (exif.getLatLong(latLong)) {
                        lat = latLong[0].toDouble()
                        lon = latLong[1].toDouble()

                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                            if (!addresses.isNullOrEmpty()) {
                                location = addresses[0].locality ?: addresses[0].adminArea ?: ""
                            }
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "지오코딩 실패", e)
                        }
                    }

                    val exifJson = JSONObject(exifMap.filterValues { it != null }).toString()

                    Log.d("MainViewModel", "추출된 EXIF JSON:\n$exifJson")

                    _uiState.value = _uiState.value.copy(
                        photoDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: "",
                        latitude = lat,
                        longitude = lon,
                        locationName = location,
                        exifJson = exifJson
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "이미지 처리 오류", e)
                _uiState.value = _uiState.value.copy(errorMessage = "이미지 처리 실패: ${e.localizedMessage}")
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

                val fileBytes = context.contentResolver.openInputStream(uri)?.readBytes()
                if (fileBytes == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "파일을 읽을 수 없습니다")
                    return@launch
                }

                val fileBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("files", "image.jpg", fileBody)

                Log.d("MainViewModel", "📤 서버 전송 정보 요약:\n" +
                        "hashtags = $hashtags\n" +
                        "cameraInfo = ${labels.joinToString(",")}\n" +
                        "latitude = ${_uiState.value.latitude}\n" +
                        "longitude = ${_uiState.value.longitude}\n" +
                        "datetime = ${_uiState.value.photoDate}\n" +
                        "exifData = ${_uiState.value.exifJson}"
                )

                val response = api.recommendMusic(
                    files = listOf(filePart),
                    hashtags = hashtags.toRequestBody("text/plain".toMediaTypeOrNull()),
                    exifData = _uiState.value.exifJson.toRequestBody("text/plain".toMediaTypeOrNull()),
                    cameraInfo = labels.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull()),
                    latitude = _uiState.value.latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    longitude = _uiState.value.longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    datetime = _uiState.value.photoDate.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                )

                val songs = response.tracks?.map { "${it.artist} - ${it.title}" } ?: emptyList()


                _uiState.value = _uiState.value.copy(
                    detectedObjects = labels,
                    recommendedSongs = songs,
                    errorMessage = null
                )

                Log.d("MainViewModel", "API 성공: ${response.emotion}, 곡 수: ${songs.size}")

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is retrofit2.HttpException -> {
                        "서버 오류 (${e.code()}): ${e.message()}"
                    }
                    else -> "요청 실패: ${e.localizedMessage}"
                }

                _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
                Log.e("MainViewModel", "API 실패", e)
            }
        }
    }
}
