package dev.fiedri.vibe.features.player.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fiedri.vibe.core.data.models.SongModel
import dev.fiedri.vibe.features.player.data.PlayerController
import dev.fiedri.vibe.features.player.data.QueueContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController
): ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var basePositionMs: Long = 0L
    private var systemStartTimeMs: Long = 0L

    init {
        playerController.create { mediaController ->

            mediaController.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) {
                        systemStartTimeMs = System.currentTimeMillis()
                        basePositionMs = mediaController.currentPosition.coerceAtLeast(0L)
                        startPositionClock()
                    } else {
                        stopClock()
                        _uiState.update {
                            it.copy(currentTimeMs = mediaController.currentPosition.coerceAtLeast(0L))
                        }
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val metadata = mediaItem?.mediaMetadata
                    val playingSong = mediaItem?.let {
                        SongPlayingState(
                            id = it.mediaId,
                            title = metadata?.title?.toString(),
                            artist = metadata?.artist?.toString(),
                            album = metadata?.albumTitle?.toString(),
                            artwork = metadata?.artworkUri
                        )
                    }

                    systemStartTimeMs = System.currentTimeMillis()
                    basePositionMs = mediaController.currentPosition.coerceAtLeast(0L)
                    startPositionClock()
                    _uiState.update {
                        it.copy(
                            currentSong = playingSong,
                            currentSongIndex = mediaController.currentMediaItemIndex,
                            numberOfSongs = mediaController.mediaItemCount,
                            durationMs = mediaController.duration.coerceAtLeast(0L),
                            currentTimeMs = basePositionMs
                        )
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _uiState.update {
                            it.copy(durationMs = mediaController.duration.coerceAtLeast(0L))
                        }
                    }
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    systemStartTimeMs = System.currentTimeMillis()
                    basePositionMs = mediaController.currentPosition.coerceAtLeast(0L)
                    _uiState.update { it.copy(currentTimeMs = basePositionMs) }
                }
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _uiState.update {
                        it.copy(isShuffle = shuffleModeEnabled)
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            repeatMode = when (repeatMode) {
                                Player.REPEAT_MODE_ONE -> PlayerState.REPEAT_ONE
                                Player.REPEAT_MODE_ALL -> PlayerState.REPEAT_ALL
                                else -> PlayerState.REPEAT_OFF
                            }
                        )
                    }
                }

            })
        }
    }

    fun toggleExpansion(){
        _uiState.update { it.copy(isExpanded = !it.isExpanded) }
    }

    fun next(){
        viewModelScope.launch {
            playerController.playNext()
        }
    }

    fun previous(){
        viewModelScope.launch {
            playerController.playPrevious()
        }
    }
    fun seekTo(ms: Long){
        viewModelScope.launch {
            playerController.seekTo(ms)
        }
    }

    fun togglePlay(){
        viewModelScope.launch {
            if(_uiState.value.isPlaying){
                playerController.pause()
            }else{
                playerController.play()
            }
        }
    }

    fun toggleShuffle(){
        viewModelScope.launch{playerController.toggleShuffle()}
    }

    fun alterRepeatMode(){
        viewModelScope.launch {
            playerController.alterRepetitionMode()
        }
    }
    // --- Reloj ---

    private fun startPositionClock(){
        stopClock()

        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isPlaying){
                val elapsedTimeMs = System.currentTimeMillis() - systemStartTimeMs
                val calculatedPosition = basePositionMs + elapsedTimeMs
                val maxDuration = _uiState.value.durationMs

                if(maxDuration > 0 && calculatedPosition >= maxDuration){
                    _uiState.update { it.copy(currentTimeMs = maxDuration) }
                    stopClock()
                    break
                } else {
                    _uiState.update { it.copy(currentTimeMs = calculatedPosition) }
                }
                delay(500L)
            }
        }
    }

    private fun stopClock() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopClock()
    }
}