package dev.fiedri.vibe.features.player.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.data.models.SongModel
import dev.fiedri.vibe.core.ui.composables.VibeMenu
import dev.fiedri.vibe.core.ui.composables.VibeMenuItem

@Composable
fun PlayerMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    song: SongPlayingState,
    modifier: Modifier = Modifier,
    onInfo: () -> Unit = {},
    onAddToPlaylists: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    VibeMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        VibeMenuItem(
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
        VibeMenuItem(
            text = stringResource(R.string.songs_options_add_to_playlists),
            onClick = {
                onDismissRequest()
                onAddToPlaylists()
            }
        )
        VibeMenuItem(
            text = stringResource(R.string.songs_options_delete),
            onClick = {
                onDismissRequest()
                onDelete()
            },
            destructive = true
        )
    }
}
