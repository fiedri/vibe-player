package dev.fiedri.vibe.features.songs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.core.data.SongsRepository
import dev.fiedri.vibe.core.data.models.SongModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface SongsUiState {
    object Loading : SongsUiState
    data class Success(
        val songs: List<SongModel>,
        val currentPlayingId: Long? = null
    ) : SongsUiState
    data class Error(val message: String) : SongsUiState
}

@HiltViewModel
class SongScreenViewModel @Inject constructor(
    private val songsRepository: SongsRepository
): ViewModel(){
    private val _uiState = MutableStateFlow<SongsUiState>(SongsUiState.Loading)
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()
    init {
        observeSongs()
        fetchSongs()
    }
    fun fetchSongs(forceRefresh: Boolean = false){
        viewModelScope.launch {
            _uiState.value = SongsUiState.Loading
            try {
                _uiState.value = SongsUiState.Success(songs = songsRepository.getSongs(forceRefresh))
            }catch (e: Exception){
                _uiState.value = SongsUiState.Error(
                    message = e.localizedMessage ?: "Error al cargar canciones"
                )
            }
        }
    }
    fun observeSongs(){
        _uiState.value = SongsUiState.Loading
        viewModelScope.launch {
            songsRepository.songs.collect{
                songsList ->
                    if(songsList.isNotEmpty()){
                        val currentPlaying = (_uiState.value as? SongsUiState.Success)?.currentPlayingId
                        _uiState.value = SongsUiState.Success(songs = songsList, currentPlayingId = currentPlaying)
                    }

            }
        }
    }
    fun playSong(songId: Long) {
        val currentState = _uiState.value
        if (currentState is SongsUiState.Success) {
            _uiState.value = currentState.copy(currentPlayingId = songId)
        }
    }
}