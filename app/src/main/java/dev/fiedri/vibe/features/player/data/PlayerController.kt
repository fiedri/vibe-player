package dev.fiedri.vibe.features.player.data

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fiedri.vibe.core.data.models.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface QueueContext {
    data class Album(val albumId: Long) : QueueContext
    data class Playlist(val playlistId: String) : QueueContext
    data class Artist(val artistId: Long) : QueueContext
    data object AllSongs : QueueContext
}
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    var mediaController: MediaController? = null
        private set
    var currentQueueContext: QueueContext? = null

    fun create(onConnected: (MediaController) -> Unit = {}) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.let { controller ->
                    onConnected(controller)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    suspend fun playSong(
        songs: List<SongModel>,
        initialIndex: Int = 0,
        queueContext: QueueContext
    ) = withContext(Dispatchers.Default) {
        val controller = mediaController ?: return@withContext
        if (queueContext == currentQueueContext) {
            withContext(Dispatchers.Main) {
                if (controller.currentMediaItemIndex != initialIndex) {
                    controller.seekToDefaultPosition(initialIndex)
                }
                if (!controller.isPlaying) controller.play()
            }
            return@withContext
        }

        currentQueueContext = queueContext
        val mediaItems = withContext(Dispatchers.Default) {
            songs.map { it.toMediaItem() }
        }


        withContext(Dispatchers.Main) {
            controller.setMediaItems(mediaItems, initialIndex, 0L)
            controller.prepare()
            controller.play()
        }

    }
    suspend fun playNext(){
        withContext(Dispatchers.Main){
            mediaController?.seekToNext()
        }

    }
    suspend fun playPrevious(){
        withContext(Dispatchers.Main){
            mediaController?.seekToPrevious()
        }

    }
    suspend fun play(){
        withContext(Dispatchers.Main){
            mediaController?.play()
        }
    }
    suspend fun pause(){
        withContext(Dispatchers.Main){
            mediaController?.pause()
        }
    }
    suspend fun seekTo(ms: Long){
        withContext(Dispatchers.Main){
            mediaController?.seekTo(ms)
        }
    }
    suspend fun toggleShuffle(activate: Boolean? = null){
        withContext(Dispatchers.Main){
            if(activate != null){
                mediaController?.shuffleModeEnabled?.let { mediaController?.shuffleModeEnabled= activate }
            }else{
                mediaController?.shuffleModeEnabled?.let { mediaController?.shuffleModeEnabled = !it }
            }
        }
    }

    suspend fun alterRepetitionMode() {
        withContext(Dispatchers.Main) {
            val controller = mediaController ?: return@withContext

            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }
}
private fun SongModel.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artists)
        .setAlbumTitle(album)
        .setTrackNumber(trackNumber)
        .setArtworkUri(albumArtUri)
        .setDisplayTitle(title)
        .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(metadata)
        .build()
}