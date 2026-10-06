package dev.fiedri.vibe.features.playlists.data

import dev.fiedri.vibe.core.data.database.PlaylistDao
import dev.fiedri.vibe.core.data.database.PlaylistWithSongCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {

    fun loadPlaylists(): Flow<List<PlaylistWithSongCount>> {
        return playlistDao.getPlaylists()
    }
}