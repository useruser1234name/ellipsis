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
     * 감정 기반 음악 추천 API
     * 서버에서 이미지 분석, 감정 분석, 색상 추출, 음악 추천을 모두 처리
     */
    @Multipart
    @POST("recommend-music")
    suspend fun recommendMusic(
        // 필수 파라미터들
        @Part files: List<MultipartBody.Part>, // 이미지 파일들
        @Part("hashtags") hashtags: RequestBody, // 사용자 해시태그

        // 카메라/이미지 정보
        @Part("exif_data") exifData: RequestBody = "{}".toRequestBody("application/json".toMediaTypeOrNull()),
        @Part("camera_info") cameraInfo: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull()),

        // 위치 정보
        @Part("latitude") latitude: RequestBody? = null,
        @Part("longitude") longitude: RequestBody? = null,
        @Part("altitude") altitude: RequestBody? = null,
        @Part("location_source") locationSource: RequestBody = "gps".toRequestBody("text/plain".toMediaTypeOrNull()),

        // 시간 정보
        @Part("datetime") datetime: RequestBody? = null,
        @Part("timezone") timezone: RequestBody? = null,

        // 분석 옵션들
        @Part("analysis_depth") analysisDepth: RequestBody = "standard".toRequestBody("text/plain".toMediaTypeOrNull()), // standard, deep, quick
        @Part("color_analysis") colorAnalysis: RequestBody = "true".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("emotion_analysis") emotionAnalysis: RequestBody = "true".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("object_detection") objectDetection: RequestBody = "true".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("scene_analysis") sceneAnalysis: RequestBody = "true".toRequestBody("text/plain".toMediaTypeOrNull()),

        // 음악 추천 설정
        @Part("max_songs") maxSongs: RequestBody = "8".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("music_genres") musicGenres: RequestBody? = null, // 선호 장르 (쉼표 구분)
        @Part("exclude_genres") excludeGenres: RequestBody? = null, // 제외 장르
        @Part("language_preference") languagePreference: RequestBody = "korean,english".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("instrumental_only") instrumentalOnly: RequestBody = "false".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("mood_intensity") moodIntensity: RequestBody = "0.5".toRequestBody("text/plain".toMediaTypeOrNull()), // 0.0 ~ 1.0

        // 시각적 요소 설정
        @Part("visual_theme") visualTheme: RequestBody = "auto".toRequestBody("text/plain".toMediaTypeOrNull()), // auto, warm, cool, vibrant, muted
        @Part("color_palette_size") colorPaletteSize: RequestBody = "5".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("animation_style") animationStyle: RequestBody = "flowing".toRequestBody("text/plain".toMediaTypeOrNull()), // flowing, pulse, wave, static

        // 문화적 맥락
        @Part("cultural_context") culturalContext: RequestBody = "korean".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("seasonal_preference") seasonalPreference: RequestBody = "auto".toRequestBody("text/plain".toMediaTypeOrNull()), // auto, spring, summer, fall, winter

        // 메타 정보
        @Part("image_count") imageCount: RequestBody = "1".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("total_file_size") totalFileSize: RequestBody = "0".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("app_version") appVersion: RequestBody = "1.0.0".toRequestBody("text/plain".toMediaTypeOrNull()),
        @Part("device_model") deviceModel: RequestBody? = null,
        @Part("android_version") androidVersion: RequestBody? = null,
        @Part("additional_context") additionalContext: RequestBody? = null,

        // 사용자 설정
        @Part("user_id") userId: RequestBody? = null, // 개인화 추천용
        @Part("session_id") sessionId: RequestBody? = null, // 세션 추적용
        @Part("previous_emotions") previousEmotions: RequestBody? = null // 이전 감정 이력 (JSON)
    ): MusicResponse

    /**
     * 간단한 음악 추천 (빠른 처리용)
     */
    @Multipart
    @POST("recommend-music/quick")
    suspend fun recommendMusicQuick(
        @Part files: List<MultipartBody.Part>,
        @Part("hashtags") hashtags: RequestBody,
        @Part("max_songs") maxSongs: RequestBody = "5".toRequestBody("text/plain".toMediaTypeOrNull())
    ): MusicResponse

    /**
     * 음악 추천 상태 확인 (비동기 처리용)
     */
    @GET("recommend-music/status/{requestId}")
    suspend fun getMusicRecommendationStatus(
        @Path("requestId") requestId: String
    ): Response<MusicRecommendationStatus>

    /**
     * 추천 결과 가져오기 (비동기 처리용)
     */
    @GET("recommend-music/result/{requestId}")
    suspend fun getMusicRecommendationResult(
        @Path("requestId") requestId: String
    ): MusicResponse

    /**
     * 사용자 피드백 전송
     */
    @POST("feedback")
    suspend fun sendFeedback(
        @Body feedback: UserFeedback
    ): Response<FeedbackResponse>

    /**
     * 감정 히스토리 조회
     */
    @GET("emotion-history/{userId}")
    suspend fun getEmotionHistory(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 50,
        @Query("days") days: Int = 30
    ): Response<EmotionHistoryResponse>

    /**
     * 개인화 설정 업데이트
     */
    @PUT("user-preferences/{userId}")
    suspend fun updateUserPreferences(
        @Path("userId") userId: String,
        @Body preferences: UserPreferences
    ): Response<Unit>

    /**
     * 시스템 헬스 체크
     */
    @GET("health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
}

// =============== Request/Response 데이터 클래스들 ===============

/**
 * 음악 추천 상태 (비동기 처리용)
 */
data class MusicRecommendationStatus(
    val requestId: String,
    val status: String, // pending, processing, completed, failed
    val progress: Float, // 0.0 ~ 1.0
    val estimatedTimeRemaining: Long? = null, // 예상 남은 시간 (ms)
    val currentStep: String? = null, // 현재 처리 단계
    val message: String? = null
)

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

/**
 * 헬스 체크 응답
 */
data class HealthCheckResponse(
    val status: String, // healthy, degraded, unhealthy
    val version: String,
    val uptime: Long,
    val services: Map<String, ServiceStatus>,
    val lastUpdated: Long
)

/**
 * 서비스 상태
 */
data class ServiceStatus(
    val name: String,
    val status: String, // online, offline, degraded
    val responseTime: Long? = null,
    val lastCheck: Long
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

// =============== API 빌더 클래스 ===============

/**
 * 음악 추천 요청을 쉽게 구성하는 빌더
 */
class MusicRecommendationRequestBuilder {
    private val parameters = mutableMapOf<String, RequestBody>()
    private var files: List<MultipartBody.Part> = emptyList()
    private var hashtags: String = ""

    fun setFiles(files: List<MultipartBody.Part>) = apply {
        this.files = files
    }

    fun setHashtags(hashtags: String) = apply {
        this.hashtags = hashtags
    }

    fun setAnalysisDepth(depth: String) = apply {
        parameters["analysis_depth"] = depth.toRequestBody()
    }

    fun setMoodIntensity(intensity: Float) = apply {
        parameters["mood_intensity"] = intensity.toRequestBody()
    }

    fun setMaxSongs(count: Int) = apply {
        parameters["max_songs"] = count.toRequestBody()
    }

    fun setVisualTheme(theme: String) = apply {
        parameters["visual_theme"] = theme.toRequestBody()
    }

    fun setCulturalContext(context: String) = apply {
        parameters["cultural_context"] = context.toRequestBody()
    }

    fun enableColorAnalysis(enable: Boolean = true) = apply {
        parameters["color_analysis"] = enable.toRequestBody()
    }

    fun enableEmotionAnalysis(enable: Boolean = true) = apply {
        parameters["emotion_analysis"] = enable.toRequestBody()
    }

    fun setLocation(latitude: Double?, longitude: Double?) = apply {
        latitude?.let { parameters["latitude"] = it.toString().toRequestBody() }
        longitude?.let { parameters["longitude"] = it.toString().toRequestBody() }
    }

    fun setDateTime(dateTime: String?) = apply {
        dateTime?.let { parameters["datetime"] = it.toRequestBody() }
    }

    fun setExifData(exifJson: String) = apply {
        parameters["exif_data"] = exifJson.toRequestBody("application/json")
    }

    suspend fun execute(apiService: GPTApiService): MusicResponse {
        return apiService.recommendMusic(
            files = files,
            hashtags = hashtags.toRequestBody(),
            exifData = parameters["exif_data"] ?: "{}".toRequestBody("application/json"),
            analysisDepth = parameters["analysis_depth"] ?: "standard".toRequestBody(),
            colorAnalysis = parameters["color_analysis"] ?: true.toRequestBody(),
            emotionAnalysis = parameters["emotion_analysis"] ?: true.toRequestBody(),
            maxSongs = parameters["max_songs"] ?: 8.toRequestBody(),
            visualTheme = parameters["visual_theme"] ?: "auto".toRequestBody(),
            culturalContext = parameters["cultural_context"] ?: "korean".toRequestBody(),
            moodIntensity = parameters["mood_intensity"] ?: 0.5f.toRequestBody(),
            latitude = parameters["latitude"],
            longitude = parameters["longitude"],
            datetime = parameters["datetime"]
        )
    }
}