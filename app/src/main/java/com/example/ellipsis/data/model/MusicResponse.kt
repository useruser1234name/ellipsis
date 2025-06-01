package com.example.ellipsis.data.model

data class MusicResponse(
    val keywords: List<String>,
    val description: String,
    val location: String,
    val datetime: String?,
    val recommendations: List<String>
)