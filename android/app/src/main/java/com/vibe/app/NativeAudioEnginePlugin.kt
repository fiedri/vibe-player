package com.vibe.app

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import com.capgo.mediasession.MediaSessionService
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
    private var playbackListener: MediaSessionService.PlaybackListener? = null

    // Set by restoreLoadPosition, consumed by the very next setSong call (and
    // cleared immediately) so a stale value can never apply to a later,
    // unrelated track.
    @Volatile
    private var pendingPositionMs: Long? = null

    @OptIn(UnstableApi::class)
    override fun load() {
        super.load()
        activity.runOnUiThread {
            val renderersFactory = DefaultRenderersFactory(context).apply {
                setEnableAudioFloatOutput(true)

            }
            val exoPlayer = ExoPlayer.Builder(context, renderersFactory)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .setHandleAudioBecomingNoisy(true)
                .build()

            player = exoPlayer

            val provider = object : MediaSessionService.ExoPlayerProvider {
                override fun getPositionMs(): Long = player?.currentPosition ?: 0L
                override fun getDurationMs(): Long {
                    val dur = player?.duration ?: 0L
                    return if (dur != C.TIME_UNSET && dur > 0) dur else 0L
                }
                override fun isPlaying(): Boolean = player?.isPlaying == true
                override fun seekTo(positionMs: Long) {
                    activity.runOnUiThread {
                        player?.seekTo(positionMs)
                    }
                }
                override fun play() {
                    activity.runOnUiThread {
                        player?.play()
                    }
                }
                override fun pause() {
                    activity.runOnUiThread {
                        player?.pause()
                    }
                }
                override fun setListener(listener: MediaSessionService.PlaybackListener?) {
                    playbackListener = listener
                }
            }
            MediaSessionService.setPlayerProvider(provider)

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    notifyMediaSessionPlaybackChanged()
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
                            notifyListeners("ended", JSObject())
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    notifyMediaSessionPlaybackChanged()
                    val data = JSObject()
                    data.put("isPlaying", isPlaying)
                    data.put("currentTime", (player?.currentPosition?: 0L) / 1000.0)
                    notifyListeners("isPlayingChange", data)
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int,
                ) {
                    notifyMediaSessionPlaybackChanged(newPosition.positionMs)
                    if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                        val data = JSObject()
                        data.put("currentTime", newPosition.positionMs / 1000.0)
                        notifyListeners("seeked", data)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    notifyMediaSessionPlaybackChanged()
                    val data = JSObject()
                    data.put("message", error.message ?: "Unknown playback error")
                    notifyListeners("error", data)
                }
            })
        }
    }

    private fun notifyMediaSessionPlaybackChanged(targetPositionMs: Long? = null) {
        val exo = player ?: return
        val duration = exo.duration
        val durationMs = if (duration != C.TIME_UNSET && duration > 0) duration else 0L
        val pos = targetPositionMs ?: exo.currentPosition
        playbackListener?.onPlaybackChanged(exo.isPlaying, pos, durationMs)
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

        activity.runOnUiThread {
            val exoPlayer = player
            if (exoPlayer == null) {
                call.reject("NativeAudioEngine is not loaded")
                return@runOnUiThread
            }
            try {
                // setMediaItem ya reemplaza el source anterior y resetea la
                // posición: NO hace falta stop() + clearMediaItems() (churn
                // pesado) ni en ráfagas de cambios rápidos ni en el flujo
                // normal. prepare() arranca el load del nuevo contenido.
                val startPositionMs = pendingPositionMs
                pendingPositionMs = null
                val mediaItem = MediaItem.fromUri(Uri.parse(uriString))
                if (startPositionMs != null) {
                    exoPlayer.setMediaItem(mediaItem, startPositionMs)
                } else {
                    exoPlayer.setMediaItem(mediaItem)
                }
                exoPlayer.prepare()
                call.resolve()
            } catch (e: Exception) {
                val data = JSObject()
                data.put("message", e.message ?: "Failed to load song")
                notifyListeners("error", data)
                call.resolve()
            }
        }
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

    override fun handleOnDestroy() {
        MediaSessionService.setPlayerProvider(null)
        playbackListener = null
        activity.runOnUiThread {
            player?.stop()
            player?.release()
            player = null
        }
        super.handleOnDestroy()
    }
}
