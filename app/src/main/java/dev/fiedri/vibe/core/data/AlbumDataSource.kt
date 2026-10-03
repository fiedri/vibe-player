package dev.fiedri.vibe.core.data

import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fiedri.vibe.core.data.models.AlbumModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlbumDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val projection = arrayOf(
        MediaStore.Audio.Albums.ALBUM_ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Albums.NUMBER_OF_SONGS
    )
    private val sortOrder = "${MediaStore.Audio.Albums.ALBUM} ASC"
    suspend fun getAlbums(): List<AlbumModel>{
        return withContext(Dispatchers.IO){
            val albums =mutableListOf<AlbumModel>()

            context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use {
                cursor ->

                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val numberCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "<Unknown>"
                    val numberOfSongs = cursor.getInt(numberCol)
                    val albumArtUri = getAlbumArtUri(albumId = id)
                    albums.add(
                        AlbumModel(
                            id = id,
                            albumName = name,
                            albumArt = albumArtUri,
                            numberOfSongs = numberOfSongs
                        )
                    )
                }
            }
            albums
        }
    }
}