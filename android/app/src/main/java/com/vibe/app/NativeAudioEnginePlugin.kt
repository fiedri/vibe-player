package com.vibe.app

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

/**
 * Native playback engine backed by Media3 ExoPlayer. Mirrors the surface of
 * the web AudioEngine (setSong/play/pause/seek/setVolume/restoreLoadPosition)
 * and reports state back to JS via notifyListeners instead of a return value,
 * the same split the <audio> element uses between method calls and DOM events.
 */
@CapacitorPlugin(name = "NativeAudioEngine")
class NativeAudioEnginePlugin : Plugin() {

    private var player: ExoPlayer? = null

    // Set by restoreLoadPosition, consumed by the very next setSong call (and
    // cleared immediately) so a stale value can never apply to a later,
    // unrelated track.
    @Volatile
    private var pendingPositionMs: Long? = null

    private val positionHandler = Handler(Looper.getMainLooper())
    private var positionRunnable: Runnable? = null

    override fun load() {
        super.load()
        activity.runOnUiThread {
            val exoPlayer = ExoPlayer.Builder(context)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .build()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            val durationMs = exoPlayer.duration
                            if (durationMs != C.TIME_UNSET) {
                                // currentPosition already reflects any restored start
                                // position passed to setMediaItem, so there's no separate
                                // seek-on-ready step and no window where duration is known
                                // but position is still stale at 0.
                                val data = JSObject()
                                data.put("duration", durationMs / 1000.0)
                                data.put("currentTime", exoPlayer.currentPosition / 1000.0)
                                notifyListeners("loadedMetadata", data)
                            }
                        }
                        Player.STATE_ENDED -> {
                            stopPositionUpdates()
                            notifyListeners("ended", JSObject())
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) startPositionUpdates() else stopPositionUpdates()
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int,
                ) {
                    if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                        val data = JSObject()
                        data.put("currentTime", newPosition.positionMs / 1000.0)
                        notifyListeners("seeked", data)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    val data = JSObject()
                    data.put("message", error.message ?: "Unknown playback error")
                    notifyListeners("error", data)
                }
            })
            player = exoPlayer
        }
    }

    @PluginMethod
    fun setSong(call: PluginCall) {
        val uriString = call.getString("uri")
        if (uriString.isNullOrEmpty()) {
            call.reject("A 'uri' is required to set the song")
            return
        }
        // Read and clear immediately: this position belongs only to the song
        // being set right now, never to whatever setSong call comes next.
        val startPositionMs = pendingPositionMs
        pendingPositionMs = null
        activity.runOnUiThread {
            val exoPlayer = player ?: return@runOnUiThread
            stopPositionUpdates()
            try {
                val mediaItem = MediaItem.fromUri(Uri.parse(uriString))
                if (startPositionMs != null) {
                    exoPlayer.setMediaItem(mediaItem, startPositionMs)
                } else {
                    exoPlayer.setMediaItem(mediaItem)
                }
                exoPlayer.prepare()
            } catch (e: Exception) {
                val data = JSObject()
                data.put("message", e.message ?: "Failed to load song")
                notifyListeners("error", data)
            }
        }
        call.resolve()
    }

    @PluginMethod
    fun restoreLoadPosition(call: PluginCall) {
        val position = call.getDouble("position")
        if (position == null) {
            call.reject("A 'position' (seconds) is required")
            return
        }
        pendingPositionMs = (position * 1000).toLong()
        call.resolve()
    }

    @PluginMethod
    fun play(call: PluginCall) {
        activity.runOnUiThread { player?.play() }
        call.resolve()
    }

    @PluginMethod
    fun pause(call: PluginCall) {
        activity.runOnUiThread { player?.pause() }
        call.resolve()
    }

    @PluginMethod
    fun seek(call: PluginCall) {
        val time = call.getDouble("time")
        if (time == null) {
            call.reject("A 'time' (seconds) is required")
            return
        }
        activity.runOnUiThread { player?.seekTo((time * 1000).toLong()) }
        call.resolve()
    }

    @PluginMethod
    fun setVolume(call: PluginCall) {
        val volume = call.getDouble("volume")
        if (volume == null) {
            call.reject("A 'volume' is required")
            return
        }
        activity.runOnUiThread { player?.volume = volume.toFloat() }
        call.resolve()
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        val runnable = object : Runnable {
            override fun run() {
                val exoPlayer = player ?: return
                val data = JSObject()
                data.put("currentTime", exoPlayer.currentPosition / 1000.0)
                notifyListeners("timeUpdate", data)
                positionHandler.postDelayed(this, POSITION_UPDATE_INTERVAL_MS)
            }
        }
        positionRunnable = runnable
        positionHandler.post(runnable)
    }

    private fun stopPositionUpdates() {
        positionRunnable?.let { positionHandler.removeCallbacks(it) }
        positionRunnable = null
    }

    override fun handleOnDestroy() {
        stopPositionUpdates()
        activity.runOnUiThread {
            player?.release()
            player = null
        }
        super.handleOnDestroy()
    }

    private companion object {
        const val POSITION_UPDATE_INTERVAL_MS = 250L
    }
}
