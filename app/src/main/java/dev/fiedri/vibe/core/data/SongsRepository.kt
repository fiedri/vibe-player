package dev.fiedri.vibe.core.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fiedri.vibe.core.data.models.SongModel
import dev.fiedri.vibe.features.player.presentation.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongsRepository @Inject constructor(// inject contruye el objeto automaticamente
    @ApplicationContext private val context: Context,
    private val audioStoreDataSource: AudioStoreDataSource
) {
    private var cachedSongs: List<SongModel> = emptyList()

    suspend fun getSongs(forceRefresh: Boolean = false): List<SongModel>{
        if (cachedSongs.isNotEmpty()) {
        return cachedSongs
        }
        cachedSongs = audioStoreDataSource.getSongs()
        return cachedSongs
    }
    suspend fun getSongById(id: Long): SongModel?{
        return cachedSongs.find { it.id == id }
    }
}
