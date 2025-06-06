package com.example.ellipsis.presentation.components

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ellipsis.presentation.main.MainUiState
import com.example.ellipsis.presentation.theme.ThemeColors
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 사진 기억 시각화 메인 컴포넌트 - IME 문제 완전 해결 버전
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoMemoryVisualization(
    imageUris: List<Uri>,
    pagerState: PagerState,
    hashtags: TextFieldValue,
    onHashtagsChange: (TextFieldValue) -> Unit,
    onRecommendMusic: () -> Unit,
    state: MainUiState,
    themeColors: ThemeColors,
    onAddMorePhotos: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 800.dp) // 최대 높이 제한
            .padding(horizontal = 24.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = themeColors.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime) // IME 패딩
                .padding(24.dp)
        ) {
            // 메모리 점들 시각화
            MemoryDotsHeader(imageUris.size, themeColors.primary)

            Spacer(modifier = Modifier.height(20.dp))

            // 사진 캐러셀
            PhotoCarousel(
                imageUris = imageUris,
                pagerState = pagerState,
                themeColors = themeColors
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 감정 해시태그 입력 - 개선된 IME 처리
            EmotionalHashtagInput(
                hashtags = hashtags,
                onHashtagsChange = onHashtagsChange,
                primaryColor = themeColors.primary,
//                scrollState = scrollState
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 점들을 잇는 버튼
            ConnectDotsButton(
                onClick = onRecommendMusic,
                primaryColor = themeColors.primary,
                secondaryColor = themeColors.secondary,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 음악 추천 결과
            if (state.recommendedSongs.isNotEmpty()) {
                MusicalNotesVisualization(
                    songs = state.recommendedSongs,
                    primaryColor = themeColors.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 메모리 메타데이터
            MemoryMetadata(state = state, primaryColor = themeColors.primary)

            // 감성 분석 결과
            if (state.extractedKeywords.isNotEmpty() || state.emotionalDescription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                EmotionalInsightsCard(
                    keywords = state.extractedKeywords,
                    description = state.emotionalDescription,
                    primaryColor = themeColors.primary
                )
            }

            // 더 많은 기억 추가 버튼
            Spacer(modifier = Modifier.height(16.dp))
            AddMoreMemoriesButton(
                onClick = onAddMorePhotos,
                primaryColor = themeColors.primary
            )
        }
    }
}

/**
 * 감정 해시태그 입력 필드 - 완전한 IME 처리
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmotionalHashtagInput(
    hashtags: TextFieldValue,
    onHashtagsChange: (TextFieldValue) -> Unit,
    primaryColor: Color
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // 키보드 상태 감지
    val imeInsets = WindowInsets.ime
    val isKeyboardVisible = imeInsets.getBottom(LocalDensity.current) > 0

    // 포커스 상태 추적
    var isFocused by remember { mutableStateOf(false) }

    // 키보드가 올라올 때 자동으로 스크롤
    LaunchedEffect(isKeyboardVisible, isFocused) {
        if (isKeyboardVisible && isFocused) {
            delay(250) // 키보드 애니메이션 대기
            bringIntoViewRequester.bringIntoView()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        OutlinedTextField(
            value = hashtags,
            onValueChange = onHashtagsChange,
            placeholder = {
                Text(
                    "감정을 태그로 남겨보세요... #그리움 #평온",
                    color = primaryColor.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.ModeEdit,
                    contentDescription = null,
                    tint = primaryColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = if (isKeyboardVisible) {
                {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                        }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "키보드 닫기",
                            tint = primaryColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        scope.launch {
                            delay(200)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor.copy(alpha = 0.6f),
                unfocusedBorderColor = primaryColor.copy(alpha = 0.2f),
                cursorColor = primaryColor,
                focusedTextColor = primaryColor
            ),
            singleLine = true,
            enabled = true
        )
    }
}

/**
 * 사진 캐러셀 컴포넌트
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoCarousel(
    imageUris: List<Uri>,
    pagerState: PagerState,
    themeColors: ThemeColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = rememberAsyncImagePainter(imageUris[page]),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 0.5.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                contentScale = ContentScale.Crop
            )
        }

        // 부드러운 페이지 인디케이터
        if (imageUris.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(
                        Color.White.copy(alpha = 0.8f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(imageUris.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                            .background(
                                if (index == pagerState.currentPage)
                                    themeColors.primary
                                else
                                    themeColors.primary.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

/**
 * 메모리 점들 헤더
 */
@Composable
private fun MemoryDotsHeader(count: Int, primaryColor: Color) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(minOf(count, 5)) { index ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        primaryColor.copy(alpha = 0.6f - (index * 0.1f)),
                        CircleShape
                    )
            )
            if (index < minOf(count, 5) - 1) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(
                            primaryColor.copy(alpha = 0.3f),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
        if (count > 5) {
            Text(
                text = " +${count - 5}",
                color = primaryColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

/**
 * 점들을 잇는 버튼 (음악 추천 버튼)
 */
@Composable
private fun ConnectDotsButton(
    onClick: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isLoading) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.4f),
                                secondaryColor.copy(alpha = 0.3f),
                                primaryColor.copy(alpha = 0.4f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.8f),
                                secondaryColor.copy(alpha = 0.6f),
                                primaryColor.copy(alpha = 0.8f)
                            )
                        )
                    },
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        "분석 중...",
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "기억을",
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    )
                    Text(
                        "·",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 20.sp
                    )
                    Text(
                        "선으로",
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    )
                    Text(
                        "·",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 20.sp
                    )
                    Text(
                        "이어보세요",
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * 음악 노트 시각화 컴포넌트
 */
@Composable
fun MusicalNotesVisualization(
    songs: List<String>,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "이어진 선율들",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = primaryColor.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 오선지와 음표들
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // 오선지 그리기
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawMusicalStaff(primaryColor.copy(alpha = 0.3f))
                }

                // 애니메이션 음표들
                songs.take(5).forEachIndexed { index, _ ->
                    val infiniteTransition = rememberInfiniteTransition(label = "note_$index")
                    val animatedOffset by infiniteTransition.animateFloat(
                        initialValue = -5f,
                        targetValue = 5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween((2000 + index * 500), easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "offset_$index"
                    )

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (60 + index * 60).dp,
                                y = (20 + (index % 5) * 20 + animatedOffset).dp
                            )
                            .size(16.dp)
                            .background(
                                primaryColor.copy(alpha = 0.8f - index * 0.1f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 노래 목록
            songs.forEach { song ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = primaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        song,
                        fontSize = 14.sp,
                        color = primaryColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "재생",
                        tint = primaryColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 오선지 그리기 함수
 */
private fun DrawScope.drawMusicalStaff(color: Color) {
    val lineSpacing = size.height / 6
    repeat(5) { index ->
        val y = lineSpacing + (index * lineSpacing)
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}

/**
 * 메모리 메타데이터 카드
 */
@Composable
fun MemoryMetadata(state: MainUiState, primaryColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.photoDate.isNotBlank()) {
                MetadataRow(
                    icon = Icons.Default.Schedule,
                    text = state.photoDate,
                    primaryColor = primaryColor
                )
            }
            if (state.locationName.isNotBlank()) {
                MetadataRow(
                    icon = Icons.Default.LocationOn,
                    text = state.locationName,
                    primaryColor = primaryColor
                )
            }
            if (state.detectedObjects.isNotEmpty()) {
                MetadataRow(
                    icon = Icons.Default.Visibility,
                    text = state.detectedObjects.joinToString(" · "),
                    primaryColor = primaryColor
                )
            }
        }
    }
}

/**
 * 메타데이터 행 컴포넌트
 */
@Composable
private fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    primaryColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = primaryColor.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = primaryColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.Light
        )
    }
}

/**
 * 감성 분석 결과 카드
 */
@Composable
fun EmotionalInsightsCard(
    keywords: List<String>,
    description: String,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = primaryColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "감성 분석",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor.copy(alpha = 0.8f)
                )
            }

            // 감성 키워드들
            if (keywords.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    keywords.take(3).forEach { keyword ->
                        Box(
                            modifier = Modifier
                                .background(
                                    primaryColor.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                keyword,
                                fontSize = 12.sp,
                                color = primaryColor.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }

            // 감성 설명
            if (description.isNotEmpty()) {
                Text(
                    description,
                    fontSize = 13.sp,
                    color = primaryColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Light,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * 더 많은 기억 추가 버튼
 */
@Composable
fun AddMoreMemoriesButton(
    onClick: () -> Unit,
    primaryColor: Color
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "더 많은 기억 추가하기",
                color = primaryColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}