package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.ui.composables.models.SongCardUiState
import dev.fiedri.vibe.core.ui.theme.VibeTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongCard(
    uiState: SongCardUiState,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit= {},
    onOptionsClick: () -> Unit = {}
) {

    val isSelected: Boolean = uiState.isSelected
    val isPlayingThis: Boolean = uiState.isPlayingThis

    val backgroundColor: Color = when {
        isSelected -> VibeTheme.colors.accent
        isPlayingThis -> VibeTheme.colors.cards
        else -> Color.Transparent
    }

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


        Icon(
            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlayingThis) "Pausar" else "Reproducir",
            tint = VibeTheme.colors.foreground,
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = uiState.song.title,
                color = VibeTheme.colors.foreground,
                style = VibeTheme.typography.songTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = uiState.song.artists,
                color = VibeTheme.colors.mutedForeground,
                style = VibeTheme.typography.songArtist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatMillisToMMSS(uiState.song.duration),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    uiState: SongCardUiState,
    onDismissRequest: () -> Unit,
    onInfo: () -> Unit = {},
    onPlayNext: () -> Unit = {},
    onAddToPlaylists: () -> Unit = {},
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(),
        shape = RectangleShape,
        containerColor = VibeTheme.colors.popover,
        contentColor = VibeTheme.colors.foreground,
        scrimColor = VibeTheme.colors.background.copy(alpha = 0.2f),
        dragHandle = null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SongOptionRow(
                text = stringResource(R.string.songs_options_info),
                onClick = {
                    onDismissRequest()
                    onInfo()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            HorizontalDivider(color = VibeTheme.colors.border)
            SongOptionRow(
                text = stringResource(R.string.songs_options_next_in_queue),
                onClick = {
                    onDismissRequest()
                    onPlayNext()
                }
            )
            HorizontalDivider(color = VibeTheme.colors.border)
            SongOptionRow(
                text = stringResource(R.string.songs_options_add_to_playlists),
                onClick = {
                    onDismissRequest()
                    onAddToPlaylists()
                }
            )
            HorizontalDivider(color = VibeTheme.colors.border)
            SongOptionRow(
                text = stringResource(R.string.songs_options_share),
                onClick = {
                    onDismissRequest()
                    onShare()
                }
            )
            HorizontalDivider(color = VibeTheme.colors.border)
            SongOptionRow(
                text = stringResource(R.string.songs_options_delete),
                onClick = {
                    onDismissRequest()
                    onDelete()
                },
                destructive = true
            )
        }
    }
}

@Composable
private fun SongOptionRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    destructive: Boolean = false
) {
    val contentColor = if (destructive) {
        VibeTheme.colors.destructive
    } else {
        VibeTheme.colors.foreground
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (leadingIcon != null) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                leadingIcon()
            }
        }
        Text(
            text = text,
            color = contentColor,
            style = VibeTheme.typography.bodyLarge
        )
    }
}