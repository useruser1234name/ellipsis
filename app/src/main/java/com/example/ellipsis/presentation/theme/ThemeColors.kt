package com.example.ellipsis.presentation.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * 테마 색상을 정의하는 데이터 클래스
 *
 * 주요 기능:
 * - 주요 색상, 보조 색상, 배경 색상 관리
 * - 색상 변환 및 조정 유틸리티
 * - 접근성 고려 색상 생성
 */
data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color
) {
    /**
     * 색상의 밝기 조정
     */
    fun adjustBrightness(factor: Float): ThemeColors {
        return copy(
            primary = adjustColorBrightness(primary, factor),
            secondary = adjustColorBrightness(secondary, factor),
            background = adjustColorBrightness(background, factor)
        )
    }

    /**
     * 색상의 채도 조정
     */
    fun adjustSaturation(factor: Float): ThemeColors {
        return copy(
            primary = adjustColorSaturation(primary, factor),
            secondary = adjustColorSaturation(secondary, factor),
            background = adjustColorSaturation(background, factor)
        )
    }

    /**
     * 색상의 투명도 조정
     */
    fun adjustAlpha(alpha: Float): ThemeColors {
        return copy(
            primary = primary.copy(alpha = alpha),
            secondary = secondary.copy(alpha = alpha),
            background = background.copy(alpha = alpha)
        )
    }

    /**
     * 다크 모드 버전 생성
     */
    fun toDarkMode(): ThemeColors {
        return copy(
            primary = primary.copy(alpha = 0.8f),
            secondary = secondary.copy(alpha = 0.7f),
            background = Color.Black.copy(alpha = 0.9f)
        )
    }

    /**
     * 라이트 모드 버전 생성
     */
    fun toLightMode(): ThemeColors {
        return copy(
            primary = adjustColorBrightness(primary, 1.2f),
            secondary = adjustColorBrightness(secondary, 1.1f),
            background = Color.White.copy(alpha = 0.95f)
        )
    }

    /**
     * 온도 기반 색상 조정 (따뜻함/차가움)
     */
    fun adjustTemperature(warmth: Float): ThemeColors {
        // warmth: -1.0 (차가움) ~ 1.0 (따뜻함)
        val warmthAdjustment = warmth.coerceIn(-1f, 1f)

        return copy(
            primary = adjustColorTemperature(primary, warmthAdjustment),
            secondary = adjustColorTemperature(secondary, warmthAdjustment),
            background = adjustColorTemperature(background, warmthAdjustment * 0.3f)
        )
    }

    /**
     * 접근성을 고려한 대비 색상 반환
     */
    fun getContrastColor(backgroundColor: Color = background): Color {
        val luminance = calculateLuminance(backgroundColor)
        return if (luminance > 0.5f) Color.Black else Color.White
    }

    /**
     * 테마의 주요 특성 반환
     */
    fun getCharacteristics(): ThemeCharacteristics {
        val primaryLuminance = calculateLuminance(primary)
        val saturation = calculateSaturation(primary)
        val warmth = calculateWarmth(primary)

        return ThemeCharacteristics(
            brightness = primaryLuminance,
            saturation = saturation,
            warmth = warmth,
            isDark = primaryLuminance < 0.5f,
            isVibrant = saturation > 0.6f,
            isWarm = warmth > 0.1f
        )
    }

    companion object {
        /**
         * 기본 테마 색상
         */
        val DEFAULT = ThemeColors(
            primary = Color(0xFF6B46C1),
            secondary = Color(0xFFEC4899),
            background = Color(0xFFFAF5FF)
        )

        /**
         * 두 테마 색상 블렌딩
         */
        fun blend(theme1: ThemeColors, theme2: ThemeColors, ratio: Float): ThemeColors {
            val blendRatio = ratio.coerceIn(0f, 1f)
            val inverseRatio = 1f - blendRatio

            return ThemeColors(
                primary = blendColors(theme1.primary, theme2.primary, inverseRatio, blendRatio),
                secondary = blendColors(theme1.secondary, theme2.secondary, inverseRatio, blendRatio),
                background = blendColors(theme1.background, theme2.background, inverseRatio, blendRatio)
            )
        }

        /**
         * 색상 목록으로부터 테마 생성
         */
        fun fromColors(colors: List<Color>): ThemeColors {
            return when (colors.size) {
                0 -> DEFAULT
                1 -> ThemeColors(
                    primary = colors[0],
                    secondary = adjustColorBrightness(colors[0], 1.2f),
                    background = adjustColorBrightness(colors[0], 1.8f).copy(alpha = 0.1f)
                )
                2 -> ThemeColors(
                    primary = colors[0],
                    secondary = colors[1],
                    background = adjustColorBrightness(colors[0], 1.8f).copy(alpha = 0.1f)
                )
                else -> ThemeColors(
                    primary = colors[0],
                    secondary = colors[1],
                    background = colors[2].copy(alpha = 0.1f)
                )
            }
        }
    }
}

/**
 * 테마 특성 정보
 */
data class ThemeCharacteristics(
    val brightness: Float,
    val saturation: Float,
    val warmth: Float,
    val isDark: Boolean,
    val isVibrant: Boolean,
    val isWarm: Boolean
) {
    fun getDescription(): String {
        val characteristics = mutableListOf<String>()

        if (isDark) characteristics.add("어두운") else characteristics.add("밝은")
        if (isVibrant) characteristics.add("생동감 있는") else characteristics.add("차분한")
        if (isWarm) characteristics.add("따뜻한") else characteristics.add("시원한")

        return characteristics.joinToString(", ") + " 테마"
    }
}

// === Helper Functions ===

/**
 * 색상의 밝기 조정
 */
private fun adjustColorBrightness(color: Color, factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * 색상의 채도 조정
 */
private fun adjustColorSaturation(color: Color, factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * 색상의 온도 조정 (따뜻함/차가움)
 */
private fun adjustColorTemperature(color: Color, warmth: Float): Color {
    val r = color.red
    val g = color.green
    val b = color.blue

    val newR = if (warmth > 0) {
        (r + warmth * 0.3f).coerceIn(0f, 1f)
    } else {
        r
    }

    val newB = if (warmth < 0) {
        (b + (-warmth) * 0.3f).coerceIn(0f, 1f)
    } else {
        b
    }

    return Color(newR, g, newB, color.alpha)
}

/**
 * 두 색상 블렌딩
 */
private fun blendColors(color1: Color, color2: Color, weight1: Float, weight2: Float): Color {
    return Color(
        red = (color1.red * weight1 + color2.red * weight2).coerceIn(0f, 1f),
        green = (color1.green * weight1 + color2.green * weight2).coerceIn(0f, 1f),
        blue = (color1.blue * weight1 + color2.blue * weight2).coerceIn(0f, 1f),
        alpha = (color1.alpha * weight1 + color2.alpha * weight2).coerceIn(0f, 1f)
    )
}

/**
 * 색상의 밝기(Luminance) 계산
 */
private fun calculateLuminance(color: Color): Float {
    // sRGB 상대 밝기 계산
    val r = if (color.red <= 0.03928f) {
        color.red / 12.92f
    } else {
        ((color.red + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    val g = if (color.green <= 0.03928f) {
        color.green / 12.92f
    } else {
        ((color.green + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    val b = if (color.blue <= 0.03928f) {
        color.blue / 12.92f
    } else {
        ((color.blue + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}


/**
 * 색상의 채도 계산
 */
private fun calculateSaturation(color: Color): Float {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    return hsv[1]
}

/**
 * 색상의 따뜻함 계산
 */
private fun calculateWarmth(color: Color): Float {
    // 빨간색과 파란색의 비율로 따뜻함 계산
    return (color.red - color.blue).coerceIn(-1f, 1f)
}

/**
 * Color를 Android 색상으로 변환
 */
private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}