package dev.fiedri.vibe.features.player.presentation

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentSongIndex: Int = 0,
    val numberOfSongs: Int = 0,
    val isShuffle: Boolean = false,
    val repeatMode: PlayerState = PlayerState.REPEAT_OFF,
    val isExpanded: Boolean = false
)
