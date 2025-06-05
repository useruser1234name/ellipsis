package com.example.ellipsis.presentation.main

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 메인 화면의 UI 상태를 관리하는 데이터 클래스
 *
 * 주요 기능:
 * - 음악 추천 프로세스의 모든 상태 관리
 * - EXIF 메타데이터 정보 표시
 * - 진행률 및 로딩 상태 추적
 * - 에러 처리 및 사용자 피드백
 * - 성능 최적화를 위한 불변성 보장
 */
@Stable
@Immutable
data class MainUiState(
    // === 기본 이미지 정보 ===
    val bitmap: Bitmap? = null,
    val detectedObjects: List<String> = emptyList(),
    val hashtags: String = "#그리움 #평온",

    // === 사진 메타데이터 ===
    val photoDate: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String = "",
    val exifJson: String = "{}",

    // === 처리 상태 관리 ===
    /**
     * 현재 처리 중인지 여부
     */
    val isLoading: Boolean = false,

    /**
     * 처리 진행률 (0.0 ~ 1.0)
     */
    val processingProgress: Float = 0f,

    /**
     * 로딩 중 표시할 메시지
     */
    val loadingMessage: String? = null,

    /**
     * 에러 메시지 (null이면 에러 없음)
     */
    val errorMessage: String? = null,

    /**
     * 성공 메시지
     */
    val successMessage: String? = null,

    /**
     * 현재 처리 단계 (디버깅 및 상세 진행률 표시용)
     */
    val currentStep: ProcessingStep = ProcessingStep.IDLE,

    /**
     * 처리 시작 시간 (성능 측정용)
     */
    val processingStartTime: Long? = null,

    // === 음악 추천 결과 ===
    /**
     * 추천된 음악 목록 (아티스트 - 곡명 형태)
     */
    val recommendedSongs: List<String> = emptyList(),

    /**
     * 서버에서 분석된 감정
     */
    val emotion: String = "",

    /**
     * 감정에 대한 설명
     */
    val emotionDescription: String = "",

    /**
     * 감정 키워드들
     */
    val emotionKeywords: List<String> = emptyList(),

    /**
     * 추출된 감정 키워드들
     */
    val extractedKeywords: List<String> = emptyList(),

    /**
     * 감정 분석 결과 설명문
     */
    val emotionalDescription: String = "",

    /**
     * 주요 감정 (대표 감정)
     */
    val mood: String = "",

    /**
     * 추천 신뢰도 (0.0 ~ 1.0)
     */
    val confidence: Float = 0f,

    // === UI 색상 및 테마 ===
    val dominantColors: List<Color> = emptyList(), // 사진에서 추출된 주요 색상들
    val primaryColor: Color = Color(0xFF6366F1), // 메인 테마 색상
    val secondaryColor: Color = Color(0xFF8B5CF6), // 보조 색상
    val backgroundGradient: List<Color> = emptyList(), // 배경 그라데이션

    // === 이미지 분석 결과 ===

    /**
     * 얼굴 표정 분석 결과
     */
    val faceExpression: String = "",

    /**
     * 색상 분석 결과
     */
    val colorAnalysis: ColorAnalysisResult? = null,

    /**
     * 장면 분석 결과 (실내/실외, 시간대 등)
     */
    val sceneAnalysis: SceneAnalysisResult? = null,

    // === EXIF 메타데이터 정보 ===
    /**
     * 카메라 정보 요약
     */
    val cameraInfo: String = "",

    /**
     * 촬영 설정 정보 (ISO, 조리개, 셔터스피드 등)
     */
    val shootingSettings: String = "",

    /**
     * 처리된 이미지 수
     */
    val imageCount: Int = 0,

    /**
     * 메타데이터 품질 점수 (0.0 ~ 1.0)
     */
    val metadataQuality: Float = 0f,

    /**
     * EXIF 정보 풍부함 등급
     */
    val metadataGrade: String = "",

    // === 메모리 다이어리 관련 ===
    val savedMemories: List<MemoryEntry> = emptyList(), // 저장된 기억들
    val currentMemoryId: String? = null, // 현재 선택된 기억 ID

    // === UI 상태 ===
    val isAnalyzing: Boolean = false, // 사진 분석 중
    val isMusicLoading: Boolean = false, // 음악 추천 로딩 중
    val showMemoryDiary: Boolean = false,
    val showMusicPlayer: Boolean = false,

    /**
     * 사용자가 입력한 해시태그
     */
    val userHashtags: String = "",

    /**
     * 현재 선택된 이미지 인덱스 (페이저용)
     */
    val selectedImageIndex: Int = 0,

    /**
     * 추천 결과 확장 여부
     */
    val isResultExpanded: Boolean = false,

    /**
     * 메타데이터 상세 보기 여부
     */
    val isMetadataExpanded: Boolean = false,

    /**
     * 설정 패널 표시 여부
     */
    val isSettingsPanelVisible: Boolean = false,

    // === 애니메이션 상태 ===
    val noteAnimationProgress: Float = 0f, // 음표 애니메이션 진행도
    val connectingLineProgress: Float = 0f, // 선 연결 애니메이션 진행도
    val colorTransitionProgress: Float = 0f, // 색상 전환 애니메이션

    // === 앱 상태 정보 ===
    /**
     * 서버 연결 상태
     */
    val serverConnectionStatus: ServerStatus = ServerStatus.UNKNOWN,

    /**
     * 마지막 업데이트 시간
     */
    val lastUpdateTime: Long = System.currentTimeMillis(),

    /**
     * 총 처리 시간 (밀리초)
     */
    val totalProcessingTime: Long? = null,

    /**
     * 세션 통계 정보
     */
    val sessionStats: SessionStats = SessionStats(),

    // === 서버 응답 상세 정보 ===
    val serverMetadata: ServerMetadata? = null
) {

    // === 계산된 속성들 ===

    /**
     * 추천 결과가 있는지 확인
     */
    fun hasRecommendations(): Boolean = recommendedSongs.isNotEmpty()

    /**
     * 에러 상태인지 확인
     */
    fun hasError(): Boolean = errorMessage != null

    /**
     * 위치 정보가 있는지 확인
     */
    fun hasLocationInfo(): Boolean = latitude != null && longitude != null

    /**
     * 메타데이터 정보가 풍부한지 확인
     */
    fun hasRichMetadata(): Boolean = metadataQuality > 0.6f

    /**
     * 처리 완료 여부 확인
     */
    fun isProcessingComplete(): Boolean = !isLoading && hasRecommendations() && !hasError()

    /**
     * 진행률 백분율 반환
     */
    fun getProgressPercentage(): Int = (processingProgress * 100).toInt()

    /**
     * 예상 남은 시간 계산 (초)
     */
    fun getEstimatedRemainingTime(): Int? {
        if (!isLoading || processingStartTime == null || processingProgress <= 0f) {
            return null
        }

        val elapsedTime = System.currentTimeMillis() - processingStartTime
        val totalEstimatedTime = elapsedTime / processingProgress
        val remainingTime = totalEstimatedTime - elapsedTime

        return (remainingTime / 1000).toInt().coerceAtLeast(0)
    }

    /**
     * 신뢰도 등급 반환
     */
    fun getConfidenceGrade(): String {
        return when {
            confidence >= 0.9f -> "매우 높음"
            confidence >= 0.7f -> "높음"
            confidence >= 0.5f -> "보통"
            confidence >= 0.3f -> "낮음"
            else -> "매우 낮음"
        }
    }

    /**
     * 메타데이터 요약 정보 반환
     */
    fun getMetadataSummary(): String {
        val parts = mutableListOf<String>()

        if (imageCount > 0) {
            parts.add("${imageCount}장")
        }

        if (photoDate.isNotBlank()) {
            parts.add("촬영시간")
        }

        if (hasLocationInfo()) {
            parts.add("위치정보")
        }

        if (cameraInfo.isNotBlank() && cameraInfo != "카메라 정보 없음") {
            parts.add("카메라정보")
        }

        if (shootingSettings.isNotBlank() && shootingSettings != "촬영 정보 없음") {
            parts.add("촬영설정")
        }

        return if (parts.isEmpty()) {
            "기본 정보만 사용"
        } else {
            parts.joinToString(", ") + " 포함"
        }
    }

    /**
     * 추천 품질 평가
     */
    fun getRecommendationQuality(): RecommendationQuality {
        return when {
            !hasRecommendations() -> RecommendationQuality.NONE
            confidence >= 0.8f && extractedKeywords.size >= 3 -> RecommendationQuality.EXCELLENT
            confidence >= 0.6f && extractedKeywords.size >= 2 -> RecommendationQuality.GOOD
            confidence >= 0.4f -> RecommendationQuality.FAIR
            else -> RecommendationQuality.POOR
        }
    }

    /**
     * 상태 안정성 확인 (Compose 리컴포지션 최적화용)
     */
    fun isStable(): Boolean {
        return !isLoading && errorMessage == null
    }

    companion object {
        /**
         * 초기 상태 생성
         */
        fun initial(): MainUiState = MainUiState()

        /**
         * 로딩 상태 생성
         */
        fun loading(
            progress: Float = 0f,
            message: String? = null,
            step: ProcessingStep = ProcessingStep.PREPARING
        ): MainUiState = MainUiState(
            isLoading = true,
            processingProgress = progress,
            loadingMessage = message,
            currentStep = step,
            processingStartTime = System.currentTimeMillis()
        )

        /**
         * 에러 상태 생성
         */
        fun error(message: String): MainUiState = MainUiState(
            errorMessage = message,
            isLoading = false,
            processingProgress = 0f
        )

        /**
         * 성공 상태 생성
         */
        fun success(
            songs: List<String>,
            keywords: List<String>,
            description: String,
            mood: String,
            confidence: Float
        ): MainUiState = MainUiState(
            recommendedSongs = songs,
            extractedKeywords = keywords,
            emotionalDescription = description,
            mood = mood,
            confidence = confidence,
            isLoading = false,
            processingProgress = 1.0f,
            currentStep = ProcessingStep.COMPLETED
        )
    }
}

/**
 * 저장된 기억 항목
 * 각 사진-음악 쌍을 하나의 기억으로 저장
 */
data class MemoryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val photoUri: String,
    val thumbnailBitmap: Bitmap?,
    val songs: List<TrackInfo>,
    val emotion: String,
    val emotionDescription: String,
    val hashtags: List<String>,
    val location: String,
    val photoDate: String,
    val dominantColor: Color,
    val notes: String = "", // 사용자가 추가한 메모
    val isFavorite: Boolean = false
)

/**
 * 음악 트랙 정보
 */
data class TrackInfo(
    val title: String,
    val artist: String,
    val albumImageUrl: String? = null,
    val duration: String? = null,
    val previewUrl: String? = null, // 미리듣기 URL (추후 기능)
    val spotifyUrl: String? = null, // 스포티파이 링크 (추후 기능)
    val appleMusicUrl: String? = null, // 애플 뮤직 링크 (추후 기능)
    val notePosition: NotePosition? = null // 오선지에서의 위치
)

/**
 * 오선지에서 음표의 위치 정보
 */
data class NotePosition(
    val staffLine: Int, // 1-5번 오선
    val xPosition: Float, // 가로 위치 (0.0 ~ 1.0)
    val noteType: NoteType = NoteType.QUARTER // 음표 종류
)

/**
 * 음표 종류 (추후 다양한 음표 표현을 위해)
 */
enum class NoteType {
    WHOLE,      // 온음표
    HALF,       // 2분음표
    QUARTER,    // 4분음표
    EIGHTH,     // 8분음표
    SIXTEENTH   // 16분음표
}

/**
 * 서버에서 받은 메타데이터
 */
data class ServerMetadata(
    val emotionKeywords: List<String> = emptyList(),
    val objectsDetected: List<String> = emptyList(),
    val location: String? = null,
    val datetime: String? = null,
    val processedImages: Int = 0,
    val confidence: Float = 0f, // 감정 분석 신뢰도
    val processingTime: Long = 0L, // 처리 시간 (ms)
    val apiVersion: String = "1.0"
)

/**
 * 처리 단계를 나타내는 열거형
 */
enum class ProcessingStep(val displayName: String, val description: String) {
    IDLE("대기", "처리 대기 중"),
    PREPARING("준비", "이미지 분석 준비 중"),
    EXTRACTING_METADATA("메타데이터 추출", "EXIF 정보를 분석하고 있어요"),
    PROCESSING_IMAGES("이미지 처리", "이미지를 최적화하고 있어요"),
    ANALYZING_EMOTIONS("감정 분석", "AI가 감정을 분석하고 있어요"),
    GENERATING_MUSIC("음악 생성", "감정에 맞는 음악을 찾고 있어요"),
    COMPLETED("완료", "음악 추천이 완료되었어요"),
    ERROR("오류", "처리 중 오류가 발생했어요");

    /**
     * 다음 단계로 이동
     */
    fun next(): ProcessingStep {
        return when (this) {
            IDLE -> PREPARING
            PREPARING -> EXTRACTING_METADATA
            EXTRACTING_METADATA -> PROCESSING_IMAGES
            PROCESSING_IMAGES -> ANALYZING_EMOTIONS
            ANALYZING_EMOTIONS -> GENERATING_MUSIC
            GENERATING_MUSIC -> COMPLETED
            COMPLETED -> COMPLETED
            ERROR -> ERROR
        }
    }

    /**
     * 진행률 반환 (0.0 ~ 1.0)
     */
    fun getProgress(): Float {
        return when (this) {
            IDLE -> 0.0f
            PREPARING -> 0.1f
            EXTRACTING_METADATA -> 0.3f
            PROCESSING_IMAGES -> 0.5f
            ANALYZING_EMOTIONS -> 0.7f
            GENERATING_MUSIC -> 0.9f
            COMPLETED -> 1.0f
            ERROR -> 0.0f
        }
    }
}

/**
 * 서버 연결 상태
 */
enum class ServerStatus(val displayName: String) {
    UNKNOWN("알 수 없음"),
    CONNECTING("연결 중"),
    CONNECTED("연결됨"),
    DISCONNECTED("연결 끊김"),
    ERROR("연결 오류");

    fun isHealthy(): Boolean = this == CONNECTED
}

/**
 * 추천 품질 등급
 */
enum class RecommendationQuality(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    NONE("없음", androidx.compose.ui.graphics.Color.Gray),
    POOR("낮음", androidx.compose.ui.graphics.Color.Red),
    FAIR("보통", androidx.compose.ui.graphics.Color(0xFFFF9800)), // Orange
    GOOD("좋음", androidx.compose.ui.graphics.Color(0xFF4CAF50)), // Green
    EXCELLENT("우수", androidx.compose.ui.graphics.Color(0xFF2196F3)) // Blue
}

/**
 * 감정 분석 결과
 */
@Immutable
data class EmotionAnalysis(
    val primaryEmotion: String,              // 주요 감정
    val secondaryEmotions: List<String>,     // 보조 감정들
    val intensity: Float,                    // 감정 강도 (0.0 ~ 1.0)
    val valence: Float,                      // 긍정/부정 정도 (-1.0 ~ 1.0)
    val arousal: Float,                      // 각성 정도 (0.0 ~ 1.0)
    val description: String,                 // 감정 설명
    val colorSuggestions: List<Color>,       // 추천 색상들
    val musicGenreSuggestions: List<String>  // 추천 음악 장르들
)

/**
 * 색상 분석 결과
 */
@Immutable
data class ColorAnalysisResult(
    val dominantColors: List<String> = emptyList(),
    val averageBrightness: Float = 0f,
    val averageSaturation: Float = 0f,
    val colorTemperature: String = "",
    val isVibrant: Boolean = false,
    val isMonochrome: Boolean = false
) {
    fun getColorDescription(): String {
        return buildString {
            when {
                isMonochrome -> append("모노톤의")
                isVibrant -> append("생동감 넘치는")
                averageSaturation > 0.7f -> append("선명한")
                averageSaturation < 0.3f -> append("차분한")
                else -> append("균형잡힌")
            }

            append(" 색감")

            if (dominantColors.isNotEmpty()) {
                append(" (${dominantColors.take(3).joinToString(", ")})")
            }
        }
    }
}

/**
 * 장면 분석 결과
 */
@Immutable
data class SceneAnalysisResult(
    val sceneType: String = "", // 실내, 실외, 자연, 도시 등
    val timeOfDay: String = "", // 아침, 낮, 저녁, 밤
    val weather: String = "", // 맑음, 흐림, 비 등
    val season: String = "", // 봄, 여름, 가을, 겨울
    val atmosphere: String = "" // 밝은, 어두운, 따뜻한, 차가운 등
) {
    fun getSceneDescription(): String {
        val parts = listOfNotNull(
            timeOfDay.takeIf { it.isNotBlank() },
            weather.takeIf { it.isNotBlank() },
            sceneType.takeIf { it.isNotBlank() },
            atmosphere.takeIf { it.isNotBlank() }
        )

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ") + "의 장면"
        } else {
            "일반적인 장면"
        }
    }
}

/**
 * 세션 통계 정보
 */
@Immutable
data class SessionStats(
    val totalRequests: Int = 0,
    val successfulRequests: Int = 0,
    val failedRequests: Int = 0,
    val averageProcessingTime: Long = 0L,
    val stateChanges: List<String> = emptyList()
) {
    fun getSuccessRate(): Int {
        return if (totalRequests > 0) {
            (successfulRequests * 100 / totalRequests)
        } else 0
    }

    fun addSuccess(): SessionStats {
        return copy(
            totalRequests = totalRequests + 1,
            successfulRequests = successfulRequests + 1
        )
    }

    fun addFailure(): SessionStats {
        return copy(
            totalRequests = totalRequests + 1,
            failedRequests = failedRequests + 1
        )
    }

    fun addStateChange(action: String): SessionStats {
        return copy(
            stateChanges = (stateChanges + action).takeLast(20) // 최근 20개만 유지
        )
    }

    fun updateProcessingTime(time: Long): SessionStats {
        val newAverage = if (successfulRequests > 0) {
            (averageProcessingTime * (successfulRequests - 1) + time) / successfulRequests
        } else time

        return copy(averageProcessingTime = newAverage)
    }
}

/**
 * UI 상태 변경을 위한 확장 함수들
 */

/**
 * 로딩 상태로 변경
 */
fun MainUiState.toLoading(
    progress: Float = 0f,
    message: String? = null,
    step: ProcessingStep = ProcessingStep.PREPARING
): MainUiState {
    return copy(
        isLoading = true,
        processingProgress = progress,
        loadingMessage = message,
        currentStep = step,
        errorMessage = null,
        processingStartTime = processingStartTime ?: System.currentTimeMillis()
    )
}

/**
 * 에러 상태로 변경
 */
fun MainUiState.toError(message: String): MainUiState {
    return copy(
        errorMessage = message,
        isLoading = false,
        currentStep = ProcessingStep.ERROR,
        sessionStats = sessionStats.addFailure()
    )
}

/**
 * 완료 상태로 변경
 */
fun MainUiState.toCompleted(processingTime: Long? = null): MainUiState {
    val finalTime = processingTime ?: processingStartTime?.let {
        System.currentTimeMillis() - it
    }

    return copy(
        isLoading = false,
        processingProgress = 1.0f,
        currentStep = ProcessingStep.COMPLETED,
        totalProcessingTime = finalTime,
        sessionStats = sessionStats.addSuccess().let { stats ->
            finalTime?.let { stats.updateProcessingTime(it) } ?: stats
        }
    )
}

/**
 * 진행률 업데이트
 */
fun MainUiState.updateProgress(
    progress: Float,
    message: String? = null,
    step: ProcessingStep? = null
): MainUiState {
    return copy(
        processingProgress = progress.coerceIn(0f, 1f),
        loadingMessage = message ?: loadingMessage,
        currentStep = step ?: currentStep
    )
}