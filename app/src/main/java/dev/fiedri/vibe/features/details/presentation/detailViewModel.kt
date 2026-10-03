package dev.fiedri.vibe.features.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.core.data.models.SongModel
import dev.fiedri.vibe.features.player.data.NowPlaying
import dev.fiedri.vibe.features.player.data.PlayerController
import dev.fiedri.vibe.features.player.data.QueueContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class detailViewModel @Inject constructor(
    private val playerController: PlayerController
): ViewModel() {
    val nowPlaying: StateFlow<NowPlaying> = playerController.nowPlaying

    fun playSong(songs: List<SongModel>, initialIndex: Int = 0, queueContext: QueueContext){
        viewModelScope.launch {
            playerController.playSong(songs = songs, initialIndex = initialIndex, queueContext = queueContext)
        }
    }
    fun shuffle(songs: List<SongModel>, queueContext: QueueContext){
        viewModelScope.launch {
            val randomIdx = songs.indices.random()

            playerController.toggleShuffle(true)
            playerController.playSong(songs, randomIdx, queueContext)
        }
    }

}