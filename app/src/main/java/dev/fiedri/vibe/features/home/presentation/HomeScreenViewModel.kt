package dev.fiedri.vibe.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fiedri.vibe.core.data.SongsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeScreenViewModel @Inject constructor(
    private val songsRepository: SongsRepository
): ViewModel(){
    fun refreshLibrary(){
        viewModelScope.launch {
            songsRepository.getSongs(true)
        }
    }
}