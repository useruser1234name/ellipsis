package com.example.ellipsis.data.remote

import com.example.ellipsis.data.model.MusicResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.*

interface GPTApiService {

    /**
     * 실제 서버 API에 맞는 음악 추천
     * 서버가 실제로 받는 파라미터만 사용
     */
    @Multipart
    @POST("recommend-music")
    suspend fun recommendMusic(
        @Part image: MultipartBody.Part,
        @Part("hashtags") hashtags: RequestBody,
        @Part("cultural_context") culturalContext: RequestBody,
        @Part("max_songs") maxSongs: RequestBody,
        @Part("facial_analysis_weight") facialAnalysisWeight: RequestBody
    ): MusicResponse

    /**
     * 얼굴 분석만 수행 (테스트용)
     */
    @Multipart
    @POST("analyze-face")
    suspend fun analyzeFace(
        @Part image: MultipartBody.Part
    ): FaceAnalysisResponse

    /**
     * 시스템 헬스 체크
     */
    @GET("health")
    suspend fun healthCheck(): HealthCheckResponse

    /**
     * 루트 엔드포인트
     */
    @GET("/")
    suspend fun getRoot(): RootResponse

    /**
     * 시스템 정보
     */
    @GET("info")
    suspend fun getSystemInfo(): SystemInfoResponse

    // =============== 추후 확장용 (현재 서버에서 지원하지 않음) ===============

    /**
     * 사용자 피드백 전송 (추후 구현)
     */
    @POST("feedback")
    suspend fun sendFeedback(
        @Body feedback: UserFeedback
    ): Response<FeedbackResponse>

    /**
     * 감정 히스토리 조회 (추후 구현)
     */
    @GET("emotion-history/{userId}")
    suspend fun getEmotionHistory(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 50,
        @Query("days") days: Int = 30
    ): Response<EmotionHistoryResponse>

    /**
     * 개인화 설정 업데이트 (추후 구현)
     */
    @PUT("user-preferences/{userId}")
    suspend fun updateUserPreferences(
        @Path("userId") userId: String,
        @Body preferences: UserPreferences
    ): Response<Unit>
}

// =============== Response 데이터 클래스들 ===============

/**
 * 헬스 체크 응답 (실제 서버 응답에 맞게 수정)
 */
data class HealthCheckResponse(
    val status: String,
    val timestamp: Double,
    val recommender_ready: Boolean
)

/**
 * 루트 엔드포인트 응답
 */
data class RootResponse(
    val message: String,
    val version: String,
    val features: List<String>,
    val status: String,
    val endpoints: Map<String, String>
)

/**
 * 시스템 정보 응답
 */
data class SystemInfoResponse(
    val system_status: String,
    val supported_expressions: List<String>,
    val cultural_contexts: List<String>,
    val sample_tracks: Int,
    val mediapipe_initialized: Boolean,
    val privacy_features: List<String>,
    val version: String
)

/**
 * 얼굴 분석 응답
 */
data class FaceAnalysisResponse(
    val success: Boolean,
    val faces_detected: Int,
    val face_confidence: Float,
    val has_expression_analysis: Boolean,
    val face_quality_score: Float? = null,
    val processing_time_ms: Int,
    val analyzer_initialized: Boolean,
    val primary_expression: String? = null,
    val expression_confidence: Float? = null,
    val expression_probabilities: Map<String, Float>? = null,
    val emotional_valence: Float? = null,
    val emotional_arousal: Float? = null,
    val error: String? = null
)

// =============== Request 데이터 클래스들 (추후 확장용) ===============

/**
 * 사용자 피드백
 */
data class UserFeedback(
    val sessionId: String,
    val requestId: String? = null,
    val rating: Int, // 1-5점
    val feedbackType: String, // emotion_accuracy, music_relevance, ui_experience
    val comment: String? = null,
    val selectedTracks: List<String>? = null, // 좋았던 곡들
    val rejectedTracks: List<String>? = null, // 별로였던 곡들
    val suggestedImprovements: List<String>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 피드백 응답
 */
data class FeedbackResponse(
    val success: Boolean,
    val message: String,
    val recommendationImprovements: List<String>? = null
)

/**
 * 감정 히스토리 응답
 */
data class EmotionHistoryResponse(
    val userId: String,
    val emotions: List<EmotionHistoryEntry>,
    val trends: EmotionTrends? = null,
    val insights: List<String>? = null
)

/**
 * 감정 히스토리 항목
 */
data class EmotionHistoryEntry(
    val timestamp: Long,
    val emotion: String,
    val intensity: Float,
    val valence: Float,
    val arousal: Float,
    val location: String? = null,
    val weather: String? = null,
    val season: String? = null,
    val timeOfDay: String? = null,
    val associatedMusic: List<String>? = null
)

/**
 * 감정 트렌드 분석
 */
data class EmotionTrends(
    val dominantEmotions: List<String>,
    val emotionalPatterns: Map<String, Float>, // 요일별, 시간별 패턴
    val moodCycles: List<MoodCycle>? = null,
    val seasonalTrends: Map<String, String>? = null
)

/**
 * 기분 주기
 */
data class MoodCycle(
    val pattern: String, // daily, weekly, monthly
    val highPoints: List<String>, // 기분 좋은 시간대
    val lowPoints: List<String>, // 기분 안좋은 시간대
    val averageIntensity: Float
)

/**
 * 사용자 개인화 설정
 */
data class UserPreferences(
    val userId: String,
    val preferredGenres: List<String> = emptyList(),
    val excludedGenres: List<String> = emptyList(),
    val moodSensitivity: Float = 0.5f, // 감정 민감도
    val nostalgiaLevel: Float = 0.5f, // 향수 선호도
    val energyPreference: Float = 0.5f, // 에너지 선호도
    val instrumentalPreference: Float = 0.5f, // 연주곡 선호도
    val languagePreferences: List<String> = listOf("korean", "english"),
    val visualThemePreference: String = "auto",
    val animationIntensity: Float = 0.5f,
    val culturalContext: String = "korean",
    val notificationSettings: NotificationSettings? = null
)

/**
 * 알림 설정
 */
data class NotificationSettings(
    val dailyMoodReminder: Boolean = false,
    val weeklyEmotionSummary: Boolean = false,
    val newMusicRecommendations: Boolean = true,
    val reminderTime: String? = null // HH:mm 형식
)

// =============== Helper Extension Functions ===============

/**
 * String을 RequestBody로 쉽게 변환
 */
fun String.toRequestBody(mediaType: String = "text/plain"): RequestBody {
    return this.toRequestBody(mediaType.toMediaTypeOrNull())
}

/**
 * Int를 RequestBody로 변환
 */
fun Int.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}

/**
 * Float를 RequestBody로 변환
 */
fun Float.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}

/**
 * Boolean을 RequestBody로 변환
 */
fun Boolean.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}

// =============== 간단한 API 빌더 클래스 ===============

/**
 * 실제 서버 API에 맞는 간단한 음악 추천 요청 빌더
 */
class SimpleMusicRecommendationBuilder {
    private var image: MultipartBody.Part? = null
    private var hashtags: String = ""
    private var culturalContext: String = "korean"
    private var maxSongs: Int = 8
    private var facialAnalysisWeight: Float = 0.6f

    fun setImage(imagePart: MultipartBody.Part) = apply {
        this.image = imagePart
    }

    fun setHashtags(hashtags: String) = apply {
        this.hashtags = hashtags
    }

    fun setCulturalContext(context: String) = apply {
        this.culturalContext = context
    }

    fun setMaxSongs(count: Int) = apply {
        this.maxSongs = count
    }

    fun setFacialAnalysisWeight(weight: Float) = apply {
        this.facialAnalysisWeight = weight
    }

    suspend fun execute(apiService: GPTApiService): MusicResponse {
        val imagepart = image ?: throw IllegalStateException("Image is required")

        return apiService.recommendMusic(
            image = imagepart,
            hashtags = hashtags.toRequestBody(),
            culturalContext = culturalContext.toRequestBody(),
            maxSongs = maxSongs.toRequestBody(),
            facialAnalysisWeight = facialAnalysisWeight.toRequestBody()
        )
    }
}

/**
 * 얼굴 분석 요청 빌더
 */
class FaceAnalysisBuilder {
    private var image: MultipartBody.Part? = null

    fun setImage(imagePart: MultipartBody.Part) = apply {
        this.image = imagePart
    }

    suspend fun execute(apiService: GPTApiService): FaceAnalysisResponse {
        val imagePart = image ?: throw IllegalStateException("Image is required")
        return apiService.analyzeFace(imagePart)
    }
}

// =============== Utility Functions ===============

/**
 * 파일을 MultipartBody.Part로 변환하는 유틸리티 함수
 */
object ApiUtils {

    /**
     * 바이트 배열을 이미지 파트로 변환
     */
    fun createImagePart(
        imageBytes: ByteArray,
        filename: String = "image.jpg",
        mimeType: String = "image/jpeg"
    ): MultipartBody.Part {
        val requestBody = RequestBody.create(mimeType.toMediaTypeOrNull(), imageBytes)
        return MultipartBody.Part.createFormData("image", filename, requestBody)
    }

    /**
     * URI를 MultipartBody.Part로 변환하는 확장 함수용 인터페이스
     */
    interface ImagePartConverter {
        fun convertUriToImagePart(uri: android.net.Uri, context: android.content.Context): MultipartBody.Part?
    }
}

// =============== Constants ===============

/**
 * API 관련 상수들
 */
object ApiConstants {

    // Cultural Context
    const val CULTURAL_CONTEXT_KOREAN = "korean"
    const val CULTURAL_CONTEXT_JAPANESE = "japanese"
    const val CULTURAL_CONTEXT_WESTERN = "western"
    const val CULTURAL_CONTEXT_GLOBAL = "global"

    // Max Songs
    const val MIN_SONGS = 1
    const val MAX_SONGS = 10
    const val DEFAULT_SONGS = 8

    // Facial Analysis Weight
    const val MIN_FACIAL_WEIGHT = 0.0f
    const val MAX_FACIAL_WEIGHT = 1.0f
    const val DEFAULT_FACIAL_WEIGHT = 0.6f

    // File Size Limits
    const val MAX_IMAGE_SIZE_MB = 10
    const val MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024

    // Supported Image Types
    val SUPPORTED_IMAGE_TYPES = listOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp"
    )
}

// =============== Error Handling ===============

/**
 * API 에러 타입
 */
sealed class ApiError : Exception() {
    object NetworkError : ApiError()
    object ServerError : ApiError()
    object InvalidRequest : ApiError()
    object ImageTooLarge : ApiError()
    object UnsupportedImageType : ApiError()
    data class HttpError(val code: Int, val errorMessage: String) : ApiError()
    data class UnknownError(val throwable: Throwable) : ApiError()
}

/**
 * API 응답 래퍼
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val error: ApiError) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * API 호출 결과를 안전하게 처리하는 확장 함수
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error(ApiError.NetworkError)
    } catch (e: java.net.ConnectException) {
        ApiResult.Error(ApiError.NetworkError)
    } catch (e: retrofit2.HttpException) {
        when (e.code()) {
            400 -> ApiResult.Error(ApiError.InvalidRequest)
            in 500..599 -> ApiResult.Error(ApiError.ServerError)
            else -> ApiResult.Error(ApiError.HttpError(e.code(), e.message() ?: "Unknown HTTP error"))
        }
    } catch (e: Exception) {
        ApiResult.Error(ApiError.UnknownError(e))
    }
}