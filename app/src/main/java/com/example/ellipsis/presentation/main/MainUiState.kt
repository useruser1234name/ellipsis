package com.example.ellipsis.presentation.main


import android.graphics.Bitmap

data class MainUiState(
    val bitmap: Bitmap? = null,
    val detectedObjects: List<String> = emptyList(),
    val hashtags: String = "#봄 #데이트",
    val photoDate: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String = "",
    val recommendedSongs: List<String> = emptyList(),
    val errorMessage: String? = null
)