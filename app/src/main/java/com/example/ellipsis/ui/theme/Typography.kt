package com.example.ellipsis.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ellipsis 앱의 타이포그래피 시스템
 *
 * 디자인 원칙:
 * - 우아하고 미니멀한 느낌
 * - 음악과 감정을 표현하는 섬세함
 * - 가독성과 심미성의 균형
 * - 다양한 화면 크기에 대응
 */

// === 폰트 패밀리 정의 ===

/**
 * 기본 폰트 패밀리 (시스템 폰트 사용)
 * - Android: Roboto
 * - iOS에서 사용 시: San Francisco
 */
val DefaultFontFamily = FontFamily.Default

/**
 * 브랜드 전용 폰트 패밀리 (추후 커스텀 폰트 추가 시 사용)
 * 현재는 시스템 폰트를 사용하되, 향후 확장 가능
 */
val EllipsisFontFamily = FontFamily.Default

/**
 * 모노스페이스 폰트 (코드, 숫자 표시용)
 */
val MonospaceFontFamily = FontFamily.Monospace

// === 타이포그래피 정의 ===

/**
 * ellipsis 앱의 완전한 타이포그래피 시스템
 * Material Design 3 기준을 따르되, 브랜드 특성에 맞게 조정
 */
val Typography = Typography(
    // === Display Styles (큰 제목) ===

    /**
     * Display Large - 앱 메인 타이틀용
     * 사용처: 스플래시 화면, 메인 브랜딩
     */
    displayLarge = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),

    /**
     * Display Medium - 주요 섹션 제목
     * 사용처: 페이지 메인 제목
     */
    displayMedium = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),

    /**
     * Display Small - 작은 디스플레이 제목
     * 사용처: 카드 메인 제목
     */
    displaySmall = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // === Headline Styles (헤드라인) ===

    /**
     * Headline Large - 대형 헤드라인
     * 사용처: 주요 기능 제목
     */
    headlineLarge = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    /**
     * Headline Medium - 중간 헤드라인
     * 사용처: 섹션 제목, 중요한 정보
     */
    headlineMedium = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    /**
     * Headline Small - 작은 헤드라인
     * 사용처: 서브 섹션 제목
     */
    headlineSmall = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // === Title Styles (타이틀) ===

    /**
     * Title Large - 큰 타이틀
     * 사용처: 다이얼로그 제목, 중요한 카드 제목
     */
    titleLarge = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    /**
     * Title Medium - 중간 타이틀
     * 사용처: 앱바 제목, 리스트 아이템 제목
     */
    titleMedium = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    /**
     * Title Small - 작은 타이틀
     * 사용처: 서브 제목, 강조 텍스트
     */
    titleSmall = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // === Body Styles (본문) ===

    /**
     * Body Large - 큰 본문 텍스트
     * 사용처: 주요 내용, 긴 텍스트
     */
    bodyLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    /**
     * Body Medium - 기본 본문 텍스트
     * 사용처: 일반적인 텍스트, 설명
     */
    bodyMedium = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    /**
     * Body Small - 작은 본문 텍스트
     * 사용처: 보조 정보, 캡션
     */
    bodySmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // === Label Styles (라벨) ===

    /**
     * Label Large - 큰 라벨
     * 사용처: 버튼 텍스트, 탭 라벨
     */
    labelLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    /**
     * Label Medium - 중간 라벨
     * 사용처: 폼 라벨, 태그
     */
    labelMedium = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    /**
     * Label Small - 작은 라벨
     * 사용처: 상태 표시, 메타데이터
     */
    labelSmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// === 커스텀 타이포그래피 스타일 ===

/**
 * ellipsis 앱 전용 커스텀 텍스트 스타일들
 */
object EllipsisTextStyles {

    /**
     * 앱 로고/브랜드명 전용 스타일
     */
    val brandLogo = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 8.sp
    )

    /**
     * 태그라인 전용 스타일
     */
    val tagline = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 2.sp
    )

    /**
     * 음악 제목 전용 스타일
     */
    val musicTitle = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    /**
     * 아티스트명 전용 스타일
     */
    val artistName = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    /**
     * 감정 키워드 전용 스타일
     */
    val emotionKeyword = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    /**
     * 메타데이터 정보 전용 스타일
     */
    val metadata = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    )

    /**
     * 에러 메시지 전용 스타일
     */
    val errorMessage = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    /**
     * 성공 메시지 전용 스타일
     */
    val successMessage = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    /**
     * 로딩 메시지 전용 스타일
     */
    val loadingMessage = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

    /**
     * 점진적 텍스트 (애니메이션용)
     */
    val progressiveText = TextStyle(
        fontFamily = EllipsisFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.sp
    )

    /**
     * 카운터/숫자 표시 전용
     */
    val counter = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    /**
     * 타임스탬프 전용 스타일
     */
    val timestamp = TextStyle(
        fontFamily = MonospaceFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
}

// === 반응형 타이포그래피 ===

/**
 * 화면 크기에 따른 반응형 타이포그래피
 */
object ResponsiveTypography {

    /**
     * 작은 화면용 타이포그래피 (폰)
     */
    val compact = Typography.copy(
        displayLarge = Typography.displayLarge.copy(fontSize = 48.sp),
        displayMedium = Typography.displayMedium.copy(fontSize = 36.sp),
        displaySmall = Typography.displaySmall.copy(fontSize = 28.sp),
        headlineLarge = Typography.headlineLarge.copy(fontSize = 28.sp),
        headlineMedium = Typography.headlineMedium.copy(fontSize = 24.sp),
        headlineSmall = Typography.headlineSmall.copy(fontSize = 20.sp)
    )

    /**
     * 중간 화면용 타이포그래피 (태블릿 세로)
     */
    val medium = Typography

    /**
     * 큰 화면용 타이포그래피 (태블릿 가로, 데스크톱)
     */
    val expanded = Typography.copy(
        displayLarge = Typography.displayLarge.copy(fontSize = 64.sp),
        displayMedium = Typography.displayMedium.copy(fontSize = 52.sp),
        displaySmall = Typography.displaySmall.copy(fontSize = 40.sp),
        headlineLarge = Typography.headlineLarge.copy(fontSize = 36.sp),
        headlineMedium = Typography.headlineMedium.copy(fontSize = 32.sp),
        headlineSmall = Typography.headlineSmall.copy(fontSize = 28.sp)
    )
}

// === 접근성 타이포그래피 ===

/**
 * 접근성을 고려한 타이포그래피 변형
 */
object AccessibleTypography {

    /**
     * 큰 글씨 모드 (시각 장애인 고려)
     */
    val largeText = Typography.copy(
        bodyLarge = Typography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
        bodyMedium = Typography.bodyMedium.copy(fontSize = 18.sp, lineHeight = 26.sp),
        bodySmall = Typography.bodySmall.copy(fontSize = 16.sp, lineHeight = 24.sp),
        labelLarge = Typography.labelLarge.copy(fontSize = 18.sp),
        labelMedium = Typography.labelMedium.copy(fontSize = 16.sp),
        labelSmall = Typography.labelSmall.copy(fontSize = 14.sp)
    )

    /**
     * 고대비 모드용 두꺼운 글씨
     */
    val highContrast = Typography.copy(
        bodyLarge = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        bodyMedium = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        bodySmall = Typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        labelLarge = Typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        labelMedium = Typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = Typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
    )
}

// === 유틸리티 함수 ===

/**
 * 텍스트 스타일 확장 함수들
 */

/**
 * 투명도 적용
 */
fun TextStyle.withAlpha(alpha: Float): TextStyle {
    return this.copy(color = this.color.copy(alpha = alpha))
}

/**
 * 크기 스케일 적용
 */
fun TextStyle.scaled(factor: Float): TextStyle {
    return this.copy(
        fontSize = fontSize * factor,
        lineHeight = lineHeight * factor
    )
}

/**
 * 자간 조정
 */
fun TextStyle.withLetterSpacing(spacing: androidx.compose.ui.unit.TextUnit): TextStyle {
    return this.copy(letterSpacing = spacing)
}

/**
 * 폰트 두께 변경
 */
fun TextStyle.withWeight(weight: FontWeight): TextStyle {
    return this.copy(fontWeight = weight)
}