package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.core.ui.composables.models.CardItem


@Composable
fun CardGrid(
    cards: List<CardItem>,
    onItemClick: (CardItem) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = cards,
            key = { card -> card.key }
        ) { card ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onItemClick(card) }
            ) {
                when (card) {
                    is CardItem.Album -> ThumbnailCard(
                        title = card.album.albumName,
                        subtitle = "${card.album.numberOfSongs} Songs",
                        img = card.album.albumArt
                    )
                    is CardItem.Artist -> ThumbnailCard(
                        title = card.name,
                        subtitle = "${card.songsCount} Songs",
                        img = card.artUri
                    )
                }

            }
        }
    }
}