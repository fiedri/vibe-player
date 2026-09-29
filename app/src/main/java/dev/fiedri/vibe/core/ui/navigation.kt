package dev.fiedri.vibe.core.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import dev.fiedri.vibe.features.home.presentation.HomeLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import dev.fiedri.vibe.features.albums.presentation.AlbumDetailsScreen
import dev.fiedri.vibe.features.artists.presentation.ArtistDetailsScreen
import dev.fiedri.vibe.features.playlists.presentation.PlaylistDetailsScreen
import dev.fiedri.vibe.features.search.presentation.SearchScreen
import dev.fiedri.vibe.features.settings.presentation.SettingsScreen




class Navigator(val backStack: MutableList<NavKey>) {
    fun navigate(key: NavKey) = backStack.add(key)
    fun goBack() = backStack.removeLastOrNull()
}
val LocalNavigator = staticCompositionLocalOf<Navigator?> { null }
@Composable
fun VibeNavGraph(
    hasAudioPermission: Boolean,
    onOpenSettings: () -> Unit,
    onPermissionRetry: () -> Unit,
){


    val backStack = remember { mutableStateListOf<NavKey>(Home) }
    val navigator = remember { Navigator(backStack) }

    CompositionLocalProvider(LocalNavigator provides navigator) {

        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) backStack.removeLastOrNull()
            },
            entryProvider = entryProvider {


                entry<Home> {
                    HomeLayout(
                        hasAudioPermission = hasAudioPermission,
                        onOpenSettings = onOpenSettings,
                        onPermissionRetry = onPermissionRetry,
                    )
                }
                entry<AlbumDetail> { key ->
                    AlbumDetailsScreen(key.id)
                }
                entry<ArtistDetail> { key ->
                    ArtistDetailsScreen(key.name)
                }   // key: NavEntry
                entry<PlaylistDetail> { key ->
                    PlaylistDetailsScreen(key.id)
                }
                entry<Settings> { SettingsScreen() }
                entry<Search> { SearchScreen(onBack = { navigator.goBack() }) }


            }
        )
    }
}
