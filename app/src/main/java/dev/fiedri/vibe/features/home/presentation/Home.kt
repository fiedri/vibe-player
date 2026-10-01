package dev.fiedri.vibe.features.home.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import dev.fiedri.vibe.core.ui.theme.VibeTheme
import dev.fiedri.vibe.core.ui.LocalNavigator
import dev.fiedri.vibe.core.ui.PlaylistDetail
import dev.fiedri.vibe.core.ui.Search
import dev.fiedri.vibe.core.ui.Settings
import dev.fiedri.vibe.features.albums.presentation.AlbumsScreen
import dev.fiedri.vibe.features.artists.presentation.ArtistsScreen
import dev.fiedri.vibe.features.permissions.presentation.PermissionBlockedScreen
import dev.fiedri.vibe.features.playlists.presentation.PlaylistsScreen
import dev.fiedri.vibe.features.songs.presentation.SongsScreen
import kotlinx.coroutines.launch

@Composable
fun HomeLayout(
    hasAudioPermission: Boolean,
    onOpenSettings: () -> Unit,
    onPermissionRetry: () -> Unit,
){
    val tabs = listOf("Songs", "Artists", "Albums", "Playlist")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current

    var drawerOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerOpen) { drawerOpen = false }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
            VibeTopBar(
                tabs = if (hasAudioPermission) tabs else emptyList(),
                activeTab = tabs[pagerState.currentPage],
                    onTabSelected = { selectedTab ->
                        val targetIndex = tabs.indexOf(selectedTab)
                        if (targetIndex != -1) {

                            coroutineScope.launch {
                                pagerState.animateScrollToPage(targetIndex)
                            }
                        }
                    },
                    onMenuClick = { drawerOpen = true },
                    onSearchClick = { navigator?.navigate(Search) }
                )
            },
            containerColor = VibeTheme.colors.background,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            if (hasAudioPermission) {
                Pager(
                    pagerState = pagerState,
                    tabs = tabs,
                    innerPadding = innerPadding
                )
            } else {
                PermissionBlockedScreen(
                    onOpenSettings = onOpenSettings,
                    onRetry = onPermissionRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }

        SettingsDrawer(
            open = drawerOpen,
            onDismissRequest = { drawerOpen = false },
            onSettingsClick = { navigator?.navigate(Settings) }
        )
    }


}

@Composable
internal fun Pager(
    pagerState: PagerState,
    tabs: List<String>,
    innerPadding: PaddingValues
) {
    val navigator = LocalNavigator.current
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
                3-> PlaylistsScreen(
                    onPlaylistClick = { playlist -> navigator?.navigate(PlaylistDetail(playlist.id)) }
                )
            }
        }
    }
}