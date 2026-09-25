package dev.fiedri.vibe.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.ui.theme.VibeTheme

data class PlaylistCardData(
    val id: Int,
    val name: String,
    val songsCount: Int
)

@Composable
fun PlaylistsScreen(
    onPlaylistClick: (PlaylistCardData) -> Unit = {}
) {
    val favorites = remember { PlaylistCardData(id = 1, name = "favoritos", songsCount = 0) }
    val playlists: List<PlaylistCardData> = remember {
        List(10) { index ->
            PlaylistCardData(
                id = index + 2,
                name = "Playlist ${index + 1}",
                songsCount = index * 3
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item(key = "favorites") {
                FavoritesRow(favorites = favorites, onClick = { onPlaylistClick(favorites) })
            }
            item(key = "header") {
                Text(
                    text = "Playlists",
                    style = VibeTheme.typography.display,
                    color = VibeTheme.colors.foreground,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            if (playlists.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay playlists",
                            color = VibeTheme.colors.mutedForeground,
                            style = VibeTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            } else {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistRow(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = {},
                containerColor = VibeTheme.colors.primary,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Backup",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            FloatingActionButton(
                onClick = {},
                containerColor = VibeTheme.colors.primary,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Crear playlist",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoritesRow(
    favorites: PlaylistCardData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .border(1.dp, VibeTheme.colors.primary)
            .background(Color.Transparent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favoritos",
                tint = VibeTheme.colors.primary,
                modifier = Modifier.size(38.dp)
            )
            Text(
                text = "Favoritos",
                color = VibeTheme.colors.primary,
                style = VibeTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
        Text(
            text = "${favorites.songsCount} canciones",
            color = VibeTheme.colors.mutedForeground,
            style = VibeTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 20.dp)
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: PlaylistCardData,
    onClick: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.QueueMusic,
                contentDescription = null,
                tint = VibeTheme.colors.foreground,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = playlist.name,
                color = VibeTheme.colors.foreground,
                style = VibeTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "${playlist.songsCount} canciones",
            color = VibeTheme.colors.mutedForeground,
            style = VibeTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 4.dp)
        )

        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opciones",
                    tint = VibeTheme.colors.mutedForeground,
                    modifier = Modifier.size(20.dp)
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = {
                        menuOpen = false
                    }
                )
            }
        }
    }
}