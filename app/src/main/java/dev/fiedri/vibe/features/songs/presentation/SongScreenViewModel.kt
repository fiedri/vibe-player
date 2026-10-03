package dev.fiedri.vibe.features.songs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.features.songs.data.SongsRepository
import dev.fiedri.vibe.core.data.models.SongModel
import dev.fiedri.vibe.features.player.data.NowPlaying
import dev.fiedri.vibe.features.player.data.PlayerController
import dev.fiedri.vibe.features.player.data.QueueContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface SongsUiState {
    object Loading : SongsUiState
    data class Success(
        val songs: List<SongModel>
    ) : SongsUiState
    data class Error(val message: String) : SongsUiState
}

@HiltViewModel
class SongScreenViewModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val playerController: PlayerController
): ViewModel(){
    private val _uiState = MutableStateFlow<SongsUiState>(SongsUiState.Loading)
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()
    val nowPlaying: StateFlow<NowPlaying> = playerController.nowPlaying
    init {
        observeSongs()
        fetchSongs()
    }
    fun fetchSongs(forceRefresh: Boolean = false){
        viewModelScope.launch {
            _uiState.value = SongsUiState.Loading
            try {
                songsRepository.getSongs(forceRefresh)
            }catch (e: Exception){
                _uiState.value = SongsUiState.Error(
                    message = e.localizedMessage ?: "Error al cargar canciones"
                )
            }
        }
    }
    private fun observeSongs(){
        viewModelScope.launch {
            songsRepository.songs.collect { songsList ->
                _uiState.value = if (songsList == null) {
                    SongsUiState.Loading
                } else {
                    SongsUiState.Success(songs = songsList)
                }
            }
        }
    }
    fun onSongClicked(songs: List<SongModel>, index: Int, context: QueueContext = QueueContext.AllSongs) {
        viewModelScope.launch {
            playerController.playSong(
                songs = songs,
                initialIndex = index,
                queueContext = context
            )
        }
    }
}