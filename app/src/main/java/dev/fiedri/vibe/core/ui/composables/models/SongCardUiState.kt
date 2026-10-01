package dev.fiedri.vibe.core.ui.composables.models

import dev.fiedri.vibe.core.data.models.SongModel

data class SongCardUiState(
    val song: SongModel,
    val isSelected: Boolean = false,
    val isPlayingThis: Boolean = false
)