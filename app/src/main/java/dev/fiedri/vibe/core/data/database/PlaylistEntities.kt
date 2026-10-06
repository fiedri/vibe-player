package dev.fiedri.vibe.core.data.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter


enum class Category{
    USER, FAVORITE
}



class CategoryConverter {
    @TypeConverter
    fun fromCategory(category: Category?): String? {
        return category?.name
    }

    @TypeConverter
    fun toCategory(value: String?): Category? {
        return value?.let {
            try {
                enumValueOf<Category>(it)
            } catch (e: IllegalArgumentException) {
                Category.USER
            }
        }
    }
}
@Entity(
    tableName = "playlists",
    indices = [Index(value = ["name"])]
)
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "dateCreated", defaultValue = "CURRENT_TIMESTAMP") val dateCreated: String,
    @ColumnInfo(name = "category") val category: Category
)

@Entity(tableName = "playlists_songs",
    primaryKeys = ["playlist_id", "song_id"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlist_id"])])
data class PlaylistSongs(
    @ColumnInfo(name = "playlist_id") val playlistId: Int,
    @ColumnInfo(name = "song_id") val songId: Long
)

data class PlaylistWithSongIds(
    @Embedded
    val playlist: Playlist,

    @Relation(
        parentColumn = "id",
        entityColumn = "playlist_id"
    )
    val playlistSongs: List<PlaylistSongs>
) {
    val songIds: List<Long>
        get() = playlistSongs.map { it.songId }
}