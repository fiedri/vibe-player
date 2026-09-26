package dev.fiedri.vibe.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.ui.NavDisplay
import dev.fiedri.vibe.features.home.HomeLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import kotlinx.serialization.Serializable

@Serializable
data object Home: NavKey

@Serializable
data class AlbumDetail(val id: Int) : NavKey
@Serializable
data class ArtistDetail(val name: String): NavKey
@Serializable
data class PlaylistDetail(val id: Int): NavKey
@Serializable
data object Search: NavKey
@Serializable
data object Settings: NavKey

class Navigator(val backStack: MutableList<NavKey>) {
    fun navigate(key: NavKey) = backStack.add(key)
    fun goBack() = backStack.removeLastOrNull()
}
val LocalNavigator = staticCompositionLocalOf<Navigator?> { null }
@Composable
fun VibeNavGraph(){


    val backStack = remember { mutableStateListOf<NavKey>(Home) }
    val navigator = remember { Navigator(backStack) }

    CompositionLocalProvider(LocalNavigator provides navigator) {

        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) backStack.removeLastOrNull()
            },
            entryProvider = entryProvider {


                entry<Home> { HomeLayout() }
                entry<AlbumDetail> { key ->
                    Text("Álbum: ${key.id}") }
                entry<ArtistDetail> { Text("Aun no disponible") }   // key: NavEntry
                entry<PlaylistDetail> { Text("Aun no disponible") }
                entry<Settings> { Text("Aun no disponible") }
                entry<Search> { Text("Aun no disponible") }


            }
        )
    }
}