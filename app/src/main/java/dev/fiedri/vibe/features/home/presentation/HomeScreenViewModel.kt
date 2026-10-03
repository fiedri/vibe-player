package dev.fiedri.vibe.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.features.albums.data.AlbumRepository
import dev.fiedri.vibe.features.songs.data.SongsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val albumRepository: AlbumRepository
): ViewModel(){
    fun refreshLibrary(){
        viewModelScope.launch {
            try {
                songsRepository.getSongs(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                albumRepository.getAlbums(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}