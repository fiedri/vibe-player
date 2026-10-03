package dev.fiedri.vibe.core.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

import dev.fiedri.vibe.core.data.models.SongModel



class AudioStoreDataSource @Inject constructor(@ApplicationContext private val context: Context) {
    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.BITRATE,
        MediaStore.Audio.Media.DATE_MODIFIED,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.DURATION
    )

    suspend fun getSongs(): List<SongModel> {
        return withContext(Dispatchers.IO) {
            val songs = mutableListOf<SongModel>()
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
            val selectionArgs = arrayOf("30000")
            val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                    val displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val bitrateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
                    val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                    val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val title = cursor.getString(titleCol) ?: "Desconocido"
                        val artistName = cursor.getString(artistCol) ?: "Artista desconocido"
                        val albumName = cursor.getString(albumCol) ?: "Álbum desconocido"
                        val duration = cursor.getLong(durationCol)
                        val size = cursor.getLong(sizeCol)
                        val dateAdded = cursor.getLong(dateAddedCol)
                        val rawDateModified = cursor.getLong(dateModifiedCol)
                        val albumId = cursor.getLong(albumIdCol)
                        val artistId = cursor.getLong(artistIdCol)
                        val trackNumber = cursor.getInt(trackCol)
                        val displayName = cursor.getString(displayNameCol) ?: ""

                        val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                        val albumArtUri = getAlbumArtUri(albumId)
                        /*
                        TODO
                        * implementar una base de datos
                        * para guardar artistas y sus canciones asociadas mediantes id
                        * regex para separar colaboraciones
                         */
                        songs.add(
                            SongModel(
                                id = id,
                                title = title,
                                artistId = artistId,
                                artists = artistName,
                                albumId = albumId,
                                album = albumName,
                                uri = songUri,
                                displayName = displayName,
                                size = size,
                                albumArtUri = albumArtUri,
                                dateModified = rawDateModified,
                                dateAdded = dateAdded,
                                trackNumber = trackNumber,
                                duration = duration,

                            )
                        )
                    }
                }
            } catch (e: Exception) {
                throw e
            }

            songs
        }
    }


}

