package dev.fiedri.vibe.features.songs.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fiedri.vibe.core.data.AudioStoreDataSource
import dev.fiedri.vibe.core.data.models.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioStoreDataSource: AudioStoreDataSource
) {
    private val _songs = MutableStateFlow<List<SongModel>?>(null)
    val songs: StateFlow<List<SongModel>?> = _songs.asStateFlow()

    suspend fun getSongs(forceRefresh: Boolean = false): List<SongModel>{
        val cached = _songs.value
        if (cached != null && !forceRefresh) {
            return cached
        }
        val freshSongs = audioStoreDataSource.getSongs()
        _songs.value = freshSongs
        return freshSongs
    }

    suspend fun getSongsByAlbumId(albumId: Long): List<SongModel> {
        return _songs.value
            ?.filter { it.albumId == albumId }
            ?.sortedBy { it.trackNumber }
            ?: emptyList()
    }
    suspend fun getSongById(id: Long): SongModel?{
        val song = withContext(Dispatchers.Default){
            _songs.value?.find { it.id == id }
        }
        return song
    }
}