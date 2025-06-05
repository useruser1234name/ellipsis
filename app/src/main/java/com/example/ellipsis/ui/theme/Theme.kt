package com.example.ellipsis.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlin.math.pow

/**
 * ellipsis 앱의 라이트 테마 색상 스킴
 * 새로운 디자인 시스템에 맞게 설계됨
 */
private val LightColorScheme = lightColorScheme(
    // 주요 색상
    primary = EllipsisPrimary,
    onPrimary = EllipsisOnPrimary,
    primaryContainer = EllipsisPrimary.copy(alpha = 0.1f),
    onPrimaryContainer = EllipsisPrimary,

    // 보조 색상
    secondary = EllipsisSecondary,
    onSecondary = EllipsisOnSecondary,
    secondaryContainer = EllipsisSecondary.copy(alpha = 0.1f),
    onSecondaryContainer = EllipsisSecondary,

    // 강조 색상
    tertiary = EllipsisAccent,
    onTertiary = Color.White,
    tertiaryContainer = EllipsisAccent.copy(alpha = 0.1f),
    onTertiaryContainer = EllipsisAccent,

    // 배경 색상
    background = EllipsisBackground,
    onBackground = EllipsisOnBackground,

    // 표면 색상
    surface = EllipsisSurface,
    onSurface = EllipsisOnSurface,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF6B7280),

    // 상태 색상
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorBackground,
    onErrorContainer = ErrorRed,

    // 아웃라인 색상
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),

    // 역영역 색상
    inverseSurface = Color(0xFF2D2D2D),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = EllipsisPrimary.copy(alpha = 0.8f),

    // 그림자 색상
    scrim = Color.Black,

    // 표면 색조
    surfaceTint = EllipsisPrimary
)

/**
 * ellipsis 앱의 다크 테마 색상 스킴
 */
private val DarkColorScheme = darkColorScheme(
    // 주요 색상
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = DarkPrimary,

    // 보조 색상
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = DarkSecondary,

    // 강조 색상
    tertiary = EllipsisAccent.copy(alpha = 0.9f),
    onTertiary = Color.Black,
    tertiaryContainer = EllipsisAccent.copy(alpha = 0.2f),
    onTertiaryContainer = EllipsisAccent,

    // 배경 색상
    background = DarkBackground,
    onBackground = DarkOnBackground,

    // 표면 색상
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF2A2A3A),
    onSurfaceVariant = Color(0xFFB0B0B0),

    // 상태 색상
    error = ErrorRed.copy(alpha = 0.9f),
    onError = Color.Black,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed,

    // 아웃라인 색상
    outline = Color(0xFF4B5563),
    outlineVariant = Color(0xFF374151),

    // 역영역 색상
    inverseSurface = Color(0xFFF5F5F5),
    inverseOnSurface = Color(0xFF2D2D2D),
    inversePrimary = EllipsisPrimary,

    // 그림자 색상
    scrim = Color.Black,

    // 표면 색조
    surfaceTint = DarkPrimary
)

/**
 * ellipsis 테마 컴포저블
 *
 * 주요 기능:
 * - 라이트/다크 테마 자동 전환
 * - Android 12+ 동적 색상 지원
 * - 시스템 바 색상 자동 조정
 * - 접근성 고려 색상 적용
 */
@Composable
fun EllipsisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color는 Android 12+에서만 사용 가능
    dynamicColor: Boolean = true,
    // 커스텀 색상 스킴 사용 여부
    useCustomColorScheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 동적 색상 사용 (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !useCustomColorScheme -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // 다크 테마
        darkTheme -> DarkColorScheme

        // 라이트 테마 (기본)
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // 시스템 바 색상 설정
            val window = (view.context as Activity).window

            // 상태 바 색상
            window.statusBarColor = Color.Transparent.toArgb()

            // 네비게이션 바 색상
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.navigationBarColor = Color.Transparent.toArgb()
            }

            // 시스템 바 아이콘 색상 (라이트/다크 모드에 따라)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 감정 기반 테마 컴포저블
 * 특정 감정에 맞는 색상 스킴 적용
 */
@Composable
fun EllipsisEmotionTheme(
    emotion: String,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val emotionColors = EmotionColors.getColorForEmotion(emotion)

    val emotionColorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = emotionColors[0].copy(alpha = 0.9f),
            secondary = emotionColors[1].copy(alpha = 0.8f),
            background = Color(0xFF0F0F23),
            surface = Color(0xFF1E1E2E)
        )
    } else {
        LightColorScheme.copy(
            primary = emotionColors[0],
            secondary = emotionColors[1],
            background = emotionColors[2],
            surface = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.navigationBarColor = Color.Transparent.toArgb()
            }

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = emotionColorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 그라데이션 기반 테마 컴포저블
 * 다중 색상을 활용한 그라데이션 테마
 */
@Composable
fun EllipsisGradientTheme(
    primaryColor: Color,
    secondaryColor: Color,
    backgroundColor: Color = Color.White,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val gradientColorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = primaryColor.copy(alpha = 0.9f),
            secondary = secondaryColor.copy(alpha = 0.8f),
            background = Color(0xFF0F0F23),
            surface = Color(0xFF1E1E2E)
        )
    } else {
        LightColorScheme.copy(
            primary = primaryColor,
            secondary = secondaryColor,
            background = backgroundColor.copy(alpha = 0.1f),
            surface = Color.White
        )
    }

    MaterialTheme(
        colorScheme = gradientColorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 접근성 높은 대비 테마 컴포저블
 * 시각 장애인을 위한 고대비 테마
 */
@Composable
fun EllipsisHighContrastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val highContrastColorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = Color.White,
            onPrimary = Color.Black,
            secondary = Color(0xFFFFFF00), // 노란색 (고대비)
            onSecondary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color(0xFF1A1A1A),
            onSurface = Color.White,
            outline = Color.White,
            outlineVariant = Color(0xFF666666)
        )
    } else {
        LightColorScheme.copy(
            primary = Color.Black,
            onPrimary = Color.White,
            secondary = Color(0xFF0066CC), // 진한 파란색 (고대비)
            onSecondary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            outline = Color.Black,
            outlineVariant = Color(0xFF666666)
        )
    }

    MaterialTheme(
        colorScheme = highContrastColorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 테마 유틸리티 객체
 * 테마 관련 헬퍼 함수들 제공
 */
object ThemeUtils {
    /**
     * 현재 테마가 다크 모드인지 확인
     */
    @Composable
    fun isDarkMode(): Boolean = isSystemInDarkTheme()

    /**
     * 동적 색상 지원 여부 확인
     */
    fun supportsDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /**
     * 색상의 접근성 대비율 계산
     */
    fun calculateContrastRatio(color1: Color, color2: Color): Float {
        val luminance1 = calculateLuminance(color1)
        val luminance2 = calculateLuminance(color2)

        val lighter = maxOf(luminance1, luminance2)
        val darker = minOf(luminance1, luminance2)

        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * 색상의 상대 밝기 계산
     */
    private fun calculateLuminance(color: Color): Float {
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
     * WCAG AA 접근성 기준 만족 여부 확인
     */
    fun meetsAccessibilityStandard(textColor: Color, backgroundColor: Color): Boolean {
        return calculateContrastRatio(textColor, backgroundColor) >= 4.5f
    }

    /**
     * WCAG AAA 접근성 기준 만족 여부 확인
     */
    fun meetsHighAccessibilityStandard(textColor: Color, backgroundColor: Color): Boolean {
        return calculateContrastRatio(textColor, backgroundColor) >= 7.0f
    }
}

/**
 * 테마 미리보기용 확장 함수들
 */

/**
 * 모든 테마 팔레트 미리보기
 */
object ThemePreviews {
    val allEmotionThemes = listOf(
        "따뜻", "차분", "자연", "꿈", "로맨틱",
        "모던", "빈티지", "석양", "바다", "숲"
    )

    @Composable
    fun PreviewAllThemes(content: @Composable (String) -> Unit) {
        allEmotionThemes.forEach { emotion ->
            EllipsisEmotionTheme(emotion = emotion) {
                content(emotion)
            }
        }
    }
}