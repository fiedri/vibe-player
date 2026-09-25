package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.fiedri.vibe.core.ui.screen.AlbumsScreen
import dev.fiedri.vibe.core.ui.screen.ArtistsScreen
import dev.fiedri.vibe.core.ui.screen.PlaylistsScreen
import dev.fiedri.vibe.core.ui.screen.SongsScreen

@Composable
fun Pager(
    pagerState: PagerState,
    tabs: List<String>,
    innerPadding: PaddingValues
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) { pageIndex ->

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when(pageIndex){
                0 -> SongsScreen()
                1 -> ArtistsScreen()
                2 -> AlbumsScreen()
                3-> PlaylistsScreen()
            }
        }
    }
}
