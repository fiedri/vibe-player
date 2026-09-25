package dev.fiedri.vibe.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.ui.theme.VibeTheme
import androidx.compose.ui.graphics.Color


data class SongCardData(
    val id: String,
    val duration: String,
    val title: String,
    val artist: String

)
@Composable
fun SongsScreen(){
    val songs: List<SongCardData> = remember {
        List(50) { index ->
            SongCardData(
                id = "id ${index + 1}",
                duration = "3:00",
                title = "Titulo ${index + 1}",
                artist = "artista"
            )
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
items(items = songs, key = { song -> song.id }){
    song -> SongCard(song, isPlayingThis = false, isSelected = false, onClick = {}, onLongClick = {}, onOptionsClick = {})
}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongCard(
    song: SongCardData,
    isPlayingThis: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit= {},
    onOptionsClick: () -> Unit = {}
) {

    /*val backgroundColor: Color = when {
        isSelected -> VibeTheme.colors.accent // o el color que uses para selección
        isPlayingThis -> VibeTheme.colors.card//.copy(alpha = 0.5f)
        else -> Color.Transparent
    }*/
    val backgroundColor: Color = when {
        isSelected -> VibeTheme.colors.accent
        isPlayingThis -> VibeTheme.colors.cards
        else -> Color.Transparent // O androidx.compose.ui.graphics.Color.Transparent
    } as Color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(color = backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        IconButton(
            onClick = onClick,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlayingThis) "Pausar" else "Reproducir",
                tint = if (isPlayingThis) VibeTheme.colors.primary else VibeTheme.colors.foreground,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                color = VibeTheme.colors.foreground,
                style = VibeTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = VibeTheme.colors.mutedForeground,
                style = VibeTheme.typography.caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = song.duration, // Aquí puedes aplicar tu función formatearMS(song.duration) si es un Long/Int
                color = VibeTheme.colors.mutedForeground,
                style = VibeTheme.typography.caption
            )

            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = VibeTheme.colors.mutedForeground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}