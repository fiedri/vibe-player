package dev.fiedri.vibe.features.artists.presentation

import dev.fiedri.vibe.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.fiedri.vibe.core.ui.composables.models.CardData
import dev.fiedri.vibe.core.ui.composables.DetailHeader
import dev.fiedri.vibe.core.ui.composables.DetailResources
import dev.fiedri.vibe.core.ui.composables.DetailsScreen
import dev.fiedri.vibe.core.ui.composables.EntityType

import dev.fiedri.vibe.core.ui.LocalNavigator
import dev.fiedri.vibe.core.ui.composables.SongCardUiState

@Composable
fun ArtistDetailsScreen(name: String) {
    val navigator = LocalNavigator.current
    val songs: List<SongCardUiState> = remember(name) {
        List(50) { index ->
            SongCardUiState(
                id = "id ${index + 1}",
                duration = "3:00",
                title = "Cancion ${index + 1}",
                artist = "artista"
            )
        }
    }
    val albums: List<CardData> = remember {
        List(50) { index ->
            CardData(
                id = index,
                name = "Album $index",
                songsCount = index,
                image = R.drawable.default_cover
            )
        }
    }
    DetailsScreen(
        header = DetailHeader(name = name, image = R.drawable.default_artist),
        type = EntityType.ARTISTS,
        resources = DetailResources(songs, albums),
        onBack = { navigator?.goBack() },
        onPlay = {},
        onShuffle = {}
    )
}