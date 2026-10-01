package dev.fiedri.vibe.features.search.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.ui.composables.models.CardData
import dev.fiedri.vibe.core.ui.composables.SongCard
import dev.fiedri.vibe.core.ui.composables.models.SongCardUiState
import dev.fiedri.vibe.core.ui.composables.ThumbnailCard
import dev.fiedri.vibe.core.ui.composables.SongOptionsSheet
import dev.fiedri.vibe.core.ui.theme.VibeTheme
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    onBack: () -> Unit = {},
    onSongClick: (SongCardUiState) -> Unit = {},
    onAlbumClick: (CardData) -> Unit = {},
    onArtistClick: (CardData) -> Unit = {}
) {
    /*val allSongs: List<SongCardUiState> = remember {
        List(50) { index ->
            SongCardUiState(
                id = "id ${index + 1}",
                duration = "3:00",
                title = "Titulo ${index + 1}",
                artist = "artista"
            )
        }
    }
    val allAlbums: List<CardData> = remember {
        List(50) { index ->
            CardData(
                id = index,
                name = "Album $index",
                songsCount = index,
                image = R.drawable.default_cover
            )
        }
    }
    val allArtists: List<CardData> = remember {
        List(50) { index ->
            CardData(
                id = index,
                name = "Artista $index",
                songsCount = index,
                image = R.drawable.default_artist
            )
        }
    }

    var query by remember { mutableStateOf("") }
    var songs by remember { mutableStateOf(emptyList<SongCardUiState>()) }
    var albums by remember { mutableStateOf(emptyList<CardData>()) }
    var artists by remember { mutableStateOf(emptyList<CardData>()) }
    var searched by remember { mutableStateOf(false) }
    var optionsFor by remember { mutableStateOf<SongCardUiState?>(null) }

    LaunchedEffect(query) {
        searched = false
        if (query.isEmpty()) {
            songs = emptyList()
            albums = emptyList()
            artists = emptyList()
            return@LaunchedEffect
        }
        delay(500)
        val q = query.lowercase()
        songs = allSongs.filter {
            it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
        }
        albums = allAlbums.filter { it.name.lowercase().contains(q) }
        artists = allArtists.filter { it.name.lowercase().contains(q) }
        searched = true
    }

    Column(
        modifier = Modifier.fillMaxSize().background(VibeTheme.colors.background)
    ) {
        SearchHeader(
            query = query,
            onQueryChange = { query = it },
            onBack = onBack
        )

        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (artists.isNotEmpty()) {
                SearchGroupHeader(title = stringResource(R.string.tabs_artists))
                SearchStrip(cards = artists, onClick = onArtistClick)
            }
            if (albums.isNotEmpty()) {
                SearchGroupHeader(title = stringResource(R.string.tabs_albums))
                SearchStrip(cards = albums, onClick = onAlbumClick)
            }
            if (songs.isNotEmpty()) {
                SearchGroupHeader(title = stringResource(R.string.tabs_songs))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(items = songs, key = { song -> song.id }) { song ->
                        SongCard(
                            song = song,
                            onClick = { onSongClick(song) },
                            onOptionsClick = { optionsFor = song }
                        )
                    }
                }
            }
            if (query.isNotEmpty() && searched &&
                songs.isEmpty() && albums.isEmpty() && artists.isEmpty()
            ) {
                Text(
                    text = stringResource(R.string.no_found),
                    color = VibeTheme.colors.foreground,
                    style = VibeTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
    }

    optionsFor?.let { song ->
        SongOptionsSheet(
            song = song,
            onDismissRequest = { optionsFor = null }
        )
    }*/
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RectangleShape,
                ambientColor = VibeTheme.colors.primary,
                spotColor = VibeTheme.colors.primary
            )
            .background(VibeTheme.colors.background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = VibeTheme.colors.foreground,
                modifier = Modifier.size(24.dp)
            )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = VibeTheme.typography.bodyLarge.copy(
                color = VibeTheme.colors.foreground,
                fontSize = 16.sp,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(VibeTheme.colors.foreground),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .background(VibeTheme.colors.input)
                .border(1.dp, VibeTheme.colors.border, RectangleShape)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            color = VibeTheme.colors.mutedForeground,
                            style = VibeTheme.typography.bodyLarge,
                            fontSize = 16.sp,
                            lineHeight = 20.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun SearchGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = VibeTheme.colors.mutedForeground,
        style = VibeTheme.typography.caption,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.35.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
    )
}

@Composable
private fun SearchStrip(
    cards: List<CardData>,
    onClick: (CardData) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = cards, key = { card -> card.id }) { card ->
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .clickable { onClick(card) }
            ) {
                ThumbnailCard(
                    title = card.name,
                    img = card.image
                )
            }
        }
    }
}
