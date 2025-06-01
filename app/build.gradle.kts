plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.ellipsis"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ellipsis"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    buildFeatures {
        compose = true
    }

    kapt {
        useBuildCache = false
        correctErrorTypes = true
    }
}

dependencies {
    // AndroidX 기본 (최신 버전으로 업데이트)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.1")

    // Compose (최신 BOM 사용)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Coil 이미지 로딩
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ML Kit 객체 인식
    implementation("com.google.mlkit:object-detection:17.0.2")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Coroutine + Play Services
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ViewModel (Compose 연동)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // Hilt DI (최신 버전)
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // 디버깅용 로그
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Retrofit 통신 로그 확인
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 테스트
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// JavaPoet 충돌 방지를 위한 강제 버전 적용
configurations.all {
    resolutionStrategy {
        force("com.squareup:javapoet:1.13.0")
        // Hilt 버전 충돌 방지
        force("com.google.dagger:hilt-android:2.51.1")
        force("com.google.dagger:hilt-compiler:2.51.1")
        force("com.google.dagger:dagger:2.51.1")
        force("com.google.dagger:dagger-compiler:2.51.1")
    }
}

// Hilt 관련 추가 설정
hilt {
    enableAggregatingTask = false
}