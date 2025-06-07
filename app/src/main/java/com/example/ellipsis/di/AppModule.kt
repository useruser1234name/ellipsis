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
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            .setFieldNamingStrategy { field ->
                field.name.replace(Regex("([a-z])([A-Z])")) { matchResult ->
                    "${matchResult.groupValues[1]}_${matchResult.groupValues[2].lowercase()}"
                }
            }
            .setPrettyPrinting()
            .create()
    }

    /**
     * HTTP 캐시 설정
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
     */
    @Provides
    @Singleton
    @LoggingInterceptor
    fun provideLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * API 헤더 인터셉터 (개선됨)
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
                .addHeader("X-Client-Version", "1.0.0")
                .addHeader("X-Platform", "Android")
                .addHeader("Connection", "keep-alive") // 연결 유지
                .build()

            chain.proceed(newRequest)
        }
    }

    /**
     * 에러 처리 인터셉터 (개선됨)
     */
    @Provides
    @Singleton
    @ErrorHandlingInterceptor
    fun provideErrorHandlingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()

            try {
                val response = chain.proceed(request)

                // 연결 상태 로깅 (ADB 포워딩 디버깅용)
                android.util.Log.d("API_CONNECTION",
                    "Request to: ${request.url} | Status: ${response.code}")

                if (!response.isSuccessful) {
                    when (response.code) {
                        429 -> android.util.Log.w("API", "Rate limit exceeded")
                        500, 502, 503, 504 -> android.util.Log.e("API", "Server error: ${response.code}")
                        else -> android.util.Log.w("API", "HTTP error: ${response.code}")
                    }
                }

                response
            } catch (e: Exception) {
                android.util.Log.e("API_CONNECTION", "Connection failed to: ${request.url}", e)
                throw e
            }
        }
    }

    /**
     * OkHttpClient 설정 (ADB 포워딩 최적화)
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
            // ADB 포워딩은 로컬 연결이므로 타임아웃 줄임
            .connectTimeout(10, TimeUnit.SECONDS)  // 기존 30초 → 10초
            .readTimeout(30, TimeUnit.SECONDS)     // 기존 60초 → 30초
            .writeTimeout(30, TimeUnit.SECONDS)    // 기존 60초 → 30초
            .callTimeout(60, TimeUnit.SECONDS)     // 전체 호출 타임아웃 추가

            // 캐시 설정
            .cache(cache)

            // 인터셉터 순서
            .addInterceptor(authInterceptor)
            .addInterceptor(errorHandlingInterceptor)
            .addNetworkInterceptor(loggingInterceptor)

            // 재시도 설정 (ADB 연결은 안정적이므로 줄임)
            .retryOnConnectionFailure(true)

            // Keep-Alive 설정 (ADB 연결 안정성 향상)
            .connectionPool(
                okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES)
            )
            .build()
    }

    /**
     * Retrofit 인스턴스 (ADB 포트 포워딩 사용)
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        // 🔧 개발 중에는 강제로 localhost 사용
        val baseUrl = "http://localhost:8000/"

        android.util.Log.i("RETROFIT_INIT", " Base URL: $baseUrl")
        android.util.Log.i("RETROFIT_INIT", " 강제 개발 모드 활성화")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
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
     * 개발용 연결 테스트 헬퍼
     */
    @Provides
    @Singleton
    fun provideConnectionTester(): ConnectionTester {
        return ConnectionTester()
    }

    // 나머지 기존 Provides 함수들...

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
            cacheExpiration = 24 * 60 * 60 * 1000L,
            enableDebugLogging = BuildConfig.DEBUG
        )
    }

    @Provides
    @Singleton
    fun provideUserPreferencesManager(@ApplicationContext context: Context): UserPreferencesManager {
        return UserPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideMemoryManager(@ApplicationContext context: Context): MemoryManager {
        return MemoryManager(context)
    }

    @Provides
    @Singleton
    fun provideAnalysisCacheManager(@ApplicationContext context: Context): AnalysisCacheManager {
        return AnalysisCacheManager(context)
    }
}

// =============== 연결 테스트 헬퍼 클래스 ===============

/**
 * ADB 포워딩 연결 테스트용 헬퍼
 */
class ConnectionTester {

    suspend fun testConnection(): ConnectionResult {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder()
                .url("http://localhost:8000/health") // 헬스 체크 엔드포인트
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                ConnectionResult.Success("ADB 포워딩 연결 성공!")
            } else {
                ConnectionResult.Error("서버 응답 오류: ${response.code}")
            }
        } catch (e: java.net.ConnectException) {
            ConnectionResult.Error("ADB 포워딩 설정되지 않음 또는 서버 미실행")
        } catch (e: Exception) {
            ConnectionResult.Error("연결 실패: ${e.localizedMessage}")
        }
    }
}

sealed class ConnectionResult {
    data class Success(val message: String) : ConnectionResult()
    data class Error(val message: String) : ConnectionResult()
}

// =============== 기존 Qualifier들 ===============

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ErrorHandlingInterceptor

// =============== 기존 설정 클래스들 ===============

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

    fun getFavoriteMemories(): List<String> {
        return prefs.getStringSet("favorite_memories", emptySet())?.toList() ?: emptyList()
    }

    fun addFavoriteMemory(memoryId: String) {
        val favorites = getFavoriteMemories().toMutableSet()
        favorites.add(memoryId)
        prefs.edit().putStringSet("favorite_memories", favorites).apply()
    }
}

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
        val expiration = 24 * 60 * 60 * 1000L
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