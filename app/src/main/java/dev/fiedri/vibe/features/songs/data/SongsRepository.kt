package dev.fiedri.vibe.features.songs.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fiedri.vibe.core.data.AudioStoreDataSource
import dev.fiedri.vibe.core.data.models.SongModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioStoreDataSource: AudioStoreDataSource
) {
    private val _songs = MutableStateFlow<List<SongModel>>(emptyList())
    val songs: StateFlow<List<SongModel>> = _songs.asStateFlow()

    suspend fun getSongs(forceRefresh: Boolean = false): List<SongModel>{
        if (_songs.value.isNotEmpty() && !forceRefresh) {
        return _songs.value
        }
        val freshSongs = audioStoreDataSource.getSongs()
        _songs.value = freshSongs
        return freshSongs
    }
    suspend fun getSongById(id: Long): SongModel?{
        return _songs.value.find { it.id == id }
    }
}