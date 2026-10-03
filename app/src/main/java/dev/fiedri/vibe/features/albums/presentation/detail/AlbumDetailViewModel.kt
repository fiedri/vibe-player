package dev.fiedri.vibe.features.albums.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.data.models.SongModel
import dev.fiedri.vibe.core.ui.AlbumDetail
import dev.fiedri.vibe.features.albums.data.AlbumRepository
import dev.fiedri.vibe.features.player.data.PlayerController
import dev.fiedri.vibe.features.player.data.QueueContext
import dev.fiedri.vibe.features.songs.data.SongsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlbumDetailUiState {
    data object Loading : AlbumDetailUiState
    data class Success(val songs: List<SongModel>, val albumName: String, val albumArt: Any?) : AlbumDetailUiState
    data class Error(val message: String) : AlbumDetailUiState
}
@HiltViewModel(assistedFactory = AlbumDetailViewModel.Factory::class)
class AlbumDetailViewModel @AssistedInject constructor(
    @Assisted val navKey: AlbumDetail,
    private val songRepository: SongsRepository,
    private val albumRepository: AlbumRepository,
) : ViewModel() {
    val albumId: Long = navKey.id

    private val _uiState = MutableStateFlow<AlbumDetailUiState>(AlbumDetailUiState.Loading)
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        loadAlbumDetail(albumId)
    }

    fun loadAlbumDetail(albumId: Long){
        viewModelScope.launch {
            _uiState.value = AlbumDetailUiState.Loading
            try {
                val result = songRepository.getSongsByAlbumId(albumId)
                val albumResult = albumRepository.getAlbumById(albumId)
                _uiState.value = AlbumDetailUiState.Success(result, albumName = albumResult?.albumName ?: "<Unknown>",
                    albumArt = albumResult?.albumArt ?: R.drawable.default_cover
                )
            }catch (e: Exception){
                _uiState.value = AlbumDetailUiState.Error(
                    message = e.localizedMessage ?: "Error al cargar el album"
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: AlbumDetail): AlbumDetailViewModel
    }
}