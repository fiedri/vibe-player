package dev.fiedri.vibe.core.data.models

import android.net.Uri

data class SongModel(
    val id: Long,
    val title: String,
    val artistId: Long,
    val artists: String,
    val albumId: Long,
    val trackNumber: Int = 0,
    val album: String,
    val uri: Uri,
    val displayName: String,
    val size: Long,
    val bitrate: Int? = null,
    val albumArtUri: Uri? = null,
    val dateModified: Long,
    val dateAdded: Long,
    val duration: Long,
    )