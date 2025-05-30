package com.example.ellipsis

import android.graphics.Bitmap
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.InputStream
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpringMusicAppWithMLKit()
        }
    }
}

data class MusicResponse(
    val keywords: List<String>,
    val description: String,
    val location: String,
    val datetime: String?,
    val recommendations: List<String>
)

interface GPTApiService {
    @Multipart
    @POST("recommend-music")
    suspend fun recommendMusic(
        @Part file: MultipartBody.Part,
        @Part("hashtags") hashtags: okhttp3.RequestBody,
        @Part("labels") labels: okhttp3.RequestBody,
        @Part("latitude") latitude: okhttp3.RequestBody?,
        @Part("longitude") longitude: okhttp3.RequestBody?,
        @Part("datetime") datetime: okhttp3.RequestBody?
    ): MusicResponse
}

@Composable
fun SpringMusicAppWithMLKit() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detectedObjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var hashtags by remember { mutableStateOf(TextFieldValue("#봄 #데이트")) }
    var photoDate by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var locationName by remember { mutableStateOf("") }
    var recommendedSongs by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("http://192.168.219.109:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api = retrofit.create(GPTApiService::class.java)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("봄날 음악 추천 (ML Kit)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("사진 선택")
        }

        imageUri?.let { uri ->
            LaunchedEffect(uri) {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(uri).allowHardware(false).build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    bitmap = result.drawable.toBitmap()
                }

                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.let {
                    val exif = ExifInterface(it)
                    photoDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: ""

                    val latLong = FloatArray(2)
                    if (exif.getLatLong(latLong)) {
                        latitude = latLong[0].toDouble()
                        longitude = latLong[1].toDouble()

                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
                        if (!addresses.isNullOrEmpty()) {
                            locationName = addresses[0].locality ?: addresses[0].adminArea ?: ""
                        }
                    }
                }
            }

            bitmap?.let { bmp ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.height(200.dp).fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    coroutineScope.launch {
                        try {
                            val image = InputImage.fromBitmap(bmp, 0)
                            val options = ObjectDetectorOptions.Builder()
                                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                                .enableMultipleObjects()
                                .enableClassification()
                                .build()

                            val detector = ObjectDetection.getClient(options)
                            val results = withContext(Dispatchers.IO) {
                                detector.process(image).await()
                            }

                            val labels = results.flatMap { it.labels.map { label -> label.text } }
                            detectedObjects = labels.distinct()

                            val fileRequestBody = context.contentResolver.openInputStream(uri)!!.readBytes()
                                .toRequestBody("image/*".toMediaTypeOrNull())
                            val multipartBody = MultipartBody.Part.createFormData("file", "image.jpg", fileRequestBody)

                            val hashtagsBody = hashtags.text.toRequestBody("text/plain".toMediaTypeOrNull())
                            val labelsBody = detectedObjects.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())
                            val latBody = latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            val lonBody = longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            val datetimeBody = photoDate.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())

                            val response = api.recommendMusic(
                                file = multipartBody,
                                hashtags = hashtagsBody,
                                labels = labelsBody,
                                latitude = latBody,
                                longitude = lonBody,
                                datetime = datetimeBody
                            )
                            recommendedSongs = response.recommendations
                        } catch (e: Exception) {
                            errorMessage = "오류 발생: ${e.localizedMessage}"
                            Log.e("App", "API 실패", e)
                        }
                    }
                }) {
                    Text("객체 인식 및 음악 추천")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("해시태그 입력")
        BasicTextField(
            value = hashtags,
            onValueChange = { hashtags = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("촬영 시간: $photoDate")
        Text("촬영 위치: $locationName")
        Text("인식된 객체: ${detectedObjects.joinToString()}", color = MaterialTheme.colorScheme.secondary)

        if (recommendedSongs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("추천 음악:", style = MaterialTheme.typography.titleMedium)
            recommendedSongs.forEach { song ->
                Text("\uD83C\uDFB5 $song")
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("${errorMessage}", color = MaterialTheme.colorScheme.error)
        }
    }
}
