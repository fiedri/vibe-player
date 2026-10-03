package dev.fiedri.vibe.features.playlists.presentation

import androidx.compose.runtime.Composable
import dev.fiedri.vibe.core.ui.LocalNavigator

@Composable
fun PlaylistDetailsScreen(id: Int) {
    val navigator = LocalNavigator.current
    /*val songs: List<SongCardUiState> = remember(id) {
        List(50) { index ->
            SongCardUiState(
                id = "id ${index + 1}",
                duration = "3:00",
                title = "Cancion ${index + 1}",
                artist = "artista"
            )
        }
    }
    DetailsScreen(
        header = DetailHeader(name = "Playlist $id"),
        type = EntityType.PLAYLIST,
        resources = DetailResources(songs),
        onBack = { navigator?.goBack() },
        onPlay = {},
        onShuffle = {}
    )

     */
}
