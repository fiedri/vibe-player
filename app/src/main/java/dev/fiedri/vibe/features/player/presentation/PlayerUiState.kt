package dev.fiedri.vibe.features.player.presentation

import android.net.Uri
import dev.fiedri.vibe.core.data.models.SongModel

data class SongPlayingState(
    val id: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val artwork: Uri? = null
)
data class PlayerUiState(
    val currentSong: SongPlayingState? = null,
    val isPlaying: Boolean = false,
    val currentTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentSongIndex: Int = 0,
    val numberOfSongs: Int = 0,
    val isShuffle: Boolean = false,
    val repeatMode: PlayerState = PlayerState.REPEAT_OFF,
    val isExpanded: Boolean = false
)
