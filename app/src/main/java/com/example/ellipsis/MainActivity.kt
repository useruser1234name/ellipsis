package com.example.ellipsis

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.ellipsis.di.MemoryManager
import com.example.ellipsis.di.UserPreferencesManager
import com.example.ellipsis.presentation.main.MainScreen
import com.example.ellipsis.ui.theme.EllipsisTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 의존성 주입
    @Inject lateinit var userPreferencesManager: UserPreferencesManager
    @Inject lateinit var memoryManager: MemoryManager

    // 상태 관리 (최적화)
    private var isAppReady by mutableStateOf(false)
    private var showPermissionDialog by mutableStateOf(false)
    private var permissionDeniedPermanently by mutableStateOf(false)
    private var sharedImageUris by mutableStateOf<List<Uri>>(emptyList())
    private var isFromShare by mutableStateOf(false)
    private var appTheme by mutableStateOf<EllipsisAppTheme?>(null)

    // 권한 요청
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 스플래시 스크린 설치
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 스플래시 조건 설정
        splashScreen.setKeepOnScreenCondition { !isAppReady }

        // 시스템 UI 설정
        setupSystemUI()

        // 인텐트 처리
        handleIncomingIntent(intent)

        Timber.d("MainActivity 생성 시작")

        // 앱 초기화
        lifecycleScope.launch {
            initializeActivity()
        }

        setContent {
            // 테마가 준비될 때까지 대기
            appTheme?.let { theme ->
                EllipsisTheme(
                    darkTheme = theme.isDarkMode,
                    dynamicColor = theme.useDynamicColors
                ) {
                    MainActivityContent()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("새로운 인텐트 수신")
        handleIncomingIntent(intent)
    }

    /**
     * 메인 액티비티 컨텐츠 컴포저블
     */
    @Composable
    private fun MainActivityContent() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            when {
                !isAppReady -> {
                    LoadingScreen()
                }
                canUseApp() -> {
                    AnimatedVisibility(
                        visible = isAppReady,
                        enter = fadeIn(animationSpec = tween(800)),
                        exit = fadeOut(animationSpec = tween(400))
                    ) {
                        MainScreen(
                            initialImageUris = sharedImageUris,
                            isFromShare = isFromShare
                        )
                    }
                }
                else -> {
                    PermissionRequestScreen()
                }
            }

            // 권한 요청 다이얼로그
            if (showPermissionDialog) {
                PermissionDialog()
            }
        }
    }

    /**
     * 앱 사용 가능 여부 판단
     */
    private fun canUseApp(): Boolean {
        return true // 권한 없어도 기본 기능 사용 가능
    }

    /**
     * 들어오는 인텐트 처리 (공유 등)
     */
    private fun handleIncomingIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                handleSingleImageShare(intent)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                handleMultipleImageShare(intent)
            }
        }
    }

    /**
     * 단일 이미지 공유 처리
     */
    private fun handleSingleImageShare(intent: Intent) {
        if (intent.type?.startsWith("image/") == true) {
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            imageUri?.let {
                sharedImageUris = listOf(it)
                isFromShare = true
                Timber.d("단일 이미지 공유 수신: $it")
            }
        }
    }

    /**
     * 다중 이미지 공유 처리
     */
    private fun handleMultipleImageShare(intent: Intent) {
        if (intent.type?.startsWith("image/") == true) {
            val imageUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            imageUris?.let {
                sharedImageUris = it
                isFromShare = true
                Timber.d("다중 이미지 공유 수신: ${it.size}개")
            }
        }
    }

    /**
     * 액티비티 초기화
     */
    private suspend fun initializeActivity() {
        try {
            Timber.d("액티비티 초기화 시작...")

            // 1. 테마 설정 로드
            loadThemeSettings()

            // 2. 권한 상태 확인
            checkPermissions()

            // 3. 사용자 설정 검증
            validateUserSettings()

            // 4. 앱 상태 로깅
            logAppState()

            // 최소 스플래시 시간 보장 (UX)
            delay(1000)

            isAppReady = true
            Timber.i("액티비티 초기화 완료")

        } catch (e: Exception) {
            Timber.e(e, "액티비티 초기화 중 오류")
            // 오류가 있어도 앱은 실행되도록
            isAppReady = true
        }
    }

    /**
     * 테마 설정 로드
     */
    private fun loadThemeSettings() {
        val themePreference = userPreferencesManager.getVisualThemePreference()
        val systemDarkMode = when (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }

        appTheme = EllipsisAppTheme(
            isDarkMode = when (themePreference) {
                "dark" -> true
                "light" -> false
                else -> systemDarkMode // "auto"
            },
            useDynamicColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            primaryColor = Color(0xFF6366F1) // 기본 색상, 추후 동적 변경
        )

        Timber.d("테마 설정 로드: ${appTheme}")
    }

    /**
     * 시스템 UI 설정 (상태바, 네비게이션바)
     */
    private fun setupSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 상태바 투명 설정
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    /**
     * 권한 상태 확인
     */
    private fun checkPermissions() {
        val requiredPermissions = getRequiredPermissions()
        val deniedPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            Timber.w("권한 필요: ${deniedPermissions.joinToString()}")
            // 권한이 없어도 앱 사용 가능하도록 변경
        } else {
            Timber.d("모든 권한 허용됨")
        }
    }

    /**
     * 필요한 권한 목록
     */
    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        // 위치 권한 (사진 촬영 위치 정보용)
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        // 저장소 권한 (사진 접근용)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // Android 13+ 사진 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        }

        return permissions
    }

    /**
     * 권한 요청 결과 처리
     */
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filter { !it.value }.keys

        if (deniedPermissions.isEmpty()) {
            Timber.i("모든 권한 허용됨")
            showPermissionDialog = false
            permissionDeniedPermanently = false
        } else {
            Timber.w("거부된 권한: ${deniedPermissions.joinToString()}")

            // 영구 거부 확인
            val permanentlyDenied = deniedPermissions.any { permission ->
                !shouldShowRequestPermissionRationale(permission)
            }

            permissionDeniedPermanently = permanentlyDenied
            showPermissionDialog = true
        }
    }

    /**
     * 권한 요청
     */
    private fun requestRequiredPermissions() {
        val permissions = getRequiredPermissions()
        permissionLauncher.launch(permissions.toTypedArray())
    }

    /**
     * 앱 설정 열기
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        settingsLauncher.launch(intent)
    }

    /**
     * 사용자 설정 검증
     */
    private fun validateUserSettings() {
        val userId = userPreferencesManager.getUserId()
        val memoryCount = memoryManager.getMemoryCount()

        Timber.d("사용자 설정 - ID: $userId, 메모리: ${memoryCount}개")

        // 사용자 설정이 유효한지 확인
        if (userId.isNullOrBlank()) {
            Timber.w("사용자 ID가 없음 - 앱 재초기화 필요할 수 있음")
        }
    }

    /**
     * 앱 상태 로깅
     */
    private fun logAppState() {
        Timber.i("앱 상태 정보 로깅 완료")
    }

    /**
     * 로딩 화면 컴포저블 (최적화됨)
     */
    @Composable
    private fun LoadingScreen() {
        val infiniteTransition = rememberInfiniteTransition(label = "loading_animation")

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        val pulse by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFAF5FF),
                            Color(0xFFE9ECEF)
                        ),
                        radius = 1000f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 로딩 아이콘
                LoadingIcon(rotation = rotation, pulse = pulse)

                // 앱 정보
                AppInfo()

                // 로딩 메시지
                Text(
                    "앱을 준비하고 있어요...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF8E8E8E),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    /**
     * 로딩 아이콘 컴포넌트
     */
    @Composable
    private fun LoadingIcon(rotation: Float, pulse: Float) {
        Box(
            modifier = Modifier
                .size((80 * pulse).dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color(0xFF6B46C1),
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
    }

    /**
     * 앱 정보 컴포넌트
     */
    @Composable
    private fun AppInfo() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "ellipsis",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF6B46C1),
                letterSpacing = 4.sp
            )
            Text(
                "memories become melodies",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF8E8E8E),
                letterSpacing = 1.sp
            )
        }
    }

    /**
     * 권한 요청 화면 컴포저블
     */
    @Composable
    private fun PermissionRequestScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFAF5FF), Color(0xFFE9ECEF)),
                        radius = 1000f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 앱 아이콘
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF6B46C1),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 앱 정보
                AppInfo()

                Spacer(modifier = Modifier.height(48.dp))

                // 권한 설명 텍스트
                Text(
                    "더 나은 경험을 위해\n권한이 필요해요",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF262626),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 권한 허용 버튼
                Button(
                    onClick = { requestRequiredPermissions() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B46C1))
                ) {
                    Text("권한 허용하기", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 나중에 설정 버튼
                TextButton(
                    onClick = {
                        // 권한 없이도 앱 사용 가능
                        showPermissionDialog = false
                    }
                ) {
                    Text("나중에 설정하기", color = Color(0xFF8E8E8E))
                }
            }
        }
    }

    /**
     * 권한 요청 다이얼로그
     */
    @Composable
    private fun PermissionDialog() {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = {
                Text("🎵", fontSize = 24.sp)
            },
            title = {
                Text(
                    text = if (permissionDeniedPermanently) "권한 설정 필요" else "권한 허용 필요",
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                Text(
                    text = if (permissionDeniedPermanently) {
                        "앱 설정에서 사진 및 위치 권한을 허용해주세요.\n\n" +
                                "• 사진: 음악 추천을 위한 이미지 분석\n" +
                                "• 위치: 촬영 장소 기반 감정 분석"
                    } else {
                        "더 나은 음악 추천을 위해 권한이 필요합니다.\n\n" +
                                "• 사진: 이미지 분석으로 감정 파악\n" +
                                "• 위치: 장소 기반 추천 개선"
                    },
                    textAlign = TextAlign.Start,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = if (permissionDeniedPermanently) ::openAppSettings else ::requestRequiredPermissions
                ) {
                    Text(
                        text = if (permissionDeniedPermanently) "설정 열기" else "권한 허용",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("나중에")
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        Timber.d("MainActivity onResume")

        // 권한 상태 재확인 (설정에서 돌아온 경우)
        if (permissionDeniedPermanently) {
            checkPermissions()
        }
    }
}

/**
 * 앱 테마 설정
 */
data class EllipsisAppTheme(
    val isDarkMode: Boolean,
    val useDynamicColors: Boolean,
    val primaryColor: Color
)