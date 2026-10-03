package dev.fiedri.vibe.core.data

import android.content.ContentUris
import android.net.Uri
import androidx.core.net.toUri

fun getAlbumArtUri(albumId: Long): Uri {
    return ContentUris.withAppendedId(
        "content://media/external/audio/albumart".toUri(),
        albumId
    )
}