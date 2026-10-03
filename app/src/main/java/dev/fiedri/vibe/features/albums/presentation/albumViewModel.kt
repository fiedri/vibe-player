package dev.fiedri.vibe.features.albums.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.core.ui.composables.models.CardItem
import dev.fiedri.vibe.features.albums.data.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AlbumsUiState{
    object Loading: AlbumsUiState
    data class Succes(
        val cards: List<CardItem>
    ): AlbumsUiState
    data class Error(val message: String) : AlbumsUiState
}

@HiltViewModel
class albumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<AlbumsUiState>(AlbumsUiState.Loading)

    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()
    init {
        observeAlbums()
        fetchAlbums()
    }

    fun fetchAlbums(){
        viewModelScope.launch {
            _uiState.value = AlbumsUiState.Loading
            try {
                albumRepository.getAlbums()
            }catch (e: Exception){
                _uiState.value = AlbumsUiState.Error(
                    message = e.localizedMessage ?: "Error al cargar albumes"
                )
            }
        }
    }
    private fun observeAlbums(){
        viewModelScope.launch {
            albumRepository.albums.collect { albumsList ->
                _uiState.value = if (albumsList == null) {
                    AlbumsUiState.Loading
                } else {
                    AlbumsUiState.Succes(cards = albumsList.map { CardItem.Album(it) })
                }
            }
        }

    }
}