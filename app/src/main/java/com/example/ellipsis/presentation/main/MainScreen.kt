package com.example.ellipsis.presentation.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ellipsis.presentation.components.PhotoMemoryVisualization
import com.example.ellipsis.presentation.theme.ThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 메인 화면 컴포넌트 (완전한 개선 버전)
 *
 * 포함된 모든 기능:
 * - 인스타그램 스타일 사진 추가 기능
 * - 선택된 사진 미리보기 및 체크 표시
 * - 사진 개별 제거 기능
 * - 확장된 감정 테마 시스템 (15가지 테마)
 * - 새로운 사진 선택 vs 기존 사진에 추가 구분
 * - 오선지 효과와 파동 애니메이션
 * - 부드러운 숨 쉬는 애니메이션
 */
/**
 * 메인 화면 컴포넌트 (수정된 버전 - MainActivity 콜백 사용)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    initialImageUris: List<Uri> = emptyList(),
    isFromShare: Boolean = false,
    onAddMorePhotos: () -> Unit = {},
    onStartNewMemory: () -> Unit = {},
    onRemovePhoto: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // UI 상태 - initialImageUris를 직접 사용하고 MainActivity에서 관리
    var hashtags by remember { mutableStateOf(TextFieldValue()) }
    var currentThemeColors by remember { mutableStateOf(getDefaultTheme()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // initialImageUris 변경 시 테마 업데이트
    LaunchedEffect(initialImageUris) {
        if (initialImageUris.isNotEmpty()) {
            currentThemeColors = getDefaultTheme()
            Timber.d("이미지 URI 변경: ${initialImageUris.size}개")
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { initialImageUris.size }
    )

    // 애니메이션된 테마 색상
    val animatedColors = AnimatedThemeColors(currentThemeColors)

    // 해시태그 변경 시 테마 조정
    LaunchedEffect(hashtags.text, initialImageUris) {
        if (initialImageUris.isNotEmpty() && hashtags.text.isNotBlank()) {
            delay(500) // debounce
            val adjustedTheme = adjustThemeBasedOnHashtags(hashtags.text)
            currentThemeColors = adjustedTheme
        }
    }

    // 에러 메시지 처리
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // 자동 분석 시작 (공유 이미지)
    LaunchedEffect(isFromShare, initialImageUris) {
        if (isFromShare && initialImageUris.isNotEmpty() && !state.isLoading) {
            delay(1000)
            viewModel.recommendMusic(context, initialImageUris, hashtags.text)
        }
    }

    // 메인 UI 렌더링
    MainScreenContent(
        imageUris = initialImageUris, // MainActivity에서 관리되는 URI 사용
        hashtags = hashtags,
        onHashtagsChange = { newHashtags -> hashtags = newHashtags },
        state = state,
        animatedColors = animatedColors,
        pagerState = pagerState,
        scrollState = scrollState,
        snackbarHostState = snackbarHostState,
        onSelectNewPhotos = onStartNewMemory, // MainActivity 콜백 사용
        onAddMorePhotos = onAddMorePhotos,    // MainActivity 콜백 사용
        onRemovePhoto = onRemovePhoto,        // MainActivity 콜백 사용
        onRecommendMusic = {
            viewModel.recommendMusic(context, initialImageUris, hashtags.text)
        },
        isFromShare = isFromShare
    )
}

/**
 * 메인 화면 컨텐츠 (수정된 버전)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainScreenContent(
    imageUris: List<Uri>,
    hashtags: TextFieldValue,
    onHashtagsChange: (TextFieldValue) -> Unit,
    state: MainUiState,
    animatedColors: AnimatedThemeColors,
    pagerState: androidx.compose.foundation.pager.PagerState,
    scrollState: ScrollState,
    snackbarHostState: SnackbarHostState,
    onSelectNewPhotos: () -> Unit,
    onAddMorePhotos: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    onRecommendMusic: () -> Unit,
    isFromShare: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        animatedColors.background,
                        animatedColors.background.copy(alpha = 0.7f),
                        animatedColors.primary.copy(alpha = 0.1f)
                    ),
                    radius = 1000f
                )
            )
    ) {
        // 향상된 배경 오선지
        EnhancedMusicalStaffBackground(
            primaryColor = animatedColors.primary,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .windowInsetsPadding(WindowInsets.ime),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 헤더
            AppHeader(
                primaryColor = animatedColors.primary,
                isFromShare = isFromShare
            )

            Spacer(modifier = Modifier.height(50.dp))

            // 메인 콘텐츠
            if (imageUris.isEmpty()) {
                EmptyStateContent(
                    onSelectPhotos = onSelectNewPhotos,
                    primaryColor = animatedColors.primary,
                    secondaryColor = animatedColors.secondary
                )
            } else {
                PhotoMemoryVisualization(
                    imageUris = imageUris,
                    pagerState = pagerState,
                    hashtags = hashtags,
                    onHashtagsChange = onHashtagsChange,
                    onRecommendMusic = onRecommendMusic,
                    state = state,
                    themeColors = ThemeColors(
                        animatedColors.primary,
                        animatedColors.secondary,
                        animatedColors.background
                    ),
                    onAddMorePhotos = onAddMorePhotos,    // MainActivity 콜백
                    onSelectNewPhotos = onSelectNewPhotos, // MainActivity 콜백
                    onRemovePhoto = onRemovePhoto         // MainActivity 콜백
                )
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // 로딩 오버레이
        if (state.isLoading) {
            LoadingOverlay(
                primaryColor = animatedColors.primary,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 플로팅 스낵바
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            CustomSnackbar(snackbarData, animatedColors.primary)
        }
    }
}

/**
 * 애니메이션된 테마 색상 컴포저블
 */
@Composable
private fun AnimatedThemeColors(targetColors: ThemeColors): AnimatedThemeColors {
    val animatedPrimary by animateColorAsState(
        targetValue = targetColors.primary,
        animationSpec = tween(1500, easing = EaseInOutCubic),
        label = "primary_color"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = targetColors.secondary,
        animationSpec = tween(1500, easing = EaseInOutCubic),
        label = "secondary_color"
    )
    val animatedBackground by animateColorAsState(
        targetValue = targetColors.background,
        animationSpec = tween(1500, easing = EaseInOutCubic),
        label = "background_color"
    )

    return AnimatedThemeColors(animatedPrimary, animatedSecondary, animatedBackground)
}

// ... 나머지 컴포넌트들은 동일 (AppHeader, EmptyStateContent 등)
/**
 * 앱 헤더 컴포넌트
 */
@Composable
private fun AppHeader(
    primaryColor: Color,
    isFromShare: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = "Ellipsis",
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            color = primaryColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isFromShare) {
                "shared memories become melodies"
            } else {
                "memories become melodies"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            color = primaryColor.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "· · ·",
            fontSize = 24.sp,
            color = primaryColor.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )

        // 공유 알림
        if (isFromShare) {
            Spacer(modifier = Modifier.height(16.dp))
            ShareNotification(primaryColor = primaryColor)
        }
    }
}

/**
 * 공유 알림 카드
 */
@Composable
private fun ShareNotification(primaryColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "공유된 사진으로 음악을 추천해드릴게요",
                fontSize = 12.sp,
                color = primaryColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Light
            )
        }
    }
}

/**
 * 빈 상태 컨텐츠
 */
@Composable
private fun EmptyStateContent(
    onSelectPhotos: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color
) {
    // 파동 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_animation")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_offset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .offset(y = floatingOffset.dp)
    ) {
        // 중앙 메모리 점
        CentralMemoryDot(
            scale = breathingScale,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            onClick = onSelectPhotos
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "첫 번째 기억을\n음표로 남겨보세요",
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            color = primaryColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "· · ·",
            fontSize = 24.sp,
            color = primaryColor.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 사진 선택 버튼
        EnhancedSelectPhotosButton(
            onClick = onSelectPhotos,
            primaryColor = primaryColor
        )
    }
}

/**
 * 중앙 메모리 점 컴포넌트
 */
@Composable
private fun CentralMemoryDot(
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((100 * scale).dp)
                .shadow(
                    elevation = (16 * scale).dp,
                    shape = CircleShape,
                    ambientColor = primaryColor.copy(alpha = 0.2f),
                    spotColor = primaryColor.copy(alpha = 0.3f)
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.9f),
                            primaryColor.copy(alpha = 0.6f),
                            primaryColor.copy(alpha = 0.3f)
                        ),
                        radius = 150f
                    ),
                    CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((60 * scale).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            radius = 80f
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "첫 번째 기억 추가",
                    tint = primaryColor.copy(alpha = 0.8f),
                    modifier = Modifier.size((24 * scale).dp)
                )
            }
        }

        // 파동 효과
        repeat(2) { index ->
            val delay = index * 1000
            val rippleScale by rememberInfiniteTransition(label = "ripple_$index").animateFloat(
                initialValue = 1f,
                targetValue = 1.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, delayMillis = delay, easing = EaseOut),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ripple_scale_$index"
            )

            val rippleAlpha by rememberInfiniteTransition(label = "ripple_alpha_$index").animateFloat(
                initialValue = 0.6f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, delayMillis = delay, easing = EaseOut),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ripple_alpha_$index"
            )

            Box(
                modifier = Modifier
                    .size((120 * rippleScale).dp)
                    .background(
                        primaryColor.copy(alpha = rippleAlpha * 0.3f),
                        CircleShape
                    )
            )
        }
    }
}

/**
 * 사진 선택 버튼
 */
@Composable
private fun EnhancedSelectPhotosButton(
    onClick: () -> Unit,
    primaryColor: Color
) {
    val buttonScale by rememberInfiniteTransition(label = "button_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(52.dp)
            .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = primaryColor.copy(alpha = 0.1f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "사진에서 음악 찾기",
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * 로딩 오버레이
 */
@Composable
private fun LoadingOverlay(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedMusicNotes(primaryColor = primaryColor)

                Text(
                    "기억을 분석하고 있어요...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor.copy(alpha = 0.8f)
                )

                Text(
                    "감정을 읽고 선율을 찾는 중입니다",
                    fontSize = 14.sp,
                    color = primaryColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AnimatedMusicNotes(primaryColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "note_$index")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_$index"
            )

            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .size((24 * scale).dp)
                    .graphicsLayer { rotationZ = index * 45f }
            )
        }
    }
}

@Composable
private fun EnhancedMusicalStaffBackground(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val staffLineSpacing = 50.dp.toPx()
        val startY = size.height * 0.25f

        repeat(5) { lineIndex ->
            val y = startY + (lineIndex * staffLineSpacing)
            val alpha = 0.08f - (lineIndex * 0.01f)

            drawLine(
                color = primaryColor.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        repeat(3) { lineIndex ->
            val y = startY + (5 * staffLineSpacing) + ((lineIndex + 1) * staffLineSpacing)

            drawLine(
                color = primaryColor.copy(alpha = 0.04f),
                start = Offset(size.width * 0.1f, y),
                end = Offset(size.width * 0.9f, y),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val notePositions = listOf(
            Offset(size.width * 0.15f, startY - 30.dp.toPx()),
            Offset(size.width * 0.85f, startY + 2 * staffLineSpacing),
            Offset(size.width * 0.25f, startY + 6 * staffLineSpacing),
            Offset(size.width * 0.75f, startY + 4 * staffLineSpacing)
        )

        notePositions.forEach { position ->
            drawCircle(
                color = primaryColor.copy(alpha = 0.06f),
                radius = 4.dp.toPx(),
                center = position
            )
        }
    }
}

@Composable
private fun CustomSnackbar(snackbarData: SnackbarData, primaryColor: Color) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.9f)
        ),
        modifier = Modifier
            .padding(16.dp)
            .blur(radius = 0.5.dp)
    ) {
        Text(
            text = snackbarData.visuals.message,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Light
        )
    }
}

data class AnimatedThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color
)

// Helper functions
private fun getDefaultTheme(): ThemeColors = ThemeColors(
    primary = Color(0xFF6B46C1),
    secondary = Color(0xFFEC4899),
    background = Color(0xFFFAF5FF)
)


/**
 * 확장된 해시태그 기반 테마 조정 시스템
 * 다양한 감정과 분위기를 반영한 색상 테마
 */
private fun adjustThemeBasedOnHashtags(hashtags: String): ThemeColors {
    val hashtagText = hashtags.lowercase()

    return when {
        // 신나는/흥분/에너지틱 (밝은 오렌지/레드/옐로우)
        hashtagText.containsAny(
            listOf(
                "신나는", "흥분", "에너지", "활기", "파티", "축제", "댄스", "재미",
                "exciting", "energetic", "party", "fun", "dance", "vibrant", "lively", "upbeat"
            )
        ) -> ThemeColors(
            primary = Color(0xFFFF6B35), // 밝은 오렌지
            secondary = Color(0xFFFFD23F), // 밝은 노랑
            background = Color(0xFFFFF8E1)
        )

        // 우울/슬픔/멜랑콜리 (어두운 블루/그레이)
        hashtagText.containsAny(
            listOf(
                "우울", "슬픔", "멜랑콜리", "쓸쓸", "외로움", "고독", "비", "눈물",
                "sad", "melancholy", "lonely", "depressed", "blue", "gloomy", "tears", "sorrow"
            )
        ) -> ThemeColors(
            primary = Color(0xFF455A64), // 어두운 블루그레이
            secondary = Color(0xFF607D8B), // 중간 블루그레이
            background = Color(0xFFECEFF1)
        )

        // 분노/격렬/폭발적 (진한 레드/다크레드)
        hashtagText.containsAny(
            listOf(
                "화남", "분노", "격렬", "폭발", "열정", "뜨거운", "강렬", "파워",
                "angry", "rage", "intense", "passionate", "fierce", "power", "explosive", "hot"
            )
        ) -> ThemeColors(
            primary = Color(0xFFD32F2F), // 진한 빨강
            secondary = Color(0xFFFF5722), // 오렌지 레드
            background = Color(0xFFFFEBEE)
        )

        //평온/차분/명상 (라이트 블루/그린)
        hashtagText.containsAny(
            listOf(
                "평온", "차분", "고요", "명상", "힐링", "평화", "조용", "안정",
                "calm", "peaceful", "serene", "meditation", "healing", "quiet", "zen", "tranquil"
            )
        ) -> ThemeColors(
            primary = Color(0xFF0891B2), // 차분한 청록
            secondary = Color(0xFF06B6D4), // 라이트 시안
            background = Color(0xFFF0F9FF)
        )

        // 로맨틱/사랑/달콤 (핑크/로즈)
        hashtagText.containsAny(
            listOf(
                "로맨틱", "사랑", "달콤", "연인", "데이트", "키스", "하트", "핑크",
                "romantic", "love", "sweet", "date", "kiss", "heart", "cute", "adorable"
            )
        ) -> ThemeColors(
            primary = Color(0xFFEC4899), // 로맨틱 핑크
            secondary = Color(0xFFF472B6), // 라이트 핑크
            background = Color(0xFFFDF2F8)
        )

        // 그리움/향수/추억 (세피아/오렌지/브라운)
        hashtagText.containsAny(
            listOf(
                "그리움", "향수", "추억", "옛날", "과거", "기억", "옛사랑", "세월",
                "nostalgic", "memory", "vintage", "old", "past", "reminiscent", "sepia", "classic"
            )
        ) -> ThemeColors(
            primary = Color(0xFFD97706), // 따뜻한 오렌지
            secondary = Color(0xFFF59E0B), // 골든 옐로우
            background = Color(0xFFFFFBEB)
        )

        // 자연/청량/상쾌 (그린 계열)
        hashtagText.containsAny(
            listOf(
                "자연", "청량", "상쾌", "숲", "나무", "풀", "산", "바다", "맑은",
                "nature", "fresh", "green", "forest", "tree", "ocean", "clean", "pure", "mint"
            )
        ) -> ThemeColors(
            primary = Color(0xFF059669), // 자연 그린
            secondary = Color(0xFF10B981), // 에메랄드
            background = Color(0xFFF0FDF4)
        )

        // 몽환/꿈/환상 (퍼플/라벤더)
        hashtagText.containsAny(
            listOf(
                "몽환", "꿈", "환상", "신비", "마법", "별", "달", "밤하늘",
                "dreamy", "fantasy", "magical", "mystical", "surreal", "ethereal", "starry", "lunar"
            )
        ) -> ThemeColors(
            primary = Color(0xFF7C3AED), // 딥 퍼플
            secondary = Color(0xFFA855F7), // 미디엄 퍼플
            background = Color(0xFFFAF5FF)
        )

        // 어둠/신비/미스터리 (다크 퍼플/블랙)
        hashtagText.containsAny(
            listOf(
                "어둠", "밤", "그림자", "미스터리", "고딕", "다크", "블랙", "음울",
                "dark", "night", "shadow", "mystery", "gothic", "black", "noir", "enigmatic"
            )
        ) -> ThemeColors(
            primary = Color(0xFF374151), // 다크 그레이
            secondary = Color(0xFF6B7280), // 미디엄 그레이
            background = Color(0xFFF9FAFB)
        )

        // 따뜻함/포근/아늑 (오렌지/브라운)
        hashtagText.containsAny(
            listOf(
                "따뜻", "포근", "아늑", "겨울", "캠프파이어", "집", "가족", "편안",
                "warm", "cozy", "comfort", "home", "family", "fireplace", "autumn", "golden"
            )
        ) -> ThemeColors(
            primary = Color(0xFFEA580C), // 따뜻한 오렌지
            secondary = Color(0xFFFB923C), // 라이트 오렌지
            background = Color(0xFFFFF7ED)
        )

        // 럭셔리/고급/우아 (골드/실버)
        hashtagText.containsAny(
            listOf(
                "럭셔리",
                "고급",
                "우아",
                "클래식",
                "골드",
                "실버",
                "품격",
                "세련",
                "luxury",
                "elegant",
                "classy",
                "sophisticated",
                "gold",
                "silver",
                "premium",
                "refined"
            )
        ) -> ThemeColors(
            primary = Color(0xFFB45309), // 골든 브라운
            secondary = Color(0xFFD97706), // 골드
            background = Color(0xFFFEF3C7)
        )

        // 🏖️ 여름/바캉스/휴양 (밝은 시안/터콰이즈)
        hashtagText.containsAny(
            listOf(
                "여름", "바캉스", "휴양", "바다", "수영", "해변", "태양", "리조트",
                "summer", "vacation", "beach", "ocean", "swimming", "sunny", "tropical", "resort"
            )
        ) -> ThemeColors(
            primary = Color(0xFF0891B2), // 오션 블루
            secondary = Color(0xFF22D3EE), // 터콰이즈
            background = Color(0xFFCFFAFE)
        )

        // 🍃 봄/새로운시작/희망 (라이트 그린/민트)
        hashtagText.containsAny(
            listOf(
                "봄", "새로운", "시작", "희망", "성장", "꽃", "싹", "생명",
                "spring", "new", "beginning", "hope", "growth", "flower", "life", "fresh"
            )
        ) -> ThemeColors(
            primary = Color(0xFF10B981), // 스프링 그린
            secondary = Color(0xFF6EE7B7), // 민트 그린
            background = Color(0xFFECFDF5)
        )

        // 🍂 가을/쓸쓸/고즈넉 (브라운/오렌지/레드)
        hashtagText.containsAny(
            listOf(
                "가을", "단풍", "고즈넉", "고요", "쓸쓸", "노을", "황혼", "갈색",
                "autumn", "fall", "leaves", "sunset", "twilight", "brown", "amber", "copper"
            )
        ) -> ThemeColors(
            primary = Color(0xFFDC2626), // 단풍 레드
            secondary = Color(0xFFEA580C), // 오텀 오렌지
            background = Color(0xFFFEF2F2)
        )

        // 기본 테마 (매칭되는 키워드가 없을 때)
        else -> getDefaultTheme()
    }
}

/**
 * 문자열이 리스트의 키워드 중 하나라도 포함하는지 확인하는 확장 함수
 */
private fun String.containsAny(keywords: List<String>): Boolean {
    return keywords.any { keyword -> this.contains(keyword, ignoreCase = true) }
}

/**
 * 현재 테마에 맞는 설명 텍스트 반환 (선택적 기능)
 */
private fun getThemeDescription(hashtags: String): String {
    val hashtagText = hashtags.lowercase()

    return when {
        hashtagText.containsAny(listOf("신나는", "흥분", "에너지", "exciting", "energetic", "party")) ->
            "활기찬 에너지가 느껴지는 테마"

        hashtagText.containsAny(listOf("우울", "슬픔", "멜랑콜리", "sad", "melancholy", "blue")) ->
            "차분하고 사색적인 분위기"

        hashtagText.containsAny(listOf("화남", "분노", "격렬", "angry", "intense", "passionate")) ->
            "강렬하고 역동적인 느낌"

        hashtagText.containsAny(listOf("평온", "차분", "고요", "calm", "peaceful", "zen")) ->
            "고요하고 평화로운 분위기"

        hashtagText.containsAny(listOf("로맨틱", "사랑", "달콤", "romantic", "love", "sweet")) ->
            "달콤하고 로맨틱한 무드"

        hashtagText.containsAny(listOf("그리움", "향수", "추억", "nostalgic", "memory", "vintage")) ->
            "따뜻한 추억이 담긴 느낌"

        hashtagText.containsAny(listOf("자연", "청량", "상쾌", "nature", "fresh", "green")) ->
            "자연의 싱그러움이 느껴지는 테마"

        hashtagText.containsAny(listOf("몽환", "꿈", "환상", "dreamy", "fantasy", "magical")) ->
            "몽환적이고 신비로운 분위기"

        hashtagText.containsAny(listOf("어둠", "밤", "미스터리", "dark", "night", "mystery")) ->
            "어둡고 신비로운 느낌"

        hashtagText.containsAny(listOf("따뜻", "포근", "아늑", "warm", "cozy", "comfort")) ->
            "따뜻하고 포근한 분위기"

        else -> "당신만의 특별한 기억"
    }
}