package dev.fiedri.vibe.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.features.player.presentation.Player
import dev.fiedri.vibe.features.player.presentation.PlayerState
import dev.fiedri.vibe.features.player.presentation.PlayerUiState
import dev.fiedri.vibe.features.player.presentation.Song

@Composable
fun VibeApp(){
    var isPlaying by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val fakeSong = remember {
        Song(
            title = "Sobreviviendo a la migración",
            artist = "Big Pickle",
            album = "Vibe Sessions",
            uri = "content://fake",
            albumArtUri = null,
            durationMs = 3_000_000
        )
    }
    BackHandler(enabled = isExpanded) {
        if(isExpanded){
            isExpanded = false
        }
    }
    Box(Modifier.fillMaxSize()){
        Column(
            modifier = Modifier.padding(bottom = 92.dp).fillMaxSize()
        ) {
            VibeNavGraph()
        }
        Player(
            uiState = PlayerUiState(
                currentSong = fakeSong,
                isPlaying = isPlaying,
                currentTimeMs = 45_000,
                durationMs = fakeSong.durationMs,
                currentSongIndex = 0,
                numberOfSongs = 1,
                isShuffle = false,
                repeatMode = PlayerState.REPEAT_OFF,
                isExpanded = isExpanded,
            ),
            modifier = Modifier.align(Alignment.BottomCenter),
            onTogglePlay = { isPlaying = !isPlaying },
            onNext = {},
            onPrevious = {},
            onSeek = {},
            onToggleShuffle = {},
            onCycleRepeat = {},
            onToggleExpand = { isExpanded = !isExpanded },
        )
    }

}