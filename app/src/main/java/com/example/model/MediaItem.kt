package com.example.model

data class MediaItem(
    val id: Long,
    val uri: String,
    val type: MediaType,
    val dateAdded: Long,
    val duration: Long = 0L,
    val isFavorite: Boolean = false,
    val location: String? = null
)

enum class MediaType {
    IMAGE, VIDEO
}
