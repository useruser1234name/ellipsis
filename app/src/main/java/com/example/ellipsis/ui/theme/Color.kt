package com.example.ellipsis.ui.theme

import androidx.compose.ui.graphics.Color

// === ellipsis 브랜드 색상 ===

// 주요 브랜드 색상
val EllipsisPrimary = Color(0xFF6B46C1)      // 보라색 - 주요 브랜드 컬러
val EllipsisSecondary = Color(0xFFEC4899)    // 핑크색 - 보조 브랜드 컬러
val EllipsisAccent = Color(0xFF8B5CF6)       // 라벤더 - 강조 색상

// 중성 색상 팔레트
val EllipsisBackground = Color(0xFFFAF5FF)   // 매우 연한 보라 배경
val EllipsisSurface = Color(0xFFFFFFFF)      // 순백색 표면
val EllipsisOnPrimary = Color(0xFFFFFFFF)    // 주요 색상 위의 텍스트
val EllipsisOnSecondary = Color(0xFFFFFFFF)  // 보조 색상 위의 텍스트
val EllipsisOnBackground = Color(0xFF1A1A1A) // 배경 위의 텍스트
val EllipsisOnSurface = Color(0xFF2D2D2D)    // 표면 위의 텍스트

// === 감정별 색상 팔레트 ===

// 따뜻한 감정
val WarmPrimary = Color(0xFFD97706)          // 주황색 - 따뜻함, 그리움
val WarmSecondary = Color(0xFFEF4444)        // 빨간색 - 열정, 사랑
val WarmBackground = Color(0xFFFFFBEB)       // 따뜻한 크림색

// 차가운 감정
val CoolPrimary = Color(0xFF0891B2)          // 청록색 - 평온, 차분함
val CoolSecondary = Color(0xFF3B82F6)        // 파란색 - 시원함, 신뢰
val CoolBackground = Color(0xFFF0F9FF)       // 차가운 하늘색

// 자연 감정
val NaturePrimary = Color(0xFF059669)        // 녹색 - 자연, 생명력
val NatureSecondary = Color(0xFF10B981)      // 에메랄드 - 신선함
val NatureBackground = Color(0xFFF0FDF4)     // 연한 민트색

// 몽환적 감정
val DreamyPrimary = Color(0xFF7C3AED)        // 진한 보라 - 몽환, 신비
val DreamySecondary = Color(0xFFA855F7)      // 연한 보라 - 꿈같음
val DreamyBackground = Color(0xFFFAF5FF)     // 보라빛 배경

// 로맨틱 감정
val RomanticPrimary = Color(0xFFEC4899)      // 핑크 - 로맨스, 달콤함
val RomanticSecondary = Color(0xFFF472B6)    // 연한 핑크 - 부드러움
val RomanticBackground = Color(0xFFFDF2F8)   // 분홍빛 배경

// 모던 감정
val ModernPrimary = Color(0xFF6B7280)        // 회색 - 모던, 세련됨
val ModernSecondary = Color(0xFF9CA3AF)      // 연한 회색 - 미니멀
val ModernBackground = Color(0xFFF9FAFB)     // 중성 배경

// 빈티지 감정
val VintagePrimary = Color(0xFF92400E)       // 갈색 - 빈티지, 향수
val VintageSecondary = Color(0xFFD97706)     // 주황 갈색 - 따뜻한 추억
val VintageBackground = Color(0xFFFFFBEB)    // 오래된 종이색

// 석양 감정
val SunsetPrimary = Color(0xFFDC2626)        // 진한 빨강 - 석양
val SunsetSecondary = Color(0xFFFB923C)      // 주황 - 노을
val SunsetBackground = Color(0xFFFFFBEB)     // 따뜻한 배경

// 바다 감정
val OceanPrimary = Color(0xFF0C4A6E)         // 깊은 파랑 - 바다 깊이
val OceanSecondary = Color(0xFF0891B2)       // 청록 - 바다 표면
val OceanBackground = Color(0xFFF0F9FF)      // 바다빛 배경

// 숲 감정
val ForestPrimary = Color(0xFF14532D)        // 짙은 녹색 - 숲의 깊이
val ForestSecondary = Color(0xFF16A34A)      // 밝은 녹색 - 잎사귀
val ForestBackground = Color(0xFFF0FDF4)     // 숲빛 배경

// === 상황별 색상 ===

// 성공 상태
val SuccessGreen = Color(0xFF10B981)
val SuccessBackground = Color(0xFFF0FDF4)

// 경고 상태
val WarningOrange = Color(0xFFF59E0B)
val WarningBackground = Color(0xFFFFFBEB)

// 에러 상태
val ErrorRed = Color(0xFFEF4444)
val ErrorBackground = Color(0xFFFEF2F2)

// 정보 상태
val InfoBlue = Color(0xFF3B82F6)
val InfoBackground = Color(0xFFF0F9FF)

// === 그라데이션 색상 ===

// 메인 그라데이션
val GradientStart = Color(0xFF6B46C1)
val GradientMiddle = Color(0xFF8B5CF6)
val GradientEnd = Color(0xFFEC4899)

// 배경 그라데이션
val BackgroundGradientStart = Color(0xFFFAF5FF)
val BackgroundGradientEnd = Color(0xFFE9ECEF)

// === 접근성 색상 ===

// 고대비 색상 (접근성 준수)
val HighContrastDark = Color(0xFF000000)
val HighContrastLight = Color(0xFFFFFFFF)
val HighContrastFocus = Color(0xFF2563EB)

// 색맹 고려 색상
val ColorBlindSafe1 = Color(0xFF0173B2)      // 파랑
val ColorBlindSafe2 = Color(0xFFDE8F05)      // 주황
val ColorBlindSafe3 = Color(0xFF029E73)      // 청록
val ColorBlindSafe4 = Color(0xFFCC78BC)      // 분홍

// === 투명도 레벨 ===

const val AlphaDisabled = 0.38f
const val AlphaMedium = 0.54f
const val AlphaHigh = 0.87f
const val AlphaFull = 1.0f

// === 어두운 테마 색상 ===

val DarkPrimary = Color(0xFF8B5CF6)
val DarkSecondary = Color(0xFFF472B6)
val DarkBackground = Color(0xFF0F0F23)
val DarkSurface = Color(0xFF1E1E2E)
val DarkOnPrimary = Color(0xFF000000)
val DarkOnSecondary = Color(0xFF000000)
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkOnSurface = Color(0xFFCCCCCC)

// === 기존 Material3 색상 (호환성) ===

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// === 색상 유틸리티 함수 ===

/**
 * 색상의 투명도 조정
 */
fun Color.withAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha.coerceIn(0f, 1f))
}

/**
 * 색상의 밝기 확인
 */
fun Color.isLight(): Boolean {
    val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
    return luminance > 0.5
}

/**
 * 대비되는 텍스트 색상 반환
 */
fun Color.contrastColor(): Color {
    return if (isLight()) Color.Black else Color.White
}

/**
 * 색상 팔레트 생성
 */
object ColorPalettes {
    val warm = listOf(WarmPrimary, WarmSecondary, WarmBackground)
    val cool = listOf(CoolPrimary, CoolSecondary, CoolBackground)
    val nature = listOf(NaturePrimary, NatureSecondary, NatureBackground)
    val dreamy = listOf(DreamyPrimary, DreamySecondary, DreamyBackground)
    val romantic = listOf(RomanticPrimary, RomanticSecondary, RomanticBackground)
    val modern = listOf(ModernPrimary, ModernSecondary, ModernBackground)
    val vintage = listOf(VintagePrimary, VintageSecondary, VintageBackground)
    val sunset = listOf(SunsetPrimary, SunsetSecondary, SunsetBackground)
    val ocean = listOf(OceanPrimary, OceanSecondary, OceanBackground)
    val forest = listOf(ForestPrimary, ForestSecondary, ForestBackground)
}

/**
 * 감정에 따른 색상 매핑
 */
object EmotionColors {
    fun getColorForEmotion(emotion: String): List<Color> {
        return when (emotion.lowercase()) {
            "따뜻", "그리운", "nostalgic", "warm" -> ColorPalettes.warm
            "차분", "평온", "cool", "calm" -> ColorPalettes.cool
            "자연", "신선", "nature", "fresh" -> ColorPalettes.nature
            "꿈", "몽환", "dreamy", "fantasy" -> ColorPalettes.dreamy
            "로맨틱", "달콤", "romantic", "sweet" -> ColorPalettes.romantic
            "모던", "세련", "modern", "sophisticated" -> ColorPalettes.modern
            "빈티지", "옛날", "vintage", "retro" -> ColorPalettes.vintage
            "석양", "노을", "sunset", "dusk" -> ColorPalettes.sunset
            "바다", "깊이", "ocean", "deep" -> ColorPalettes.ocean
            "숲", "나무", "forest", "tree" -> ColorPalettes.forest
            else -> listOf(EllipsisPrimary, EllipsisSecondary, EllipsisBackground)
        }
    }
}