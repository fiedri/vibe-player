package dev.fiedri.vibe.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.fiedri.vibe.features.player.presentation.Player
import dev.fiedri.vibe.features.player.presentation.PlayerState
import dev.fiedri.vibe.features.player.presentation.Song
import dev.fiedri.vibe.core.ui.composables.Pager
import dev.fiedri.vibe.core.ui.composables.VibeToBar
import dev.fiedri.vibe.core.ui.theme.VibeTheme
import kotlinx.coroutines.launch

@Composable
fun VibeApp(){
    val tabs = listOf("Songs", "Artists", "Albums", "Playlist")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
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
        Scaffold(
            topBar = {
                VibeToBar(
                    tabs = tabs,
                    activeTab = tabs[pagerState.currentPage],
                    onTabSelected = { selectedTab ->
                        val targetIndex = tabs.indexOf(selectedTab)
                        if (targetIndex != -1) {

                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        }
                    }
                )
            },



            containerColor = VibeTheme.colors.background,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Pager(
                pagerState = pagerState,
                tabs = tabs,
                innerPadding = innerPadding
            )
        }
        Player(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentSong = fakeSong,
            isPlaying = isPlaying,
            currentTimeMs = 45_000,
            durationMs = fakeSong.durationMs,
            currentSongIndex = 0,
            numberOfSongs = 1,
            isShuffle = false,
            repeatMode = PlayerState.REPEAT_OFF,
            isExpanded = isExpanded,
            onTogglePlay = { isPlaying = !isPlaying },
            onNext = {},
            onPrevious = {},
            onSeek = {},
            onToggleShuffle = {},
            onCycleRepeat = {},
            onToogleExpand = { isExpanded = !isExpanded },
        )
    }
}