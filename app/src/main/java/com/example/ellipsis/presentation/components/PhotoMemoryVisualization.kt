package com.example.ellipsis.presentation.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.times

/**
 * 사진 기억 시각화 메인 컴포넌트 - 사진 추가 기능 개선 버전
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
    onAddMorePhotos: () -> Unit, // 기존 사진에 추가
    onSelectNewPhotos: () -> Unit = onAddMorePhotos, // 새로운 사진 선택 (기본값은 추가와 동일)
    onRemovePhoto: (Uri) -> Unit = {} // 사진 제거 콜백 추가
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

            // 개선된 사진 캐러셀 (사진 추가 기능 포함)
            PhotoCarousel(
                imageUris = imageUris,
                pagerState = pagerState,
                themeColors = themeColors,
                onAddMorePhotos = onAddMorePhotos,
                onRemovePhoto = onRemovePhoto
            )

            Spacer(modifier = Modifier.height(20.dp))

//            if (imageUris.isNotEmpty()) {
//                SelectedPhotosPreview(
//                    imageUris = imageUris,
//                    onRemovePhoto = onRemovePhoto,
//                    primaryColor = themeColors.primary,
//                    onAddMorePhotos = onAddMorePhotos
//                )
//                        Spacer(modifier = Modifier.height(16.dp))
//        }

        // 감정 해시태그 입력
        EmotionalHashtagInput(
            hashtags = hashtags,
            onHashtagsChange = onHashtagsChange,
            primaryColor = themeColors.primary
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

            // 하단 버튼들 (개선된 사진 추가 기능)
            Spacer(modifier = Modifier.height(16.dp))

            // 점들을 잇는 버튼
            ConnectDotsButton(
                onClick = onRecommendMusic,
                primaryColor = themeColors.primary,
                secondaryColor = themeColors.secondary,
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            BottomActionButtons(
                onAddMorePhotos = onAddMorePhotos,
                onSelectNewPhotos = onSelectNewPhotos,
                primaryColor = themeColors.primary,
                imageCount = imageUris.size
            )
        }
    }
}

@Composable
private fun BottomActionButtons(
    onAddMorePhotos: () -> Unit,
    onSelectNewPhotos: () -> Unit,
    primaryColor: Color,
    imageCount: Int
) {
    // 새로운 기억 시작 버튼만 남김
    TextButton(
        onClick = onSelectNewPhotos,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "새로운 기억 시작하기",
                color = primaryColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

///**
// * 하단 액션 버튼들 (사진 추가 vs 새로운 기억 시작)
// */
//@Composable
//private fun BottomActionButtons(
//    onAddMorePhotos: () -> Unit,
//    onSelectNewPhotos: () -> Unit,
//    primaryColor: Color,
//    imageCount: Int
//) {
//    Column(
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        // 현재 세트에 사진 추가 버튼 (개선된 텍스트)
//        if (imageCount < 10) {
//            OutlinedButton(
//                onClick = onAddMorePhotos,
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(20.dp),
//                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = primaryColor
//                )
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Add,
//                        contentDescription = null,
//                        tint = primaryColor.copy(alpha = 0.7f),
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Text(
//                        "사진 더 추가하기",
//                        color = primaryColor.copy(alpha = 0.8f),
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//
//                    // 진행률 표시
//                    Box(
//                        modifier = Modifier
//                            .background(
//                                primaryColor.copy(alpha = 0.1f),
//                                RoundedCornerShape(8.dp)
//                            )
//                            .padding(horizontal = 6.dp, vertical = 2.dp)
//                    ) {
//                        Text(
//                            "${imageCount}/10",
//                            color = primaryColor.copy(alpha = 0.6f),
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                }
//            }
//        } else {
//            // 최대 개수 도달시 표시
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(
//                    containerColor = primaryColor.copy(alpha = 0.1f)
//                )
//            ) {
//                Row(
//                    modifier = Modifier.padding(12.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Icon(
//                        Icons.Default.CheckCircle,
//                        contentDescription = null,
//                        tint = primaryColor.copy(alpha = 0.7f),
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Text(
//                        "최대 10장까지 선택됨",
//                        color = primaryColor.copy(alpha = 0.8f),
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//
//        // 새로운 기억 시작 버튼
//        TextButton(
//            onClick = onSelectNewPhotos,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(6.dp)
//            ) {
//                Icon(
//                    Icons.Default.Refresh,
//                    contentDescription = null,
//                    tint = primaryColor.copy(alpha = 0.6f),
//                    modifier = Modifier.size(16.dp)
//                )
//                Text(
//                    "새로운 기억 시작하기",
//                    color = primaryColor.copy(alpha = 0.6f),
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Light
//                )
//            }
//        }
//    }
//}

/**
 * 현재 세트에 사진 추가 버튼
 */
@Composable
private fun AddMorePhotosToCurrentSetButton(
    onClick: () -> Unit,
    primaryColor: Color,
    currentCount: Int
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = primaryColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "이 기억에 사진 추가하기",
                color = primaryColor.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "(${currentCount}/10)",
                color = primaryColor.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

/**
 * 새로운 기억 시작 버튼
 */
@Composable
private fun StartNewMemoryButton(
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
                Icons.Default.Refresh,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "새로운 기억 시작하기",
                color = primaryColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light
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
 * 개선된 사진 캐러셀 (인스타그램 스타일 사진 추가 기능 포함)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoCarousel(
    imageUris: List<Uri>,
    pagerState: PagerState,
    themeColors: ThemeColors,
    onAddMorePhotos: () -> Unit = {},
    onRemovePhoto: (Uri) -> Unit = {}
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

        // 우상단 사진 추가 버튼 (인스타그램 스타일)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            AddPhotoButton(
                onClick = onAddMorePhotos,
                primaryColor = themeColors.primary,
                imageCount = imageUris.size
            )
        }

        // 우측상단 사진 제거 버튼
        if (imageUris.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                RemovePhotoButton(
                    onClick = {
                        if (imageUris.isNotEmpty()) {
                            onRemovePhoto(imageUris[pagerState.currentPage])
                        }
                    },
                    primaryColor = themeColors.primary
                )
            }
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


        // 사진 개수 표시 (우하단)
        if (imageUris.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${imageUris.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RemovePhotoButton(
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                Color.Red.copy(alpha = 0.9f),
                CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Remove,
            contentDescription = "현재 사진 제거",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}


/**
 * 우상단 사진 추가 버튼 (인스타그램 스타일)
 */
@Composable
private fun AddPhotoButton(
    onClick: () -> Unit,
    primaryColor: Color,
    imageCount: Int,
    modifier: Modifier = Modifier
) {
    val canAddMore = imageCount < 10

    AnimatedVisibility(
        visible = canAddMore,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = modifier
                .size(24.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = primaryColor.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .clickable { onClick() },

            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "사진 추가",
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
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
        enabled = !isLoading,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp) // 이 줄 추가!
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
                    RoundedCornerShape(20.dp)
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
 * 선택된 사진들 미리보기
 */
@Composable
private fun SelectedPhotosPreview(
    imageUris: List<Uri>,
    onRemovePhoto: (Uri) -> Unit,
    primaryColor: Color,
    onAddMorePhotos: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = primaryColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "선택된 사진",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor.copy(alpha = 0.8f)
                    )
                    Text(
                        "(${imageUris.size}/10)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = primaryColor.copy(alpha = 0.6f)
                    )
                }

                // 추가 버튼
                if (imageUris.size < 10) {
                    IconButton(
                        onClick = onAddMorePhotos,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "사진 추가",
                            tint = primaryColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 사진 썸네일 그리드
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                itemsIndexed(imageUris) { index, uri ->
                    SelectedPhotoThumbnail(
                        uri = uri,
                        index = index + 1,
                        onRemove = { onRemovePhoto(uri) },
                        primaryColor = primaryColor
                    )
                }
            }

            // 도움말 텍스트
            if (imageUris.size < 10) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "최대 ${10 - imageUris.size}장 더 추가할 수 있어요",
                    fontSize = 11.sp,
                    color = primaryColor.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
private fun SelectedPhotoThumbnail(
    uri: Uri,
    index: Int,
    onRemove: () -> Unit,
    primaryColor: Color
) {
    Box(
        modifier = Modifier.size(60.dp)
    ) {
        // 사진 썸네일
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 순서 번호 (체크 스타일)
//        Box(
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .offset(x = 4.dp, y = (-4).dp)
//                .size(20.dp)
//                .background(
//                    primaryColor,
//                    CircleShape
//                )
//                .border(
//                    width = 2.dp,
//                    color = Color.White,
//                    shape = CircleShape
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = index.toString(),
//                color = Color.White,
//                fontSize = 10.sp,
//                fontWeight = FontWeight.Bold
//            )
//        }

        // 제거 버튼
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(20.dp)
                .background(
                    Color.Red.copy(alpha = 0.8f),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = CircleShape
                )
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "사진 제거",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}