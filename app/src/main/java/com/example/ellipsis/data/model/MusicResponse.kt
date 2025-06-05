package com.example.ellipsis.data.model

data class MusicResponse(
    val emotion: String, // 서버: "emotion"
    val description: String, // 서버: "description"
    val tracks: List<Track>, // 서버: "tracks"
    val metadata: Metadata? = null // 서버: "metadata"
)

data class Track(
    val title: String,
    val artist: String,
    val album_image_url: String? = null
)

data class Metadata(
    val emotion_keywords: List<String>? = null,
    val objects_detected: List<String>? = null,
    val location: String? = null,
    val datetime: String? = null,
    val processed_images: Int? = null
)
