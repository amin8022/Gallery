package com.example.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.model.MediaItem
import com.example.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

    fun getMediaItems(): Flow<List<MediaItem>> = flow {
        val mediaList = mutableListOf<MediaItem>()
        // Try reading from MediaStore
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DURATION
        )
        
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        val selectionArgs = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )
        
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val typeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val type = cursor.getInt(typeColumn)
                    val dateAdded = cursor.getLong(dateColumn)
                    val duration = cursor.getLong(durationColumn)

                    val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                    val mediaType = if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) MediaType.VIDEO else MediaType.IMAGE
                    
                    mediaList.add(MediaItem(id, uri.toString(), mediaType, dateAdded * 1000, duration))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (mediaList.isEmpty()) {
            mediaList.addAll(getDummyData())
        }

        emit(mediaList)
    }.flowOn(Dispatchers.IO)
    
    private fun getDummyData(): List<MediaItem> {
        val dummy = mutableListOf<MediaItem>()
        val topics = listOf("nature", "water", "mountain", "city", "architecture", "food", "abstract", "animals", "people", "tech", "cars")
        val now = System.currentTimeMillis()
        for (i in 1..200) {
            val randomTopic = topics[i % topics.size]
            val offset = (Math.random() * 86400 * 30 * 12 * 1000).toLong() // up to 1 year back
            dummy.add(
                MediaItem(
                    id = i.toLong(),
                    // Picsum URL for dummy images
                    uri = "https://picsum.photos/seed/${i * 1337}/400/400",
                    type = MediaType.IMAGE,
                    dateAdded = now - offset,
                    isFavorite = Math.random() > 0.8
                )
            )
        }
        return dummy.sortedByDescending { it.dateAdded } // Sort by date desc
    }
}
