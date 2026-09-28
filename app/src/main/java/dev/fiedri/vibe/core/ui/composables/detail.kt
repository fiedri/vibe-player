package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.ui.composables.models.CardData
import dev.fiedri.vibe.core.ui.theme.VibeTheme

enum class EntityType(val kicker: String, val emptyStateText: String) {
    ALBUM("Album", "Este album no tiene canciones."),
    PLAYLIST("Playlist", "Esta playlist no tiene canciones."),
    ARTISTS("Artista", "Recursos no disponibles")
}

data class DetailHeader(
    val name: String,
    val image: Int = R.drawable.default_cover
)

data class DetailResources(
    val songs: List<SongCardUiState>,
    val albums: List<CardData> = emptyList()
)
@Composable
fun DetailsScreen(
    header: DetailHeader,
    type: EntityType,
    resources: DetailResources,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onPlay: () -> Unit = {},
    onShuffle: () -> Unit = {},
    onSongClick: (SongCardUiState) -> Unit = {}
) {
    LazyColumn(modifier = modifier.fillMaxSize().background(VibeTheme.colors.background)) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                Image(
                    painter = painterResource(id = header.image),
                    contentDescription = "Cover",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 1.0f)
                                )
                            )
                        )
                )
                Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 10.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atras",
                            tint = VibeTheme.colors.foreground
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = type.kicker.uppercase(),
                        color = VibeTheme.colors.primary,
                        style = VibeTheme.typography.caption,
                        fontSize = 16.sp
                    )
                    Text(
                        text = header.name.uppercase(),
                        color = VibeTheme.colors.foreground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        style = VibeTheme.typography.titleMedium
                    )
                    if (type !== EntityType.ARTISTS) {
                        Text(
                            text = (if (resources.songs.size == 1) "1 cancion" else "${resources.songs.size} canciones").uppercase(),
                            color = VibeTheme.colors.mutedForeground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            style = VibeTheme.typography.display
                        )
                    }
                }
            }
            if (type == EntityType.ARTISTS) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "${resources.albums.size} albumes".uppercase(),
                    color = VibeTheme.colors.foreground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = VibeTheme.typography.titleMedium
                )
                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resources.albums) { album ->
                        ThumbnailCard(
                            title = album.name,
                            img = album.image,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    shape = RectangleShape,
                    onClick = onPlay,
                    modifier = Modifier.weight(1f).height(48.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibeTheme.colors.foreground,
                        contentColor = VibeTheme.colors.background
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir"
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        "Reproducir".uppercase(),
                        style = VibeTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onShuffle,
                    shape = RectangleShape,
                    modifier = Modifier.weight(1f).height(48.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    border = BorderStroke(
                        width = 2.dp,
                        color = VibeTheme.colors.border
                    ),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = VibeTheme.colors.foreground,
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Aleatorio"
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        "Aleatorio".uppercase(),
                        style = VibeTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (resources.songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        type.emptyStateText,
                        color = VibeTheme.colors.mutedForeground,
                        style = VibeTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(items = resources.songs, key = { song -> song.id }) { song ->
                SongCard(song, onClick = { onSongClick(song) })
            }
        }
    }
}
