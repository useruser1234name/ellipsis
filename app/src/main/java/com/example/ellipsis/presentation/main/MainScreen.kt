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
 * 메인 화면 컴포넌트 (개선된 버전)
 *
 * 개선사항:
 * - 오선지 효과와 파동 애니메이션
 * - 더 음악적이고 감성적인 시각 효과
 * - 팔각형 제거, 순수한 원형 디자인
 * - 부드러운 숨 쉬는 애니메이션
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    initialImageUris: List<Uri> = emptyList(),
    isFromShare: Boolean = false
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // UI 상태 (remember로 최적화)
    var imageUris by remember { mutableStateOf(initialImageUris) }
    var hashtags by remember { mutableStateOf(TextFieldValue()) }
    var currentThemeColors by remember { mutableStateOf(getDefaultTheme()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // 이미지 선택 런처 (콜백 최적화)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        scope.launch {
            handleImageSelection(
                uris = uris,
                onUrisUpdated = { imageUris = it },
                onThemeUpdated = { currentThemeColors = it },
                onShowSnackbar = { message ->
                    snackbarHostState.showSnackbar(message)
                }
            )
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { imageUris.size }
    )

    // 애니메이션된 테마 색상 (메모이제이션)
    val animatedColors = AnimatedThemeColors(currentThemeColors)

    // 공유로 들어온 경우 초기 설정 (LaunchedEffect 최적화)
    LaunchedEffect(initialImageUris) {
        if (initialImageUris.isNotEmpty() && imageUris.isEmpty()) {
            imageUris = initialImageUris
            // 기본 테마 사용 (실제 이미지 분석은 서버에서)
            currentThemeColors = getDefaultTheme()
            Timber.d("공유된 이미지 ${initialImageUris.size}개로 초기화")
        }
    }

    // 해시태그 변경 시 테마 조정 (간단한 매핑 사용)
    LaunchedEffect(hashtags.text, imageUris) {
        if (imageUris.isNotEmpty() && hashtags.text.isNotBlank()) {
            delay(500) // 500ms debounce
            // 간단한 해시태그 기반 색상 변경
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
    LaunchedEffect(isFromShare, imageUris) {
        if (isFromShare && imageUris.isNotEmpty() && !state.isLoading) {
            delay(1000) // 사용자 확인 시간
            viewModel.recommendMusic(context, imageUris, hashtags.text)
        }
    }

    // 메인 UI 렌더링
    MainScreenContent(
        imageUris = imageUris,
        hashtags = hashtags,
        onHashtagsChange = { hashtags = it },
        state = state,
        animatedColors = animatedColors,
        pagerState = pagerState,
        scrollState = scrollState,
        snackbarHostState = snackbarHostState,
        onSelectPhotos = { launcher.launch("image/*") },
        onRecommendMusic = {
            viewModel.recommendMusic(context, imageUris, hashtags.text)
        },
        isFromShare = isFromShare
    )
}

/**
 * 이미지 선택 처리 로직 분리
 */
private suspend fun handleImageSelection(
    uris: List<Uri>,
    onUrisUpdated: (List<Uri>) -> Unit,
    onThemeUpdated: (ThemeColors) -> Unit,
    onShowSnackbar: suspend (String) -> Unit
) {
    val maxImageCount = 10

    when {
        uris.isEmpty() -> return

        uris.size > maxImageCount -> {
            val selectedUris = uris.take(maxImageCount)
            onUrisUpdated(selectedUris)
            onThemeUpdated(getDefaultTheme())
            onShowSnackbar("최대 ${maxImageCount}장까지만 선택할 수 있어요.")
        }

        else -> {
            onUrisUpdated(uris)
            onThemeUpdated(getDefaultTheme())
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

/**
 * 메인 화면 컨텐츠 (분리된 컴포넌트)
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
    onSelectPhotos: () -> Unit,
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
        // 🎵 향상된 배경 오선지
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

            Spacer(modifier = Modifier.height(50.dp)) // 간격 증가

            // 메인 콘텐츠
            if (imageUris.isEmpty()) {
                EmptyStateContent(
                    onSelectPhotos = onSelectPhotos,
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
                    onAddMorePhotos = onSelectPhotos
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
 * 🎵 개선된 빈 상태 컨텐츠 (오선지 효과와 파동 애니메이션)
 */
@Composable
private fun EmptyStateContent(
    onSelectPhotos: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color
) {
    // 더 부드러운 파동 애니메이션 (숨 쉬는 효과)
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_animation")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f, // 더 미세한 변화 (1.1 → 1.05)
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000, // 더 천천히 (2000 → 3000)
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    // 떠다니는 효과
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
            .offset(y = floatingOffset.dp) // 부드러운 떠다니는 효과
    ) {
        //  개선된 중앙 메모리 점
        CentralMemoryDot(
            scale = breathingScale,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            onClick = onSelectPhotos
        )

        Spacer(modifier = Modifier.height(40.dp)) // 간격 늘림

        //  더 시적인 설명 텍스트
        Text(
            text = "첫 번째 기억을\n음표로 남겨보세요",
            fontSize = 20.sp, // 글씨 크기 증가
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
            textAlign = TextAlign.Center,
//            letterSpacing = 4.sp
        )


        Spacer(modifier = Modifier.height(32.dp))

        //  개선된 버튼
        EnhancedSelectPhotosButton(
            onClick = onSelectPhotos,
            primaryColor = primaryColor
        )
    }
}

/**
 *중앙 메모리 점 컴포넌트
 */
@Composable
private fun CentralMemoryDot(
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(200.dp), // 오선지 공간 확보
        contentAlignment = Alignment.Center
    ) {

        //  주 메모리 점 (부드러운 파동 효과)
        Box(
            modifier = Modifier
                .size((100 * scale).dp) // 기본 크기 줄임 (140 → 100)
                .shadow(
                    elevation = (16 * scale).dp,
                    shape = CircleShape,
                    ambientColor = primaryColor.copy(alpha = 0.2f),
                    spotColor = primaryColor.copy(alpha = 0.3f)
                )
                .background(
                    //  단순하고 깔끔한 그라데이션
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
            // 내부 글로우 효과
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
                // 음표 아이콘 (더 작고 우아하게)
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "첫 번째 기억 추가",
                    tint = primaryColor.copy(alpha = 0.8f),
                    modifier = Modifier.size((24 * scale).dp)
                )
            }
        }

        // 추가 파동 효과 (외곽)
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
 * 점 주변 오선지 효과
 */
@Composable
private fun StaffLinesAroundDot(
    primaryColor: Color,
    scale: Float
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val lineSpacing = 20.dp.toPx()
        val lineLength = (80 * scale).dp.toPx()

        // 점 위아래로 오선 그리기 (총 5개선)
        for (i in -2..2) {
            val y = centerY + (i * lineSpacing)
            val alpha = (1f - kotlin.math.abs(i) * 0.2f) * 0.4f // 중앙이 더 진하게

            drawLine(
                color = primaryColor.copy(alpha = alpha),
                start = Offset(centerX - lineLength/2, y),
                end = Offset(centerX + lineLength/2, y),
                strokeWidth = (1.5f * scale).dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 미세한 음표들 (점 주변에 떠다니는 효과)
        val notePositions = listOf(
            Offset(centerX - 60.dp.toPx(), centerY - 40.dp.toPx()),
            Offset(centerX + 50.dp.toPx(), centerY - 30.dp.toPx()),
            Offset(centerX - 40.dp.toPx(), centerY + 45.dp.toPx()),
            Offset(centerX + 45.dp.toPx(), centerY + 35.dp.toPx())
        )

        notePositions.forEachIndexed { index, position ->
            val noteAlpha = (0.3f - index * 0.05f) * scale
            drawCircle(
                color = primaryColor.copy(alpha = noteAlpha),
                radius = (3 * scale).dp.toPx(),
                center = position
            )
        }
    }
}

/**
 * 개선된 사진 선택 버튼
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
 * 로딩 오버레이 컴포넌트
 */
@Composable
private fun LoadingOverlay(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) { /* 클릭 무시 */ },
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
                // 애니메이션 음표들
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

/**
 * 애니메이션 음표들
 */
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

/**
 * 향상된 배경 오선지 (더 음악적으로)
 */
@Composable
private fun EnhancedMusicalStaffBackground(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val staffLineSpacing = 50.dp.toPx()
        val startY = size.height * 0.25f

        // 메인 오선지 (5개선)
        repeat(5) { lineIndex ->
            val y = startY + (lineIndex * staffLineSpacing)
            val alpha = 0.08f - (lineIndex * 0.01f) // 위쪽이 더 연하게

            drawLine(
                color = primaryColor.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 추가 보조선 (더 연하게)
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

        // 떠다니는 음표들 (매우 연하게)
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

/**
 * 커스텀 스낵바
 */
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

/**
 * 애니메이션된 테마 색상 데이터 클래스
 */
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

private fun adjustThemeBasedOnHashtags(hashtags: String): ThemeColors {
    val hashtagText = hashtags.lowercase()
    return when {
        hashtagText.contains("그리움") || hashtagText.contains("nostalgic") -> ThemeColors(
            primary = Color(0xFFD97706),
            secondary = Color(0xFFEF4444),
            background = Color(0xFFFFFBEB)
        )
        hashtagText.contains("평온") || hashtagText.contains("calm") -> ThemeColors(
            primary = Color(0xFF0891B2),
            secondary = Color(0xFF3B82F6),
            background = Color(0xFFF0F9FF)
        )
        hashtagText.contains("자연") || hashtagText.contains("nature") -> ThemeColors(
            primary = Color(0xFF059669),
            secondary = Color(0xFF10B981),
            background = Color(0xFFF0FDF4)
        )
        hashtagText.contains("로맨틱") || hashtagText.contains("romantic") -> ThemeColors(
            primary = Color(0xFFEC4899),
            secondary = Color(0xFFF472B6),
            background = Color(0xFFFDF2F8)
        )
        else -> getDefaultTheme()
    }
}