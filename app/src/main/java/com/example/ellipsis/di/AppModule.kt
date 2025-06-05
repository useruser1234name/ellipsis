package com.example.ellipsis.di

import android.content.Context
import com.example.ellipsis.data.remote.GPTApiService
import com.google.android.datatransport.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Gson 컨버터 설정
     * 날짜 포맷, null 처리, 필드 네이밍 정책 등 설정
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // ISO 8601 형식
            .serializeNulls() // null 값도 JSON에 포함
            .setFieldNamingStrategy { field ->
                // 카멜케이스를 스네이크케이스로 변환 (서버 호환성)
                field.name.replace(Regex("([a-z])([A-Z])")) { matchResult ->
                    "${matchResult.groupValues[1]}_${matchResult.groupValues[2].lowercase()}"
                }
            }
            .setPrettyPrinting() // 디버깅용 포맷팅
            .create()
    }

    /**
     * HTTP 캐시 설정
     * 이미지나 설정 정보 캐싱용
     */
    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 10 * 1024 * 1024 // 10MB
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, cacheSize.toLong())
    }

    /**
     * 로깅 인터셉터
     * 개발 환경에서만 활성화
     */
    @Provides
    @Singleton
    @LoggingInterceptor
    fun provideLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY // 개발: 전체 로깅
            } else {
                HttpLoggingInterceptor.Level.NONE // 프로덕션: 로깅 비활성화
            }
        }
    }

    /**
     * API 키 인터셉터 (추후 인증용)
     */
    @Provides
    @Singleton
    @AuthInterceptor
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Ellipsis-Android/1.0.0")
                // .addHeader("Authorization", "Bearer $API_KEY") // 추후 API 키 추가
                .addHeader("X-Client-Version", "1.0.0")
                .addHeader("X-Platform", "Android")
                .build()

            chain.proceed(newRequest)
        }
    }

    /**
     * 에러 처리 인터셉터
     * 네트워크 오류, 서버 오류 등을 일관되게 처리
     */
    @Provides
    @Singleton
    @ErrorHandlingInterceptor
    fun provideErrorHandlingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            try {
                val response = chain.proceed(request)

                // 서버 오류 응답 처리
                if (!response.isSuccessful) {
                    when (response.code) {
                        429 -> {
                            // Rate Limit - 잠시 대기 후 재시도 로직 추가 가능
                            android.util.Log.w("API", "Rate limit exceeded")
                        }
                        500, 502, 503, 504 -> {
                            // 서버 오류 - 재시도 로직 추가 가능
                            android.util.Log.e("API", "Server error: ${response.code}")
                        }
                    }
                }

                response
            } catch (e: Exception) {
                // 네트워크 오류 로깅
                android.util.Log.e("API", "Network error", e)
                throw e
            }
        }
    }

    /**
     * OkHttpClient 설정
     * 타임아웃, 인터셉터, 캐시 등 모든 네트워크 설정
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        @LoggingInterceptor loggingInterceptor: Interceptor,
        @AuthInterceptor authInterceptor: Interceptor,
        @ErrorHandlingInterceptor errorHandlingInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            // 타임아웃 설정 (이미지 업로드 고려)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS) // 이미지 분석 시간 고려
            .writeTimeout(60, TimeUnit.SECONDS) // 이미지 업로드 시간 고려

            // 캐시 설정
            .cache(cache)

            // 인터셉터 순서 중요!
            .addInterceptor(authInterceptor) // 1. 인증 헤더 추가
            .addInterceptor(errorHandlingInterceptor) // 2. 에러 처리
            .addNetworkInterceptor(loggingInterceptor) // 3. 로깅 (가장 마지막)

            // 재시도 설정
            .retryOnConnectionFailure(true)
            .build() // build() 메서드 위치 수정
    }

    /**
     * Retrofit 인스턴스
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.219.107:8000/") // 개발용 URL 직접 설정
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * GPT API 서비스
     */
    @Provides
    @Singleton
    fun provideGPTApiService(retrofit: Retrofit): GPTApiService {
        return retrofit.create(GPTApiService::class.java)
    }

    /**
     * 설정 관리자
     */
    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig {
        return AppConfig(
            enableColorAnalysis = true,
            enableEmotionAnalysis = true,
            enableObjectDetection = true,
            defaultMaxSongs = 8,
            defaultMoodIntensity = 0.5f,
            defaultVisualTheme = "auto",
            defaultCulturalContext = "korean",
            cacheExpiration = 24 * 60 * 60 * 1000L, // 24시간
            enableDebugLogging = BuildConfig.DEBUG
        )
    }

    /**
     * 사용자 설정 관리자
     */
    @Provides
    @Singleton
    fun provideUserPreferencesManager(@ApplicationContext context: Context): UserPreferencesManager {
        return UserPreferencesManager(context)
    }

    /**
     * 메모리 관리자
     */
    @Provides
    @Singleton
    fun provideMemoryManager(@ApplicationContext context: Context): MemoryManager {
        return MemoryManager(context)
    }

    /**
     * 분석 결과 캐시 관리자
     */
    @Provides
    @Singleton
    fun provideAnalysisCacheManager(@ApplicationContext context: Context): AnalysisCacheManager {
        return AnalysisCacheManager(context)
    }
}

// =============== Qualifier 어노테이션들 ===============

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ErrorHandlingInterceptor

// =============== 설정 및 관리자 클래스들 ===============

/**
 * 앱 전체 설정
 */
data class AppConfig(
    val enableColorAnalysis: Boolean,
    val enableEmotionAnalysis: Boolean,
    val enableObjectDetection: Boolean,
    val defaultMaxSongs: Int,
    val defaultMoodIntensity: Float,
    val defaultVisualTheme: String,
    val defaultCulturalContext: String,
    val cacheExpiration: Long,
    val enableDebugLogging: Boolean
)

/**
 * 사용자 설정 관리자
 */
class UserPreferencesManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    fun getPreferredGenres(): List<String> {
        return prefs.getStringSet("preferred_genres", emptySet())?.toList() ?: emptyList()
    }

    fun setPreferredGenres(genres: List<String>) {
        prefs.edit().putStringSet("preferred_genres", genres.toSet()).apply()
    }

    fun getMoodSensitivity(): Float {
        return prefs.getFloat("mood_sensitivity", 0.5f)
    }

    fun setMoodSensitivity(sensitivity: Float) {
        prefs.edit().putFloat("mood_sensitivity", sensitivity).apply()
    }

    fun getVisualThemePreference(): String {
        return prefs.getString("visual_theme", "auto") ?: "auto"
    }

    fun setVisualThemePreference(theme: String) {
        prefs.edit().putString("visual_theme", theme).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    fun setUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean("is_first_launch", false).apply()
    }
}

/**
 * 메모리(기억) 관리자
 */
class MemoryManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("memories", Context.MODE_PRIVATE)

    fun saveMemoryCount(count: Int) {
        prefs.edit().putInt("memory_count", count).apply()
    }

    fun getMemoryCount(): Int {
        return prefs.getInt("memory_count", 0)
    }

    fun getLastMemoryDate(): Long {
        return prefs.getLong("last_memory_date", 0L)
    }

    fun setLastMemoryDate(timestamp: Long) {
        prefs.edit().putLong("last_memory_date", timestamp).apply()
    }

    // 추후 SQLite 데이터베이스로 확장 가능
    fun getFavoriteMemories(): List<String> {
        return prefs.getStringSet("favorite_memories", emptySet())?.toList() ?: emptyList()
    }

    fun addFavoriteMemory(memoryId: String) {
        val favorites = getFavoriteMemories().toMutableSet()
        favorites.add(memoryId)
        prefs.edit().putStringSet("favorite_memories", favorites).apply()
    }
}

/**
 * 분석 결과 캐시 관리자
 */
class AnalysisCacheManager(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "analysis_cache")

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    fun getCachedResult(imageHash: String): String? {
        val cacheFile = File(cacheDir, "$imageHash.json")
        return if (cacheFile.exists() && !isExpired(cacheFile)) {
            cacheFile.readText()
        } else {
            null
        }
    }

    fun cacheResult(imageHash: String, result: String) {
        val cacheFile = File(cacheDir, "$imageHash.json")
        cacheFile.writeText(result)
    }

    private fun isExpired(file: File): Boolean {
        val expiration = 24 * 60 * 60 * 1000L // 24시간
        return (System.currentTimeMillis() - file.lastModified()) > expiration
    }

    fun clearExpiredCache() {
        cacheDir.listFiles()?.forEach { file ->
            if (isExpired(file)) {
                file.delete()
            }
        }
    }

    fun clearAllCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}