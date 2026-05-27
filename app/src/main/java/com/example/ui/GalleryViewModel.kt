package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.MediaItem
import com.example.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalleryViewModel(private val repository: MediaRepository) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()
    
    val groupedMedia = mediaItems.map { items ->
        items.groupBy { 
            // Group by year and month
            val date = java.util.Date(it.dateAdded)
            val format = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
            format.format(date)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            repository.getMediaItems().collect { items ->
                _mediaItems.value = items
            }
        }
    }
}
