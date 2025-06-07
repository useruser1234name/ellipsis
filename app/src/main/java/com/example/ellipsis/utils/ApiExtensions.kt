package com.example.ellipsis.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * API 관련 확장 함수들
 * 프로젝트 전체에서 사용 가능
 */

/**
 * String을 RequestBody로 쉽게 변환
 */
fun String.toRequestBody(mediaType: String = "text/plain"): RequestBody {
    return this.toRequestBody(mediaType.toMediaTypeOrNull())
}

/**
 * Int를 RequestBody로 변환
 */
fun Int.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}

/**
 * Float를 RequestBody로 변환
 */
fun Float.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}

/**
 * Boolean을 RequestBody로 변환
 */
fun Boolean.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}

/**
 * Double을 RequestBody로 변환
 */
fun Double.toRequestBody(): RequestBody {
    return this.toString().toRequestBody("text/plain".toMediaTypeOrNull())
}