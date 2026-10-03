package dev.fiedri.vibe.core.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Home: NavKey

@Serializable
data class AlbumDetail(val id: Long) : NavKey
@Serializable
data class ArtistDetail(val name: String): NavKey
@Serializable
data class PlaylistDetail(val id: Int): NavKey
@Serializable
data object Search: NavKey
@Serializable
data object Settings: NavKey