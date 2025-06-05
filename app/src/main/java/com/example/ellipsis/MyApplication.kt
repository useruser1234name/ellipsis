package com.example.ellipsis

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Ellipsis 애플리케이션 클래스 (최적화된 버전)
 *
 * 주요 기능:
 * - Hilt 의존성 주입 초기화
 * - Timber 로깅 설정 (간소화)
 * - WorkManager 설정 (필요시)
 * - 전역 예외 처리
 * - 메모리 최적화
 */
@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (isDebugMode()) android.util.Log.DEBUG
                else android.util.Log.ERROR
            )
            .build()

    override fun onCreate() {
        super.onCreate()

        // 로깅 시스템 초기화 (간소화)
        initializeLogging()

        // WorkManager 초기화 (필요한 경우에만)
        if (needsWorkManager()) {
            initializeWorkManager()
        }

        // 전역 예외 처리 설정 (간소화)
        setupGlobalExceptionHandler()

        Timber.i("=== Ellipsis 앱 시작 ===")
        Timber.i("앱 초기화 완료")
    }

    /**
     * 디버그 모드 체크
     */
    private fun isDebugMode(): Boolean {
        return try {
            this.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Timber 로깅 초기화 (간소화된 버전)
     */
    private fun initializeLogging() {
        val isDebug = isDebugMode()

        if (isDebug) {
            // 디버그 빌드: 기본 로깅
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return "Ellipsis_${element.fileName}:${element.lineNumber}"
                }
            })
            Timber.d("Timber 디버그 로깅 활성화")
        } else {
            // 릴리즈 빌드: 에러만 로깅
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority >= android.util.Log.ERROR) {
                        // 실제 앱에서는 Crashlytics 등 크래시 리포팅 도구 사용
                        android.util.Log.println(priority, tag, message)
                        t?.printStackTrace()
                    }
                }
            })
        }
    }

    /**
     * WorkManager가 필요한지 확인
     */
    private fun needsWorkManager(): Boolean {
        // 실제로 백그라운드 작업이 필요한 경우에만 true 반환
        // 현재는 음악 추천만 하므로 false
        return false
    }

    /**
     * WorkManager 초기화
     */
    private fun initializeWorkManager() {
        try {
            WorkManager.initialize(this, workManagerConfiguration)
            Timber.d("WorkManager 초기화 완료")
        } catch (e: Exception) {
            Timber.e(e, "WorkManager 초기화 실패")
        }
    }

    /**
     * 전역 예외 처리 설정 (간소화된 버전)
     */
    private fun setupGlobalExceptionHandler() {
        val isDebug = isDebugMode()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Timber.e(exception, "처리되지 않은 예외 발생 (스레드: ${thread.name})")

            if (isDebug) {
                // 디버그 모드에서는 크래시 허용 (개발자가 확인 가능)
                Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, exception)
            } else {
                // 릴리즈 모드에서는 앱 종료
                android.os.Process.killProcess(android.os.Process.myPid())
                kotlin.system.exitProcess(1)
            }
        }
    }

    override fun onTerminate() {
        Timber.i("앱 종료")
        super.onTerminate()
    }

    override fun onLowMemory() {
        Timber.w("메모리 부족 경고")
        // 메모리 정리 시도
        System.gc()
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        val levelName = when (level) {
            TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
            TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
            TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
            TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            TRIM_MEMORY_MODERATE -> "MODERATE"
            TRIM_MEMORY_COMPLETE -> "COMPLETE"
            else -> "UNKNOWN($level)"
        }

        Timber.w("메모리 트림 요청: $levelName")

        // 레벨에 따른 메모리 정리
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW -> {
                // 적극적이지 않은 정리
                System.gc()
            }

            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_COMPLETE -> {
                // 적극적인 메모리 정리
                System.gc()
                // 추가 정리 작업 수행 가능
            }
        }

        super.onTrimMemory(level)
    }
}