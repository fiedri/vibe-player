package dev.fiedri.vibe.core.ui.screen



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import dev.fiedri.vibe.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.core.ui.composables.ThumbnailCard
import dev.fiedri.vibe.navigation.AlbumDetail
import dev.fiedri.vibe.navigation.LocalNavigator


data class AlbumCardData(
    val id: Int,
    val name: String,
    val songsCount: Int

)

@Composable
fun AlbumsScreen(){
    val navigator = LocalNavigator.current
    val albums: List<AlbumCardData> = remember {
        List(50) { index ->
            AlbumCardData(
                id = index,
                name = "Album $index",
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
items(items = albums, key = { album -> album.songsCount }){
    albums ->
    Box(
        modifier = Modifier.fillMaxSize()
            .clickable{
                navigator?.navigate(AlbumDetail(albums.id))
            }

    ) {
        ThumbnailCard(title = albums.name, subtitle = "${albums.songsCount} Songs", img = R.drawable.default_cover)
    }
}
    }
}
