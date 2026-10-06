package dev.fiedri.vibe.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao{
    @Query("""
        SELECT playlists.id AS id, playlists.name AS name,
         COUNT(playlists_songs.song_id) AS songsCount,
         playlists.category as category
         FROM playlists
         LEFT JOIN playlists_songs ON playlists.id = playlists_songs.playlist_id
         GROUP BY playlists.id
    """)
    fun getPlaylists(): Flow<List<PlaylistWithSongCount>>
    // usar en el repository para filtrar y crear playlist con nombres incrementales
    @Query("SELECT name FROM playlists WHERE name LIKE :query")
    suspend fun getExistingNamesLike(query: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)


    @Query("SELECT song_id FROM playlists_songs WHERE playlist_id = :playlistId")
    suspend fun getSongIdsByPlaylistId(playlistId: Int): List<Long>

    @Insert
    suspend fun addSongToPlaylist(playlistSongs: PlaylistSongs)

    @Delete
    suspend fun removeSongFromPlaylist(playlistSongs: PlaylistSongs)

    @Query("""
        DELETE FROM playlists_songs WHERE song_id = :songId
    """)
    suspend fun removeSongFromAllPlaylist(songId: Long)

    @Query("""
        DELETE FROM playlists_songs 
        WHERE playlist_id = :playlistId 
          AND song_id IN (:songIds)
    """)
    suspend fun removeManySongsFromPlaylist(playlistId: Int, songIds: List<Long>)

    @Query("""
        DELETE FROM playlists_songs
        WHERE song_id IN (:songIds)
    """)
    suspend fun removeManySongsFromAllPlaylist(songIds: List<Long>)

    @Insert
    suspend fun addManySongsToPlaylists(items: List<PlaylistSongs>)
}


data class PlaylistWithSongCount(
    val id: Int,
    val name: String,
    val songsCount: Int
)

data class PlaylistWithSongs(
    val id: Int,
    val name: String,
    val dateCreated: String,
    val playlistSongs: List<Long>
)