plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize") // Parcelable 자동 생성
}

android {
    namespace = "com.example.ellipsis"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ellipsis"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 벡터 드로어블 지원
        vectorDrawables {
            useSupportLibrary = true
        }

        // 다국어 리소스 최적화 (한국어, 영어만) - 수정된 방식
        resourceConfigurations.addAll(listOf("ko", "en"))
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // 디버그 빌드 설정
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.219.107:8000/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")

            // 디버그용 ProGuard
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            // 프로덕션 빌드 설정
            buildConfigField("String", "API_BASE_URL", "\"https://api.ellipsis.music/\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // 서명 설정 (추후 릴리즈용)
            // signingConfig = signingConfigs.getByName("release")
        }

        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // 컴파일 최적화
        isCoreLibraryDesugaringEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    buildFeatures {
        compose = true
        buildConfig = true // BuildConfig 클래스 생성 활성화
        viewBinding = false // ViewBinding 비활성화 (Compose 사용)
        dataBinding = false // DataBinding 비활성화
    }

    kapt {
        correctErrorTypes = true
        useBuildCache = false

        // Hilt 컴파일 최적화
        arguments {
            arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
        }
    }

    // 패키징 옵션
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }

    // 테스트 옵션
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    // APK/AAB 최적화
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    // 네이티브 라이브러리 최적화
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = false
        }

        density {
            isEnable = true
            reset()
            include("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
        }
    }
}

dependencies {

    // ===== EXIF정보 긁어오기 =====

    implementation ("androidx.exifinterface:exifinterface:1.3.6")

    // ===== Core Android 라이브러리 =====
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Core 라이브러리 Desugaring (Java 8+ API 지원)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ===== Compose UI =====
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("com.google.android.material:material:1.12.0")

    // Compose 디버그 도구
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ===== 스플래시 스크린 =====
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ===== 이미지 로딩 =====
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0") // GIF 지원
    implementation("io.coil-kt:coil-svg:2.7.0") // SVG 지원

    // ===== ML Kit (Google) =====
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:image-labeling:17.0.9") // 추가 이미지 라벨링

    // ===== 네트워킹 =====
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 네트워크 상태 모니터링
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")

    // ===== JSON 처리 =====
    implementation("com.google.code.gson:gson:2.10.1")

    // ===== 코루틴 =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ===== ViewModel & Lifecycle =====
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-process:2.8.6") // 앱 라이프사이클 추적

    // ===== Hilt 의존성 주입 =====
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0") // WorkManager와 Hilt 통합
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // ===== 네비게이션 (추후 확장용) =====
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.2")

    // ===== 로깅 시스템 =====
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ===== 데이터 저장 =====
    implementation("androidx.datastore:datastore-preferences:1.1.1") // SharedPreferences 대체
    implementation("androidx.datastore:datastore-core:1.1.1")

    // Room 데이터베이스 (추후 확장용)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ===== 권한 관리 =====
//    implementation("com.google.accompanist:accompanist-insets:0.28.0")


    // ===== 시스템 UI =====
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
//    implementation("com.google.accompanist:accompanist-insets:0.32.0")

    // ===== 애니메이션 =====
    implementation("com.airbnb.android:lottie-compose:6.5.0") // Lottie 애니메이션

    // ===== 성능 모니터링 =====
    implementation("androidx.metrics:metrics-performance:1.0.0-beta01")

    // ===== WorkManager (백그라운드 작업) =====
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // ===== 보안 =====
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // 암호화된 저장소

    // ===== 유틸리티 =====
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1") // 날짜/시간 처리
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3") // JSON 직렬화

    // ===== 테스팅 =====
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

    // Compose 테스팅
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.navigation:navigation-testing:2.8.2")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.51.1")

    // UI 테스팅
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

// ===== 의존성 충돌 해결 =====
configurations.all {
    resolutionStrategy {
        // JavaPoet 버전 강제 통일
        force("com.squareup:javapoet:1.13.0")

        // Hilt 관련 버전 통일
        force("com.google.dagger:hilt-android:2.51.1")
        force("com.google.dagger:hilt-compiler:2.51.1")
        force("com.google.dagger:dagger:2.51.1")
        force("com.google.dagger:dagger-compiler:2.51.1")

        // Kotlin 관련 버전 통일
        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")

        // Coroutines 버전 통일
        force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
        force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

        // OkHttp 버전 통일
        force("com.squareup.okhttp3:okhttp:4.12.0")
        force("com.squareup.okhttp3:logging-interceptor:4.12.0")

        // Lifecycle 버전 통일
        force("androidx.lifecycle:lifecycle-viewmodel:2.8.6")
        force("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
        force("androidx.lifecycle:lifecycle-runtime:2.8.6")
        force("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    }
}

// ===== Hilt 컴파일러 최적화 =====
hilt {
    enableAggregatingTask = false
}