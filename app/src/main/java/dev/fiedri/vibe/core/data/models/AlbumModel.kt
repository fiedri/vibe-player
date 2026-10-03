package dev.fiedri.vibe.core.data.models

import android.net.Uri

data class AlbumModel(
    val id: Long,
    val albumName: String,
    val albumArt: Uri,
    val numberOfSongs: Int,
)
