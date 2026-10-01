package dev.fiedri.vibe.features.songs.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.data.models.SongModel
import dev.fiedri.vibe.core.ui.composables.SongCard
import dev.fiedri.vibe.core.ui.composables.SongOptionsSheet
import dev.fiedri.vibe.core.ui.composables.models.SongCardUiState
import dev.fiedri.vibe.core.ui.theme.VibeTheme

@Composable
fun SongsScreen(
    viewModel: SongScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is SongsUiState.Loading -> {
            SongsLoadingState()
        }

        is SongsUiState.Success -> {
            SongsList(
                songs = state.songs,
                currentPlayingId = state.currentPlayingId,
                onSongClick = {}
            )
        }

        is SongsUiState.Error -> {
            SongsErrorState(
                message = state.message,
                onRetry = { viewModel.fetchSongs(forceRefresh = true) }
            )
        }
    }
}

@Composable
private fun SongsLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = VibeTheme.colors.primary)
    }
}

@Composable
private fun SongsErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                color = VibeTheme.colors.foreground,
                style = VibeTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VibeTheme.colors.destructive
                )
            ) {
                Text(
                    text = stringResource(R.string.biblioteca_retry),
                    style = VibeTheme.typography.titleMedium,
                    color = VibeTheme.colors.destructive
                )
            }
        }
    }
}


@Composable
fun SongsList(songs: List<SongModel>, currentPlayingId: Long? = null, onSongClick: ()-> Unit = {}){
    var optionsFor by remember { mutableStateOf<SongCardUiState?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = songs, key = { song -> song.id }){
                song ->
            val isCurrent = currentPlayingId != null && song.id == currentPlayingId
            val songUiState = SongCardUiState(
                song = song,
                isSelected = isCurrent,
                isPlayingThis = isCurrent
            )
            SongCard(
                uiState = songUiState,
                onClick = {onSongClick()}, onLongClick = {}, onOptionsClick = { optionsFor = songUiState })
        }
    }
    optionsFor?.let { song ->
        SongOptionsSheet(
            uiState = song,
            onDismissRequest = { optionsFor = null }
        )
    }
}
