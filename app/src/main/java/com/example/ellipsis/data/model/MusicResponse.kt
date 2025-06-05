package com.example.ellipsis.data.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

/**
 * 서버에서 받는 음악 추천 응답
 */
data class MusicResponse(
    @SerializedName("emotion")
    val emotion: String, // 주요 감정 (예: "nostalgia", "joy", "calm")

    @SerializedName("description")
    val description: String, // 감정에 대한 상세 설명

    @SerializedName("tracks")
    val tracks: List<Track>, // 추천 음악 리스트

    @SerializedName("metadata")
    val metadata: Metadata? = null, // 추가 메타데이터

    @SerializedName("emotion_analysis")
    val emotionAnalysis: EmotionAnalysisResponse? = null, // 고급 감정 분석

    @SerializedName("visual_elements")
    val visualElements: VisualElementsResponse? = null, // 시각적 요소 추천

    @SerializedName("processing_info")
    val processingInfo: ProcessingInfo? = null // 처리 정보
)

/**
 * 음악 트랙 정보
 */
data class Track(
    @SerializedName("title")
    val title: String, // 곡 제목

    @SerializedName("artist")
    val artist: String, // 아티스트명

    @SerializedName("album")
    val album: String? = null, // 앨범명

    @SerializedName("album_image_url")
    val album_image_url: String? = null, // 앨범 이미지 URL

    @SerializedName("duration")
    val duration: String? = null, // 재생 시간 (예: "3:45")

    @SerializedName("release_year")
    val releaseYear: Int? = null, // 발매년도

    @SerializedName("genre")
    val genre: String? = null, // 장르

    @SerializedName("mood_tags")
    val moodTags: List<String>? = null, // 감정 태그들

    @SerializedName("spotify_url")
    val spotifyUrl: String? = null, // 스포티파이 링크

    @SerializedName("apple_music_url")
    val appleMusicUrl: String? = null, // 애플뮤직 링크

    @SerializedName("youtube_url")
    val youtubeUrl: String? = null, // 유튜브 링크

    @SerializedName("preview_url")
    val previewUrl: String? = null, // 미리듣기 URL

    @SerializedName("recommendation_score")
    val recommendationScore: Float? = null, // 추천 점수 (0.0 ~ 1.0)

    @SerializedName("musical_elements")
    val musicalElements: MusicalElements? = null // 음악적 특성
)

/**
 * 음악의 음향적/감정적 특성
 */
data class MusicalElements(
    @SerializedName("tempo")
    val tempo: Int? = null, // BPM

    @SerializedName("key")
    val key: String? = null, // 조성 (예: "C Major", "A Minor")

    @SerializedName("energy")
    val energy: Float? = null, // 에너지 레벨 (0.0 ~ 1.0)

    @SerializedName("valence")
    val valence: Float? = null, // 긍정도 (0.0 ~ 1.0)

    @SerializedName("danceability")
    val danceability: Float? = null, // 춤추기 좋은 정도

    @SerializedName("acousticness")
    val acousticness: Float? = null, // 어쿠스틱 정도

    @SerializedName("instrumentalness")
    val instrumentalness: Float? = null, // 연주곡 정도

    @SerializedName("loudness")
    val loudness: Float? = null, // 음량 (dB)

    @SerializedName("note_position")
    val notePosition: NotePositionResponse? = null // 오선지 위치 정보
)

/**
 * 오선지에서의 음표 위치 (서버 응답)
 */
data class NotePositionResponse(
    @SerializedName("staff_line")
    val staffLine: Int, // 1-5번 오선

    @SerializedName("x_position")
    val xPosition: Float, // 가로 위치 (0.0 ~ 1.0)

    @SerializedName("note_type")
    val noteType: String = "quarter", // 음표 종류

    @SerializedName("stem_direction")
    val stemDirection: String = "up" // 음표 기둥 방향
)

/**
 * 기존 메타데이터 (호환성 유지)
 */
data class Metadata(
    @SerializedName("emotion_keywords")
    val emotion_keywords: List<String>? = null, // 감정 키워드들

    @SerializedName("objects_detected")
    val objects_detected: List<String>? = null, // 인식된 객체들

    @SerializedName("location")
    val location: String? = null, // 위치 정보

    @SerializedName("datetime")
    val datetime: String? = null, // 촬영 시간

    @SerializedName("processed_images")
    val processed_images: Int? = null, // 처리된 이미지 수

    @SerializedName("dominant_colors")
    val dominantColors: List<String>? = null, // 주요 색상들 (hex)

    @SerializedName("image_analysis")
    val imageAnalysis: ImageAnalysisResponse? = null // 이미지 분석 결과
)

/**
 * 고급 감정 분석 응답
 */
data class EmotionAnalysisResponse(
    @SerializedName("primary_emotion")
    val primaryEmotion: String, // 주요 감정

    @SerializedName("secondary_emotions")
    val secondaryEmotions: List<String> = emptyList(), // 보조 감정들

    @SerializedName("intensity")
    val intensity: Float, // 감정 강도 (0.0 ~ 1.0)

    @SerializedName("valence")
    val valence: Float, // 긍정/부정 정도 (-1.0 ~ 1.0)

    @SerializedName("arousal")
    val arousal: Float, // 각성 정도 (0.0 ~ 1.0)

    @SerializedName("confidence")
    val confidence: Float, // 분석 신뢰도 (0.0 ~ 1.0)

    @SerializedName("emotion_timeline")
    val emotionTimeline: List<EmotionPoint>? = null, // 감정 변화 타임라인

    @SerializedName("cultural_context")
    val culturalContext: CulturalContext? = null, // 문화적 맥락

    @SerializedName("season_mood")
    val seasonMood: String? = null, // 계절적 감정 (봄, 여름, 가을, 겨울)

    @SerializedName("time_of_day_mood")
    val timeOfDayMood: String? = null // 시간대별 감정 (새벽, 아침, 낮, 저녁, 밤)
)

/**
 * 감정 변화 포인트
 */
data class EmotionPoint(
    @SerializedName("timestamp")
    val timestamp: Float, // 0.0 ~ 1.0 (곡의 진행 정도)

    @SerializedName("emotion")
    val emotion: String,

    @SerializedName("intensity")
    val intensity: Float
)

/**
 * 문화적 맥락 정보
 */
data class CulturalContext(
    @SerializedName("korean_emotion_term")
    val koreanEmotionTerm: String? = null, // 한국어 감정 표현 (예: "그리움", "한")

    @SerializedName("related_concepts")
    val relatedConcepts: List<String>? = null, // 관련 개념들

    @SerializedName("traditional_elements")
    val traditionalElements: List<String>? = null, // 전통적 요소들

    @SerializedName("modern_interpretation")
    val modernInterpretation: String? = null // 현대적 해석
)

/**
 * 시각적 요소 추천
 */
data class VisualElementsResponse(
    @SerializedName("color_palette")
    val colorPalette: List<String>, // 추천 색상 팔레트 (hex)

    @SerializedName("gradient_suggestions")
    val gradientSuggestions: List<GradientSuggestion>? = null, // 그라데이션 제안

    @SerializedName("animation_style")
    val animationStyle: String? = null, // 애니메이션 스타일 (예: "flowing", "pulse", "wave")

    @SerializedName("visual_mood")
    val visualMood: String? = null, // 시각적 분위기 (예: "dreamy", "energetic", "calm")

    @SerializedName("typography_suggestion")
    val typographySuggestion: TypographySuggestion? = null, // 타이포그래피 제안

    @SerializedName("particle_effects")
    val particleEffects: List<ParticleEffect>? = null // 파티클 효과들
)

/**
 * 그라데이션 제안
 */
data class GradientSuggestion(
    @SerializedName("colors")
    val colors: List<String>, // 색상들 (hex)

    @SerializedName("direction")
    val direction: String, // 방향 (예: "vertical", "horizontal", "radial")

    @SerializedName("stops")
    val stops: List<Float>? = null // 색상 정지점들
)

/**
 * 타이포그래피 제안
 */
data class TypographySuggestion(
    @SerializedName("font_weight")
    val fontWeight: String? = null, // 글꼴 두께

    @SerializedName("letter_spacing")
    val letterSpacing: Float? = null, // 자간

    @SerializedName("line_height")
    val lineHeight: Float? = null, // 줄간격

    @SerializedName("style")
    val style: String? = null // 스타일 (예: "elegant", "modern", "handwritten")
)

/**
 * 파티클 효과
 */
data class ParticleEffect(
    @SerializedName("type")
    val type: String, // 효과 타입 (예: "snow", "sparkle", "floating_notes")

    @SerializedName("intensity")
    val intensity: Float, // 강도 (0.0 ~ 1.0)

    @SerializedName("color")
    val color: String? = null, // 색상 (hex)

    @SerializedName("speed")
    val speed: Float? = null // 속도
)

/**
 * 이미지 분석 결과
 */
data class ImageAnalysisResponse(
    @SerializedName("brightness")
    val brightness: Float? = null, // 밝기 (0.0 ~ 1.0)

    @SerializedName("contrast")
    val contrast: Float? = null, // 대비 (0.0 ~ 1.0)

    @SerializedName("saturation")
    val saturation: Float? = null, // 채도 (0.0 ~ 1.0)

    @SerializedName("warmth")
    val warmth: Float? = null, // 색온도 (-1.0 ~ 1.0, -는 차가움, +는 따뜻함)

    @SerializedName("composition_score")
    val compositionScore: Float? = null, // 구도 점수 (0.0 ~ 1.0)

    @SerializedName("focal_points")
    val focalPoints: List<FocalPoint>? = null, // 주요 관심 지점들

    @SerializedName("scene_type")
    val sceneType: String? = null, // 장면 타입 (예: "landscape", "portrait", "urban")

    @SerializedName("weather_mood")
    val weatherMood: String? = null, // 날씨 분위기 (예: "sunny", "cloudy", "rainy")

    @SerializedName("lighting_condition")
    val lightingCondition: String? = null // 조명 상태 (예: "golden_hour", "blue_hour", "indoor")
)

/**
 * 이미지의 주요 관심 지점
 */
data class FocalPoint(
    @SerializedName("x")
    val x: Float, // X 좌표 (0.0 ~ 1.0)

    @SerializedName("y")
    val y: Float, // Y 좌표 (0.0 ~ 1.0)

    @SerializedName("importance")
    val importance: Float, // 중요도 (0.0 ~ 1.0)

    @SerializedName("description")
    val description: String? = null // 설명
)

/**
 * 처리 정보
 */
data class ProcessingInfo(
    @SerializedName("processing_time_ms")
    val processingTimeMs: Long, // 처리 시간 (밀리초)

    @SerializedName("model_version")
    val modelVersion: String, // 사용된 모델 버전

    @SerializedName("api_version")
    val apiVersion: String, // API 버전

    @SerializedName("image_quality_score")
    val imageQualityScore: Float? = null, // 이미지 품질 점수

    @SerializedName("recommendation_confidence")
    val recommendationConfidence: Float? = null, // 추천 신뢰도

    @SerializedName("error_messages")
    val errorMessages: List<String>? = null, // 경고/오류 메시지들

    @SerializedName("debug_info")
    val debugInfo: Map<String, Any>? = null // 디버그 정보
)

// =============== Extension Functions ===============

/**
 * Track을 TrackInfo(앱 내부 모델)로 변환
 */
fun Track.toTrackInfo(index: Int = 0, totalTracks: Int = 1): com.example.ellipsis.presentation.main.TrackInfo {
    return com.example.ellipsis.presentation.main.TrackInfo(
        title = this.title,
        artist = this.artist,
        albumImageUrl = this.album_image_url,
        duration = this.duration,
        previewUrl = this.previewUrl,
        spotifyUrl = this.spotifyUrl,
        appleMusicUrl = this.appleMusicUrl,
        notePosition = this.musicalElements?.notePosition?.toNotePosition(index, totalTracks)
    )
}

/**
 * NotePositionResponse를 NotePosition으로 변환
 */
fun NotePositionResponse.toNotePosition(index: Int, totalTracks: Int): com.example.ellipsis.presentation.main.NotePosition {
    val noteType = when (this.noteType.lowercase()) {
        "whole" -> com.example.ellipsis.presentation.main.NoteType.WHOLE
        "half" -> com.example.ellipsis.presentation.main.NoteType.HALF
        "quarter" -> com.example.ellipsis.presentation.main.NoteType.QUARTER
        "eighth" -> com.example.ellipsis.presentation.main.NoteType.EIGHTH
        "sixteenth" -> com.example.ellipsis.presentation.main.NoteType.SIXTEENTH
        else -> com.example.ellipsis.presentation.main.NoteType.QUARTER
    }

    return com.example.ellipsis.presentation.main.NotePosition(
        staffLine = this.staffLine.coerceIn(1, 5),
        xPosition = this.xPosition.coerceIn(0f, 1f),
        noteType = noteType
    )
}

/**
 * 색상 팔레트를 Color 리스트로 변환
 */
fun VisualElementsResponse.getColorPalette(): List<Color> {
    return this.colorPalette.mapNotNull { hexColor ->
        try {
            val cleanHex = hexColor.removePrefix("#")
            val colorInt = if (cleanHex.length == 6) {
                android.graphics.Color.parseColor("#$cleanHex")
            } else {
                null
            }
            colorInt?.let { Color(it) }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 감정 분석 결과를 앱 내부 모델로 변환
 */
fun EmotionAnalysisResponse.toEmotionAnalysis(): com.example.ellipsis.presentation.main.EmotionAnalysis {
    return com.example.ellipsis.presentation.main.EmotionAnalysis(
        primaryEmotion = this.primaryEmotion,
        secondaryEmotions = this.secondaryEmotions,
        intensity = this.intensity,
        valence = this.valence,
        arousal = this.arousal,
        description = "${this.primaryEmotion} 감정이 ${(this.intensity * 100).toInt()}% 강도로 감지되었습니다.",
        colorSuggestions = emptyList(), // 별도 처리 필요
        musicGenreSuggestions = emptyList() // 별도 처리 필요
    )
}