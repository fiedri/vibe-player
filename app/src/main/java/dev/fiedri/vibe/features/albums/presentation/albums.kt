package dev.fiedri.vibe.features.albums.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fiedri.vibe.core.ui.composables.CardGrid
import dev.fiedri.vibe.core.ui.AlbumDetail
import dev.fiedri.vibe.core.ui.LoadingState
import dev.fiedri.vibe.core.ui.LocalNavigator
import dev.fiedri.vibe.core.ui.composables.models.CardItem
import dev.fiedri.vibe.core.ui.theme.VibeTheme

@Composable
fun AlbumsScreen(
    viewModel: albumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    when (val state = uiState) {
        AlbumsUiState.Loading -> {
            LoadingState()
        }
        is AlbumsUiState.Succes -> {
            if (state.cards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No albums found",
                        color = VibeTheme.colors.mutedForeground
                    )
                }
            } else {
                CardGrid(
                    cards = state.cards,
                    onItemClick = { card ->
                        when (card) {
                            is CardItem.Album -> navigator?.navigate(
                                AlbumDetail(id = card.album.id)
                            )
                            is CardItem.Artist -> Unit
                        }
                    }
                )
            }
        }
        is AlbumsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = VibeTheme.colors.destructive
                )
            }
        }
    }

}
