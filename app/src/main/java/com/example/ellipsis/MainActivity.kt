package com.example.ellipsis

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 의존성 주입
    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager
    @Inject
    lateinit var memoryManager: MemoryManager

    // 상태 관리
    private var isAppReady by mutableStateOf(false)
    private var showPermissionDialog by mutableStateOf(false)
    private var permissionDeniedPermanently by mutableStateOf(false)
    private var sharedImageUris by mutableStateOf<List<Uri>>(emptyList())
    private var isFromShare by mutableStateOf(false)
    private var appTheme by mutableStateOf<EllipsisAppTheme?>(null)
    private var selectedImageUris by mutableStateOf<List<Uri>>(emptyList())

    // 권한 요청 런처
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

    // 사진 선택 런처
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        handleSelectedPhotos(uris)
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
            testServerConnection()
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

    override fun onResume() {
        super.onResume()
        Timber.d("MainActivity onResume")

        // 권한 상태 재확인 (설정에서 돌아온 경우)
        if (permissionDeniedPermanently) {
            checkPermissions()
        }
    }

    // =============== 서버 연결 테스트 함수들 ===============

    /**
     * 서버 연결 테스트
     */
    private fun testServerConnection() {
        lifecycleScope.launch {
            try {
                Timber.d("=== 서버 연결 테스트 시작 ===")

                // 네트워크 상태 먼저 확인
                checkNetworkStatus()

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                // 1단계: Health check 테스트
                testHealthEndpoint(client)

                // 2단계: Root 엔드포인트 테스트
                testRootEndpoint(client)

                // 3단계: Recommend music 엔드포인트 테스트
                testRecommendMusicEndpoint(client)

            } catch (e: Exception) {
                Timber.e(e, "전체 연결 테스트 실패")
            }
        }
    }

    /**
     * Health 엔드포인트 테스트
     */
    private suspend fun testHealthEndpoint(client: OkHttpClient) {
        try {
            Timber.d("1. Health 엔드포인트 테스트...")

            val request = Request.Builder()
                .url("http://localhost:8000/health")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string()

            Timber.d("Health 응답 코드: ${response.code}")
            Timber.d("Health 응답: $responseBody")

            if (response.isSuccessful) {
                Timber.i("✅ Health check 성공!")
            } else {
                Timber.e("❌ Health check 실패: ${response.code}")
            }

        } catch (e: Exception) {
            Timber.e(e, "❌ Health 테스트 실패")
        }
    }

    /**
     * Root 엔드포인트 테스트
     */
    private suspend fun testRootEndpoint(client: OkHttpClient) {
        try {
            Timber.d("2. Root 엔드포인트 테스트...")

            val request = Request.Builder()
                .url("http://localhost:8000/")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string()

            Timber.d("Root 응답 코드: ${response.code}")
            Timber.d("Root 응답: $responseBody")

            if (response.isSuccessful) {
                Timber.i("✅ Root endpoint 성공!")
            } else {
                Timber.e("❌ Root endpoint 실패: ${response.code}")
            }

        } catch (e: Exception) {
            Timber.e(e, "❌ Root 테스트 실패")
        }
    }

    /**
     * Recommend Music 엔드포인트 테스트
     */
    private suspend fun testRecommendMusicEndpoint(client: OkHttpClient) {
        try {
            Timber.d("3. Recommend Music 엔드포인트 테스트...")

            // 간단한 JSON 데이터
            val json = """
                {
                    "hashtags": "#test #connection",
                    "cultural_context": "korean",
                    "max_songs": 3
                }
            """.trimIndent()

            val requestBody = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("http://localhost:8000/recommend-music")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()

            Timber.d("요청 URL: ${request.url}")
            Timber.d("요청 헤더: ${request.headers}")
            Timber.d("요청 본문: $json")

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string()

            Timber.d("Recommend 응답 코드: ${response.code}")
            Timber.d("Recommend 응답: $responseBody")

            if (response.isSuccessful) {
                Timber.i("✅ Recommend Music 성공!")
            } else {
                Timber.e("❌ Recommend Music 실패: ${response.code}")
            }

        } catch (e: Exception) {
            Timber.e(e, "❌ Recommend Music 테스트 실패")
        }
    }

    /**
     * 네트워크 상태 확인
     */
    private fun checkNetworkStatus() {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            Timber.d("=== 네트워크 상태 ===")
            Timber.d("네트워크 연결됨: ${network != null}")
            Timber.d("인터넷 사용 가능: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
            Timber.d("검증됨: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetworkInfo
                Timber.d("활성 네트워크: ${activeNetwork?.typeName}")
                Timber.d("연결 상태: ${activeNetwork?.isConnected}")
            }

        } catch (e: Exception) {
            Timber.e(e, "네트워크 상태 확인 실패")
        }
    }

    // =============== 사진 선택 관련 함수들 ===============

    /**
     * 선택된 사진들 처리 (중복 제거 포함)
     */
    private fun handleSelectedPhotos(newUris: List<Uri>) {
        if (newUris.isEmpty()) return

        // 중복 제거 로직
        val uniqueNewUris = newUris.filter { newUri ->
            !selectedImageUris.contains(newUri)
        }

        when {
            uniqueNewUris.isEmpty() -> {
                // 모든 사진이 이미 선택됨
                showDuplicateMessage("이미 선택된 사진들입니다")
            }
            uniqueNewUris.size < newUris.size -> {
                // 일부만 중복
                val duplicateCount = newUris.size - uniqueNewUris.size
                addUniquePhotos(uniqueNewUris)
                showDuplicateMessage("${duplicateCount}장의 중복된 사진을 제외했습니다")
            }
            else -> {
                // 모두 새로운 사진
                addUniquePhotos(uniqueNewUris)
            }
        }
    }

    /**
     * 기존 사진에 추가하기
     */
    private fun addMorePhotos() {
        // 현재 10장 제한 확인
        if (selectedImageUris.size >= 10) {
            showMessage("최대 10장까지 선택할 수 있습니다")
            return
        }

        photoPickerLauncher.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
        )
    }

    /**
     * 새로운 기억 시작하기
     */
    private fun startNewMemory() {
        selectedImageUris = emptyList()
        photoPickerLauncher.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
        )
    }

    /**
     * 고유한 사진들을 리스트에 추가
     */
    private fun addUniquePhotos(uniqueUris: List<Uri>) {
        val totalCount = selectedImageUris.size + uniqueUris.size
        val finalUris = if (totalCount > 10) {
            // 10장 제한 적용
            val availableSlots = 10 - selectedImageUris.size
            uniqueUris.take(availableSlots)
        } else {
            uniqueUris
        }

        selectedImageUris = selectedImageUris + finalUris

        // 성공 메시지
        if (finalUris.size < uniqueUris.size) {
            showMessage("${finalUris.size}장 추가됨 (10장 제한으로 일부 제외)")
        } else {
            showMessage("${finalUris.size}장의 사진을 추가했습니다")
        }
    }

    /**
     * 사진 제거
     */
    private fun removePhoto(uri: Uri) {
        selectedImageUris = selectedImageUris.filter { it != uri }
    }

    /**
     * 공유된 이미지와 기존 선택 이미지 합치기 (중복 제거)
     */
    private fun mergeSharedImages(sharedUris: List<Uri>) {
        if (sharedUris.isEmpty()) return

        val uniqueSharedUris = sharedUris.filter { sharedUri ->
            !selectedImageUris.contains(sharedUri)
        }

        if (uniqueSharedUris.isNotEmpty()) {
            selectedImageUris = (selectedImageUris + uniqueSharedUris).take(10)
            showMessage("공유된 ${uniqueSharedUris.size}장의 사진을 추가했습니다")
        } else {
            showMessage("공유된 사진이 이미 선택되어 있습니다")
        }
    }

    /**
     * 중복 메시지 표시
     */
    private fun showDuplicateMessage(message: String) {
        lifecycleScope.launch {
            Timber.i("중복 체크: $message")
            // TODO: 실제 앱에서는 스낵바나 토스트로 표시
        }
    }

    /**
     * 일반 메시지 표시
     */
    private fun showMessage(message: String) {
        lifecycleScope.launch {
            Timber.i("사진 선택: $message")
            // TODO: 실제 앱에서는 스낵바나 토스트로 표시
        }
    }

    // =============== UI 컴포저블 함수들 ===============

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
                        // 공유된 이미지가 있으면 먼저 합치기
                        LaunchedEffect(sharedImageUris, isFromShare) {
                            if (isFromShare && sharedImageUris.isNotEmpty()) {
                                mergeSharedImages(sharedImageUris)
                                // 공유 상태 초기화
                                isFromShare = false
                                sharedImageUris = emptyList()
                            }
                        }

                        MainScreen(
                            initialImageUris = if (isFromShare) sharedImageUris else selectedImageUris,
                            isFromShare = isFromShare,
                            onAddMorePhotos = ::addMorePhotos,
                            onStartNewMemory = ::startNewMemory,
                            onRemovePhoto = ::removePhoto
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
     * 로딩 화면 컴포저블
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

    // =============== 초기화 및 설정 함수들 ===============

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
     * 앱 사용 가능 여부 판단
     */
    private fun canUseApp(): Boolean {
        return true // 권한 없어도 기본 기능 사용 가능
    }

    // =============== 권한 관련 함수들 ===============

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
}

/**
 * 앱 테마 설정
 */
data class EllipsisAppTheme(
    val isDarkMode: Boolean,
    val useDynamicColors: Boolean,
    val primaryColor: Color
)