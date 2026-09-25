package dev.fiedri.vibe.core.ui.screen



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import dev.fiedri.vibe.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.core.ui.composables.ThumbnailCard


data class ArtistCardData(
    val name: String,
    val songsCount: Int

)

@Composable
fun ArtistsScreen(){
    val artists: List<ArtistCardData> = remember {
        List(50) { index ->
            ArtistCardData(
                name = "Artista 1",
                songsCount = index
            )
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
items(items = artists, key = { artist -> artist.songsCount }){
    artists ->
    ThumbnailCard(title = artists.name, subtitle = "${artists.songsCount} Songs", img = R.drawable.default_artist)
}
    }
}