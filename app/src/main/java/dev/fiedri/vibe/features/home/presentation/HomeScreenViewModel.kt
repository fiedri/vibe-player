package dev.fiedri.vibe.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.features.songs.data.SongsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val songsRepository: SongsRepository
): ViewModel(){
    fun refreshLibrary(){
        viewModelScope.launch {
            songsRepository.getSongs(true)
        }
    }
}