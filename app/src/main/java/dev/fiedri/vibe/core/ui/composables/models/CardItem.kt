package dev.fiedri.vibe.core.ui.composables.models

import android.net.Uri
import dev.fiedri.vibe.core.data.models.AlbumModel


sealed interface CardItem {
    val key: String

    data class Album(val album: AlbumModel) : CardItem {
        override val key: String get() = "album-${album.id}"
    }

    data class Artist(
        val id: Long,
        val name: String,
        val artUri: Uri?,
        val songsCount: Int
    ) : CardItem {
        override val key: String get() = "artist-${id}"
    }
}
