package dev.fiedri.vibe.features.albums.data


import javax.inject.Inject
import javax.inject.Singleton
import dev.fiedri.vibe.core.data.AlbumDataSource
import dev.fiedri.vibe.core.data.models.AlbumModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AlbumRepository @Inject constructor (
    private val albumDataSource: AlbumDataSource
){
    private val _albums = MutableStateFlow<List<AlbumModel>?>(null)
    val albums: StateFlow<List<AlbumModel>?> = _albums.asStateFlow()

    suspend fun getAlbums(forceRefresh: Boolean = false): List<AlbumModel>{
        val cached = _albums.value
        if (cached != null && !forceRefresh){
            return cached
        }
        val freshAlbums = albumDataSource.getAlbums()
        _albums.value = freshAlbums
        return freshAlbums
    }

    suspend fun getAlbumById(albumId: Long): AlbumModel?{
        return _albums.value?.find { it.id == albumId }
    }
}