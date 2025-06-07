package com.example.ellipsis.presentation.main

import android.content.Context
import android.location.Geocoder
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.ellipsis.data.model.toTrackInfo
import com.example.ellipsis.data.model.getColorPalette
import com.example.ellipsis.data.model.toEmotionAnalysis
import com.example.ellipsis.data.remote.GPTApiService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import java.lang.reflect.Field
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val api: GPTApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    // 저장된 기억들 (실제 앱에서는 데이터베이스 사용)
    private val savedMemories = mutableListOf<MemoryEntry>()

    // =============== EXIF 완전 추출기 ===============

    /**
     * ExifInterface의 모든 TAG_ 상수들을 자동으로 가져오기 (리플렉션 사용)
     */
    private fun getAllExifTags(): List<String> {
        val tags = mutableListOf<String>()

        try {
            val exifClass = ExifInterface::class.java
            val fields: Array<Field> = exifClass.declaredFields

            for (field in fields) {
                // TAG_로 시작하는 public static final String 필드들만 가져오기
                if (field.name.startsWith("TAG_") &&
                    field.type == String::class.java &&
                    java.lang.reflect.Modifier.isStatic(field.modifiers) &&
                    java.lang.reflect.Modifier.isFinal(field.modifiers)) {

                    try {
                        val tagValue = field.get(null) as String
                        tags.add(tagValue)
                    } catch (e: Exception) {
                        Timber.v("TAG 접근 실패: ${field.name} - ${e.message}")
                    }
                }
            }

            Timber.i("🔍 총 ${tags.size}개의 EXIF 태그 발견 (리플렉션)")

        } catch (e: Exception) {
            Timber.e(e, "❌ EXIF 태그 리플렉션 실패, 백업 태그 사용")
            // 백업: 알려진 주요 태그들 사용
            return getKnownExifTags()
        }

        return tags.distinct() // 중복 제거
    }

    /**
     * 알려진 주요 EXIF 태그들 (백업용 - 리플렉션 실패시)
     */
    private fun getKnownExifTags(): List<String> {
        return listOf(
            // === 카메라 기본 정보 ===
            ExifInterface.TAG_MAKE,                    // 제조사
            ExifInterface.TAG_MODEL,                   // 모델명
            ExifInterface.TAG_SOFTWARE,                // 소프트웨어
            ExifInterface.TAG_DATETIME,                // 파일 생성 시간
            ExifInterface.TAG_DATETIME_ORIGINAL,       // 원본 촬영 시간
            ExifInterface.TAG_DATETIME_DIGITIZED,      // 디지털화 시간
            ExifInterface.TAG_ARTIST,                  // 작가/사진가
            ExifInterface.TAG_COPYRIGHT,               // 저작권

            // === 이미지 기본 정보 ===
            ExifInterface.TAG_IMAGE_WIDTH,             // 이미지 너비
            ExifInterface.TAG_IMAGE_LENGTH,            // 이미지 높이
            ExifInterface.TAG_BITS_PER_SAMPLE,         // 샘플당 비트수
            ExifInterface.TAG_COMPRESSION,             // 압축 방식
            ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION, // 색상 해석
            ExifInterface.TAG_ORIENTATION,             // 회전 정보
            ExifInterface.TAG_SAMPLES_PER_PIXEL,       // 픽셀당 샘플수
            ExifInterface.TAG_X_RESOLUTION,            // X 해상도
            ExifInterface.TAG_Y_RESOLUTION,            // Y 해상도
            ExifInterface.TAG_RESOLUTION_UNIT,         // 해상도 단위

            // === 촬영 설정 ===
            ExifInterface.TAG_EXPOSURE_TIME,           // 노출 시간
            ExifInterface.TAG_F_NUMBER,                // 조리개 값
            ExifInterface.TAG_EXPOSURE_PROGRAM,        // 노출 프로그램
            ExifInterface.TAG_ISO_SPEED_RATINGS,       // ISO 감도
            ExifInterface.TAG_SHUTTER_SPEED_VALUE,     // 셔터 속도 값
            ExifInterface.TAG_APERTURE_VALUE,          // 조리개 값
            ExifInterface.TAG_BRIGHTNESS_VALUE,        // 밝기 값
            ExifInterface.TAG_EXPOSURE_BIAS_VALUE,     // 노출 보정 값
            ExifInterface.TAG_MAX_APERTURE_VALUE,      // 최대 조리개 값
            ExifInterface.TAG_SUBJECT_DISTANCE,        // 피사체 거리
            ExifInterface.TAG_METERING_MODE,           // 측광 모드
            ExifInterface.TAG_LIGHT_SOURCE,            // 광원
            ExifInterface.TAG_FLASH,                   // 플래시
            ExifInterface.TAG_FOCAL_LENGTH,            // 초점 거리
            ExifInterface.TAG_SUBJECT_AREA,            // 피사체 영역
            ExifInterface.TAG_MAKER_NOTE,              // 제조사 노트
            ExifInterface.TAG_USER_COMMENT,            // 사용자 코멘트
            ExifInterface.TAG_SUBSEC_TIME,             // 서브초
            ExifInterface.TAG_FLASHPIX_VERSION,        // FlashPix 버전
            ExifInterface.TAG_COLOR_SPACE,             // 색공간
            ExifInterface.TAG_PIXEL_X_DIMENSION,       // 픽셀 X 차원
            ExifInterface.TAG_PIXEL_Y_DIMENSION,       // 픽셀 Y 차원
            ExifInterface.TAG_RELATED_SOUND_FILE,      // 관련 사운드 파일
            ExifInterface.TAG_FLASH_ENERGY,            // 플래시 에너지
            ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION, // 초점면 X 해상도
            ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION, // 초점면 Y 해상도
            ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT, // 초점면 해상도 단위
            ExifInterface.TAG_SUBJECT_LOCATION,        // 피사체 위치
            ExifInterface.TAG_EXPOSURE_INDEX,          // 노출 지수
            ExifInterface.TAG_SENSING_METHOD,          // 센싱 방법
            ExifInterface.TAG_FILE_SOURCE,             // 파일 소스
            ExifInterface.TAG_SCENE_TYPE,              // 장면 타입
            ExifInterface.TAG_CUSTOM_RENDERED,         // 커스텀 렌더링
            ExifInterface.TAG_EXPOSURE_MODE,           // 노출 모드
            ExifInterface.TAG_WHITE_BALANCE,           // 화이트 밸런스
            ExifInterface.TAG_DIGITAL_ZOOM_RATIO,      // 디지털 줌 비율
            ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, // 35mm 환산 초점거리
            ExifInterface.TAG_SCENE_CAPTURE_TYPE,      // 장면 캡처 타입
            ExifInterface.TAG_GAIN_CONTROL,            // 게인 제어
            ExifInterface.TAG_CONTRAST,                // 대비
            ExifInterface.TAG_SATURATION,              // 채도
            ExifInterface.TAG_SHARPNESS,               // 선명도
            ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION, // 장치 설정 설명
            ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,  // 피사체 거리 범위

            // === GPS 정보 ===
            ExifInterface.TAG_GPS_VERSION_ID,          // GPS 버전
            ExifInterface.TAG_GPS_LATITUDE_REF,        // 위도 참조
            ExifInterface.TAG_GPS_LATITUDE,            // 위도
            ExifInterface.TAG_GPS_LONGITUDE_REF,       // 경도 참조
            ExifInterface.TAG_GPS_LONGITUDE,           // 경도
            ExifInterface.TAG_GPS_ALTITUDE_REF,        // 고도 참조
            ExifInterface.TAG_GPS_ALTITUDE,            // 고도
            ExifInterface.TAG_GPS_TIMESTAMP,           // GPS 타임스탬프
            ExifInterface.TAG_GPS_SATELLITES,          // GPS 위성
            ExifInterface.TAG_GPS_STATUS,              // GPS 상태
            ExifInterface.TAG_GPS_MEASURE_MODE,        // GPS 측정 모드
            ExifInterface.TAG_GPS_DOP,                 // GPS DOP
            ExifInterface.TAG_GPS_SPEED_REF,           // GPS 속도 참조
            ExifInterface.TAG_GPS_SPEED,               // GPS 속도
            ExifInterface.TAG_GPS_TRACK_REF,           // GPS 트랙 참조
            ExifInterface.TAG_GPS_TRACK,               // GPS 트랙
            ExifInterface.TAG_GPS_IMG_DIRECTION_REF,   // GPS 이미지 방향 참조
            ExifInterface.TAG_GPS_IMG_DIRECTION,       // GPS 이미지 방향
            ExifInterface.TAG_GPS_MAP_DATUM,           // GPS 지도 기준점
            ExifInterface.TAG_GPS_DEST_LATITUDE_REF,   // GPS 목적지 위도 참조
            ExifInterface.TAG_GPS_DEST_LATITUDE,       // GPS 목적지 위도
            ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,  // GPS 목적지 경도 참조
            ExifInterface.TAG_GPS_DEST_LONGITUDE,      // GPS 목적지 경도
            ExifInterface.TAG_GPS_DEST_BEARING_REF,    // GPS 목적지 방위 참조
            ExifInterface.TAG_GPS_DEST_BEARING,        // GPS 목적지 방위
            ExifInterface.TAG_GPS_DEST_DISTANCE_REF,   // GPS 목적지 거리 참조
            ExifInterface.TAG_GPS_DEST_DISTANCE,       // GPS 목적지 거리
            ExifInterface.TAG_GPS_PROCESSING_METHOD,   // GPS 처리 방법
            ExifInterface.TAG_GPS_AREA_INFORMATION,    // GPS 지역 정보
            ExifInterface.TAG_GPS_DATESTAMP,           // GPS 날짜 스탬프
            ExifInterface.TAG_GPS_DIFFERENTIAL,        // GPS 차등

            // === 렌즈 정보 (Android 7.1+) ===
            ExifInterface.TAG_LENS_MAKE,               // 렌즈 제조사
            ExifInterface.TAG_LENS_MODEL,              // 렌즈 모델
            ExifInterface.TAG_LENS_SERIAL_NUMBER,      // 렌즈 시리얼 번호
            ExifInterface.TAG_LENS_SPECIFICATION,      // 렌즈 사양

            // === 고급 카메라 정보 ===
            ExifInterface.TAG_BODY_SERIAL_NUMBER,      // 바디 시리얼 번호
            ExifInterface.TAG_CAMERA_OWNER_NAME,       // 카메라 소유자 이름
            ExifInterface.TAG_IMAGE_UNIQUE_ID,         // 이미지 고유 ID

            // === 색상 및 화이트 밸런스 ===
            ExifInterface.TAG_WHITE_POINT,             // 화이트 포인트
            ExifInterface.TAG_PRIMARY_CHROMATICITIES,  // 주 색도
            ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,    // YCbCr 계수
            ExifInterface.TAG_REFERENCE_BLACK_WHITE,   // 참조 흑백
            ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,    // 색상 서브샘플링
            ExifInterface.TAG_Y_CB_CR_POSITIONING,     // 색상 위치

            // === 썸네일 정보 ===
            ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,  // 썸네일 이미지 길이
            ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,   // 썸네일 이미지 너비

            // === 기타 메타데이터 ===
            ExifInterface.TAG_EXIF_VERSION,            // EXIF 버전
            ExifInterface.TAG_COMPONENTS_CONFIGURATION, // 구성 요소 구성
            ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL, // 압축된 픽셀당 비트
            ExifInterface.TAG_INTEROPERABILITY_INDEX,  // 상호 운용성 인덱스
            ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT, // JPEG 교환 형식
            ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, // JPEG 교환 형식 길이
            ExifInterface.TAG_TRANSFER_FUNCTION,       // 전송 함수
            ExifInterface.TAG_PLANAR_CONFIGURATION,    // 평면 구성
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,    // 원본 서브초
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,   // 디지털화 서브초
        )
    }

    /**
     * 중요한 태그인지 확인 (로깅용)
     */
    private fun isImportantTag(tag: String): Boolean {
        return tag in setOf(
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_LENS_MAKE,
            ExifInterface.TAG_LENS_MODEL,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_ISO_SPEED_RATINGS,
            ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_WHITE_BALANCE,
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_COLOR_SPACE,
            ExifInterface.TAG_CONTRAST,
            ExifInterface.TAG_SATURATION,
            ExifInterface.TAG_SHARPNESS
        )
    }

    // =============== 기존 Public 함수들 ===============

    /**
     * 사진이 선택되었을 때 처리
     * - 이미지 로드 (분석은 서버에서)
     * - 완전한 EXIF 데이터 추출
     */
    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    isAnalyzing = true,
                    errorMessage = null
                )

                // 이미지 로드 (표시용만)
                val bitmap = loadImageBitmap(uri, context)
                if (bitmap == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAnalyzing = false,
                        errorMessage = "이미지를 불러올 수 없습니다"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    bitmap = bitmap
                )

                // 🎯 완전한 EXIF 데이터 추출
                processCompleteExifData(uri, context)

                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    isLoading = false,
                    successMessage = "사진을 불러왔습니다. 음악 추천을 요청해보세요!"
                )

                // 성공 메시지 자동 제거
                delay(2000)
                _uiState.value = _uiState.value.copy(successMessage = null)

            } catch (e: Exception) {
                Timber.e(e, "❌ 이미지 처리 오류")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAnalyzing = false,
                    errorMessage = "이미지 처리 실패: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * 음악 추천 요청 - 서버에서 모든 분석 처리
     */
    fun recommendMusic(context: Context, uris: List<Uri>, hashtags: String) {
        if (uris.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    isMusicLoading = true,
                    errorMessage = null,
                    currentStep = ProcessingStep.PREPARING
                )

                // 첫 번째 이미지의 비트맵 가져오기
                val bitmap = loadImageBitmap(uris.first(), context)
                bitmap?.let {
                    _uiState.value = _uiState.value.copy(bitmap = it)
                }

                // 간단한 객체 인식만 (옵션)
                val detectedObjects = if (bitmap != null) {
                    detectObjects(bitmap)
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    detectedObjects = detectedObjects,
                    currentStep = ProcessingStep.ANALYZING_EMOTIONS,
                    processingProgress = 0.3f
                )

                // 🎯 완전한 EXIF 데이터 추출 (첫 번째 이미지)
                processCompleteExifData(uris.first(), context)

                // 서버에 음악 추천 요청
                val response = requestMusicRecommendation(uris.first(), context, hashtags, detectedObjects)

                // 서버 응답으로부터 UI 업데이트
                updateUIFromServerResponse(response, hashtags, uris.first())

                // 음표 애니메이션 시작
                animateNotes()

                Timber.i("✅ 음악 추천 성공: ${response.emotion}, 곡 수: ${response.tracks.size}")

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is retrofit2.HttpException -> {
                        "서버 연결 실패 (${e.code()}): 잠시 후 다시 시도해주세요"
                    }
                    else -> "음악 추천 실패: ${e.localizedMessage}"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isMusicLoading = false,
                    errorMessage = errorMsg,
                    currentStep = ProcessingStep.ERROR
                )
                Timber.e(e, "음악 추천 실패")
            }
        }
    }

    /**
     * 에러 메시지 지우기
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 메모리 다이어리 표시/숨김
     */
    fun toggleMemoryDiary() {
        _uiState.value = _uiState.value.copy(
            showMemoryDiary = !_uiState.value.showMemoryDiary,
            savedMemories = savedMemories.toList()
        )
    }

    /**
     * 저장된 기억 선택
     */
    fun selectMemory(memoryId: String) {
        val memory = savedMemories.find { it.id == memoryId }
        memory?.let {
            _uiState.value = _uiState.value.copy(
                currentMemoryId = memoryId,
                bitmap = it.thumbnailBitmap,
                recommendedSongs = it.songs.map { song -> "${song.artist} - ${song.title}" },
                emotion = it.emotion,
                emotionDescription = it.emotionDescription,
                hashtags = it.hashtags.joinToString(" "),
                locationName = it.location,
                photoDate = it.photoDate,
                primaryColor = it.dominantColor
            )

            // 색상 전환 애니메이션
            animateColorTransition()
        }
    }

    // =============== Private Helper Functions ===============

    /**
     *  완전한 EXIF 데이터 추출 (프라이버시 필터링 포함)
     */
    private suspend fun processCompleteExifData(uri: Uri, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                Timber.i("완전한 EXIF 데이터 추출 시작...")
                val startTime = System.currentTimeMillis()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val exif = ExifInterface(inputStream)
                    val exifMap = mutableMapOf<String, Any?>()

                    // 모든 EXIF 태그 가져오기
                    val allTags = getAllExifTags()
                    Timber.i("${allTags.size}개의 EXIF 태그 발견")

                    // 프라이버시 필터링 옵션 (사용자 설정에 따라)
                    val privacyLevel = getPrivacyLevel() // "none", "basic", "strict"
                    val filteredTags = when (privacyLevel) {
                        "none" -> allTags // 모든 태그 사용
                        "basic" -> filterBasicPrivacy(allTags) // 기본 민감 정보 제외
                        "strict" -> filterStrictPrivacy(allTags) // 엄격한 필터링
                        else -> allTags
                    }

                    Timber.i("프라이버시 필터링($privacyLevel): ${allTags.size}개 → ${filteredTags.size}개 태그")

                    var extractedCount = 0
                    var totalSize = 0
                    var filteredCount = 0

                    // 필터링된 태그들에 대해 값 추출
                    for (tag in filteredTags) {
                        try {
                            val value = exif.getAttribute(tag)
                            if (value != null && value.isNotBlank()) {
                                exifMap[tag] = value
                                extractedCount++
                                totalSize += value.length

                                // 중요한 태그들은 로깅
                                if (isImportantTag(tag)) {
                                    Timber.d("📋 $tag: $value")
                                }
                            }
                        } catch (e: Exception) {
                            // 일부 태그는 접근 불가능할 수 있음
                            Timber.v("태그 접근 실패: $tag - ${e.message}")
                        }
                    }

                    filteredCount = allTags.size - filteredTags.size

                    // GPS 위치 정보 특별 처리 (프라이버시 설정 고려)
                    val latLong = FloatArray(2)
                    var lat: Double? = null
                    var lon: Double? = null
                    var location = ""

                    if (privacyLevel != "strict" && exif.getLatLong(latLong)) {
                        lat = latLong[0].toDouble()
                        lon = latLong[1].toDouble()

                        // 위치 정확도 조정 (프라이버시 보호)
                        if (privacyLevel == "basic") {
                            // 기본 모드: 소수점 3자리까지 (약 100m 정확도)
                            lat = (lat * 1000).toInt() / 1000.0
                            lon = (lon * 1000).toInt() / 1000.0
                        }

                        // 계산된 GPS 정보 추가
                        exifMap["computed_latitude"] = lat
                        exifMap["computed_longitude"] = lon
                        extractedCount += 2

                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0]
                                location = when (privacyLevel) {
                                    "basic" -> {
                                        // 기본 모드: 시/도 레벨까지만
                                        address.adminArea ?: address.countryName ?: "알 수 없는 지역"
                                    }
                                    "none" -> {
                                        // 모든 정보: 상세 주소
                                        when {
                                            !address.locality.isNullOrEmpty() -> address.locality!!
                                            !address.adminArea.isNullOrEmpty() -> address.adminArea!!
                                            !address.countryName.isNullOrEmpty() -> address.countryName!!
                                            else -> "알 수 없는 위치"
                                        }
                                    }
                                    else -> "위치 정보 비활성화"
                                }
                                exifMap["computed_address"] = location
                                extractedCount++
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "지오코딩 실패")
                            location = "위치 정보 처리 실패"
                            exifMap["computed_address_error"] = e.localizedMessage
                        }
                    }

                    // 회전 정보 특별 처리
                    val rotationDegrees = exif.rotationDegrees
                    if (rotationDegrees != 0) {
                        exifMap["computed_rotation_degrees"] = rotationDegrees
                        extractedCount++
                    }

                    // 이미지 크기 특별 처리
                    val imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                    val imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
                    if (imageWidth > 0 && imageHeight > 0) {
                        exifMap["computed_image_width"] = imageWidth
                        exifMap["computed_image_height"] = imageHeight
                        exifMap["computed_aspect_ratio"] = imageWidth.toFloat() / imageHeight.toFloat()
                        exifMap["computed_megapixels"] = (imageWidth * imageHeight / 1000000.0f)
                        extractedCount += 4
                    }

                    // 처리 시간 계산
                    val processingTime = System.currentTimeMillis() - startTime

                    // 메타데이터 통계 추가
                    exifMap["metadata_stats"] = mapOf(
                        "total_tags_available" to allTags.size,
                        "filtered_tags_count" to filteredTags.size,
                        "privacy_filtered_count" to filteredCount,
                        "extracted_tags_count" to extractedCount,
                        "extraction_timestamp" to System.currentTimeMillis(),
                        "total_data_size_chars" to totalSize,
                        "extraction_success_rate" to (extractedCount.toFloat() / filteredTags.size * 100).toInt(),
                        "processing_time_ms" to processingTime,
                        "privacy_level" to privacyLevel,
                        "extraction_method" to "privacy_filtered_reflection"
                    )

                    val exifJsonString = JSONObject(exifMap).toString()
                    val photoDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                        ?: formatCurrentDate()

                    // UI 상태 업데이트
                    _uiState.value = _uiState.value.copy(
                        photoDate = formatPhotoDate(photoDate),
                        latitude = lat,
                        longitude = lon,
                        locationName = location,
                        exifJson = exifJsonString
                    )

                    // 추출된 데이터 통계 로깅
                    Timber.i("📊 EXIF 추출 완료:")
                    Timber.i("  • 전체 태그: ${allTags.size}개")
                    Timber.i("  • 필터링 후: ${filteredTags.size}개 (${filteredCount}개 제외)")
                    Timber.i("  • 추출 성공: ${extractedCount}개 (성공률: ${(extractedCount.toFloat() / filteredTags.size * 100).toInt()}%)")
                    Timber.i("  • 데이터 크기: ${totalSize}자, JSON: ${exifJsonString.length}자")
                    Timber.i("  • 처리 시간: ${processingTime}ms")
                    Timber.i("  • 프라이버시: $privacyLevel 모드")

                    // 중요한 메타데이터 로깅
                    logImportantMetadata(JSONObject(exifMap))

                    Timber.d("🔒 프라이버시 필터링된 EXIF 처리 완료")

                } ?: run {
                    Timber.w("⚠️ 이미지 스트림 열기 실패")
                    _uiState.value = _uiState.value.copy(
                        exifJson = "{\"error\": \"이미지 스트림 열기 실패\"}"
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "❌ EXIF 처리 실패")
                _uiState.value = _uiState.value.copy(
                    exifJson = "{\"error\": \"EXIF 처리 실패: ${e.localizedMessage}\"}"
                )
            }
        }
    }

    /**
     * 프라이버시 레벨 설정 가져오기
     */
    private fun getPrivacyLevel(): String {
        // 실제로는 SharedPreferences나 사용자 설정에서 가져옴
        // 지금은 기본값 "basic" 사용
        return "basic" // "none", "basic", "strict"
    }

    /**
     * 기본 프라이버시 필터링 (권장)
     */
    private fun filterBasicPrivacy(allTags: List<String>): List<String> {
        val sensitiveTags = setOf(
            ExifInterface.TAG_CAMERA_OWNER_NAME,      // 카메라 소유자 이름
            ExifInterface.TAG_BODY_SERIAL_NUMBER,     // 바디 시리얼 번호
            ExifInterface.TAG_LENS_SERIAL_NUMBER,     // 렌즈 시리얼 번호
            ExifInterface.TAG_COPYRIGHT,              // 저작권 정보
            ExifInterface.TAG_ARTIST,                 // 작가 정보
            ExifInterface.TAG_SOFTWARE,               // 소프트웨어 정보 (일부)
            ExifInterface.TAG_IMAGE_UNIQUE_ID,        // 이미지 고유 ID
            ExifInterface.TAG_USER_COMMENT,           // 사용자 코멘트
        )

        return allTags.filter { tag -> tag !in sensitiveTags }
    }

    /**
     * 엄격한 프라이버시 필터링
     */
    private fun filterStrictPrivacy(allTags: List<String>): List<String> {
        // 엄격 모드: GPS, 개인정보, 기기정보 모두 제외
        val allowedTagPrefixes = setOf(
            "Exposure", "FNumber", "ISO", "Flash", "FocalLength",
            "WhiteBalance", "Contrast", "Saturation", "Sharpness",
            "ColorSpace", "DateTime", "Orientation", "ImageWidth", "ImageLength"
        )

        return allTags.filter { tag ->
            allowedTagPrefixes.any { prefix ->
                tag.contains(prefix, ignoreCase = true)
            } && !tag.contains("GPS", ignoreCase = true)
                    && !tag.contains("Serial", ignoreCase = true)
                    && !tag.contains("Owner", ignoreCase = true)
                    && !tag.contains("Copyright", ignoreCase = true)
                    && !tag.contains("Artist", ignoreCase = true)
                    && !tag.contains("Software", ignoreCase = true)
                    && !tag.contains("Computer", ignoreCase = true)
        }
    }

    /**
     * 중요한 메타데이터 로깅
     */
    private fun logImportantMetadata(jsonObject: JSONObject) {
        try {
            val importantTags = mapOf(
                "카메라 제조사" to ExifInterface.TAG_MAKE,
                "카메라 모델" to ExifInterface.TAG_MODEL,
                "렌즈 제조사" to ExifInterface.TAG_LENS_MAKE,
                "렌즈 모델" to ExifInterface.TAG_LENS_MODEL,
                "촬영 시간" to ExifInterface.TAG_DATETIME_ORIGINAL,
                "ISO 감도" to ExifInterface.TAG_ISO_SPEED_RATINGS,
                "조리개 값" to ExifInterface.TAG_F_NUMBER,
                "노출 시간" to ExifInterface.TAG_EXPOSURE_TIME,
                "초점 거리" to ExifInterface.TAG_FOCAL_LENGTH,
                "35mm 환산" to ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
                "플래시 사용" to ExifInterface.TAG_FLASH,
                "화이트 밸런스" to ExifInterface.TAG_WHITE_BALANCE,
                "이미지 방향" to ExifInterface.TAG_ORIENTATION,
                "색공간" to ExifInterface.TAG_COLOR_SPACE,
                "대비" to ExifInterface.TAG_CONTRAST,
                "채도" to ExifInterface.TAG_SATURATION,
                "선명도" to ExifInterface.TAG_SHARPNESS
            )

            Timber.i("📷 중요 EXIF 정보:")
            importantTags.forEach { (name, tag) ->
                val value = jsonObject.optString(tag, null)
                if (!value.isNullOrBlank()) {
                    Timber.i("  • $name: $value")
                }
            }

            // GPS 정보 로깅
            val computedLat = jsonObject.optDouble("computed_latitude", Double.NaN)
            val computedLon = jsonObject.optDouble("computed_longitude", Double.NaN)
            if (!computedLat.isNaN() && !computedLon.isNaN()) {
                Timber.i("  • GPS 좌표: $computedLat, $computedLon")
            }

            // 이미지 정보 로깅
            val width = jsonObject.optInt("computed_image_width", 0)
            val height = jsonObject.optInt("computed_image_height", 0)
            val megapixels = jsonObject.optDouble("computed_megapixels", 0.0)
            if (width > 0 && height > 0) {
                Timber.i("  • 이미지 크기: ${width}x${height} (${String.format("%.1f", megapixels)}MP)")
            }

        } catch (e: Exception) {
            Timber.w("중요 메타데이터 로깅 실패: ${e.message}")
        }
    }

    private suspend fun loadImageBitmap(uri: Uri, context: Context): android.graphics.Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            (result as? SuccessResult)?.drawable?.toBitmap()
        } catch (e: Exception) {
            Timber.e(e, "이미지 로드 실패")
            null
        }
    }

    private suspend fun detectObjects(bitmap: android.graphics.Bitmap): List<String> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val detector = ObjectDetection.getClient(
                ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()
                    .build()
            )

            val results = withContext(Dispatchers.IO) {
                detector.process(image).await()
            }

            results.flatMap { it.labels.map { label -> label.text } }
                .distinct()
                .take(5) // 최대 5개 객체

        } catch (e: Exception) {
            Timber.e(e, "객체 인식 실패")
            emptyList()
        }
    }

    /**
     * 🎯 서버 요청 - 새로운 단순화된 API에 맞게 수정
     */
    private suspend fun requestMusicRecommendation(
        uri: Uri,
        context: Context,
        hashtags: String,
        detectedObjects: List<String>
    ) = withContext(Dispatchers.IO) {
        val fileBytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: throw IllegalStateException("파일을 읽을 수 없습니다")

        // 단일 이미지 파트 생성 (새 API는 image 파라미터 하나만 받음)
        val fileBody = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", fileBody)

        val currentExifJson = _uiState.value.exifJson

        Timber.d("🎵 음악 추천 요청 (단순화된 API):\n" +
                "hashtags = $hashtags\n" +
                "objects = ${detectedObjects.joinToString(",")}\n" +
                "location = ${_uiState.value.locationName}\n" +
                "exif_size = ${currentExifJson.length}자")

        // 🎯 새로운 단순화된 API 호출 (직접 RequestBody 생성)
        api.recommendMusic(
            image = imagePart,
            hashtags = hashtags.toRequestBody("text/plain".toMediaTypeOrNull()),
            culturalContext = "korean".toRequestBody("text/plain".toMediaTypeOrNull()),
            maxSongs = "8".toRequestBody("text/plain".toMediaTypeOrNull()),
            facialAnalysisWeight = "0.6".toRequestBody("text/plain".toMediaTypeOrNull())
        )
    }

    /**
     * 서버 응답으로부터 UI 상태 업데이트
     */
    private fun updateUIFromServerResponse(
        response: com.example.ellipsis.data.model.MusicResponse,
        hashtags: String,
        uri: Uri
    ) {
        // 트랙 정보 변환
        val tracks = response.tracks.mapIndexed { index, track ->
            track.toTrackInfo(index, response.tracks.size)
        }

        // 서버에서 제공한 색상 팔레트 사용
        val colors = response.visualElements?.getColorPalette() ?: listOf(Color(0xFF6366F1))
        val primaryColor = colors.firstOrNull() ?: Color(0xFF6366F1)
        val secondaryColor = colors.getOrNull(1) ?: Color(0xFF8B5CF6)

        _uiState.value = _uiState.value.copy(
            // 음악 정보
            recommendedSongs = response.tracks.map { "${it.artist} - ${it.title}" },
            emotion = response.emotion,
            emotionDescription = response.description,
            emotionKeywords = response.metadata?.emotion_keywords ?: emptyList(),

            // 서버 분석 기반 색상
            dominantColors = colors,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,

            // 메타데이터
            serverMetadata = ServerMetadata(
                emotionKeywords = response.metadata?.emotion_keywords ?: emptyList(),
                objectsDetected = response.metadata?.objects_detected ?: _uiState.value.detectedObjects,
                location = response.metadata?.location,
                datetime = response.metadata?.datetime,
                processedImages = response.metadata?.processed_images ?: 1,
                confidence = response.emotionAnalysis?.confidence ?: 0.0f,
                processingTime = response.processingInfo?.processingTimeMs ?: 0L,
                apiVersion = response.processingInfo?.apiVersion ?: "1.0"
            ),

            // 추가 분석 결과
            extractedKeywords = response.metadata?.emotion_keywords ?: emptyList(),
            emotionalDescription = response.description,
            mood = response.emotion,
            confidence = response.emotionAnalysis?.confidence ?: 0.0f,

            // 상태 업데이트
            isLoading = false,
            isMusicLoading = false,
            currentStep = ProcessingStep.COMPLETED,
            processingProgress = 1.0f
        )

        // 메모리로 저장
        saveCurrentMemory(uri, tracks, hashtags, primaryColor)

        // 색상 전환 애니메이션
        animateColorTransition()
    }

    private fun saveCurrentMemory(
        uri: Uri,
        tracks: List<TrackInfo>,
        hashtags: String,
        dominantColor: Color
    ) {
        val currentState = _uiState.value
        val memory = MemoryEntry(
            photoUri = uri.toString(),
            thumbnailBitmap = currentState.bitmap,
            songs = tracks,
            emotion = currentState.emotion,
            emotionDescription = currentState.emotionDescription,
            hashtags = hashtags.split(" ").filter { it.startsWith("#") },
            location = currentState.locationName,
            photoDate = currentState.photoDate,
            dominantColor = dominantColor,
            timestamp = System.currentTimeMillis()
        )

        savedMemories.add(0, memory) // 최신순 정렬

        // 최대 100개 기억만 보관
        if (savedMemories.size > 100) {
            savedMemories.removeAt(savedMemories.size - 1)
        }

        Timber.d("메모리 저장 완료: ${memory.emotion} - ${tracks.size}곡")
    }

    // 애니메이션 관련 함수들
    private fun animateColorTransition() {
        viewModelScope.launch {
            for (i in 0..100 step 5) {
                _uiState.value = _uiState.value.copy(
                    colorTransitionProgress = i / 100f
                )
                delay(50)
            }
        }
    }

    private fun animateNotes() {
        viewModelScope.launch {
            // 음표 등장 애니메이션
            for (i in 0..100 step 10) {
                _uiState.value = _uiState.value.copy(
                    noteAnimationProgress = i / 100f
                )
                delay(100)
            }

            delay(500)

            // 연결선 애니메이션
            for (i in 0..100 step 15) {
                _uiState.value = _uiState.value.copy(
                    connectingLineProgress = i / 100f
                )
                delay(80)
            }
        }
    }

    // Utility 함수들
    private fun formatCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
            .format(Date())
    }

    private fun formatPhotoDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREAN)
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}