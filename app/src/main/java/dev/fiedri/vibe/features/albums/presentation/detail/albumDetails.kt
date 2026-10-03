package dev.fiedri.vibe.features.albums.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fiedri.vibe.core.ui.LoadingState
import dev.fiedri.vibe.core.ui.LocalNavigator
import dev.fiedri.vibe.core.ui.composables.DetailHeader
import dev.fiedri.vibe.core.ui.composables.DetailResources
import dev.fiedri.vibe.core.ui.composables.DetailsScreen
import dev.fiedri.vibe.core.ui.composables.EntityType

@Composable
fun AlbumDetailsScreen(
    viewModel: AlbumDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current


    when(val state = uiState){
        is AlbumDetailUiState.Loading -> LoadingState()
        is AlbumDetailUiState.Success ->{
            DetailsScreen(
                header = DetailHeader(name = state.albumName, image = state.albumArt),
                type = EntityType.ALBUM,
                resources = DetailResources(state.songs),
                onBack = { navigator?.goBack() },
                onPlay = {},
                onShuffle = {}
            )
        }
        is AlbumDetailUiState.Error ->{
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No se pudo cargar el álbum")
                Button(onClick = { viewModel.loadAlbumDetail(viewModel.albumId) }) {
                    Text("Reintentar")
                }
            }
        }
    }
}
