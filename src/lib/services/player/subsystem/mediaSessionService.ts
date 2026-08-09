import { MediaSession } from "@capgo/capacitor-media-session";
import { Capacitor } from "@capacitor/core";
import { artworkCache } from "$lib/services/artworks";
import type { Song } from "$lib/types/songs";
export class MediaSessionService {
  public onPlayRequest?: () => void;
  public onPreviousTrackRequest?: () => void;
  public onNextTrackRequest?: () => void;
  public onSeekRequest?: (time: number) => void;
  public onPauseRequest?: () => void;
  private handlersInitialized = false;
  private suppressNativePausePush = false;
  private lastPositionSync = 0;
  public get isSuppressingNativePause(): boolean {
    return this.suppressNativePausePush;
  }

  /** Activar antes de que el src del <audio> cambie. */
  public beginNativePauseSuppression(): void {
    this.suppressNativePausePush = true;
  }

  /** Desactivar cuando la reproducción arrancó o el nuevo src cargó. */
  public endNativePauseSuppression(): void {
    this.suppressNativePausePush = false;
  }

  public initMediaSessionHandlers() {
    if (Capacitor.isNativePlatform()) {
      try {
        MediaSession.setActionHandler({ action: "play" }, () =>
          this.onPlayRequest?.(),
        );
        MediaSession.setActionHandler({ action: "pause" }, () =>
          this.onPauseRequest?.(),
        );
        MediaSession.setActionHandler({ action: "previoustrack" }, () =>
          this.onPreviousTrackRequest?.(),
        );
        MediaSession.setActionHandler({ action: "nexttrack" }, () =>
          this.onNextTrackRequest?.(),
        );
        MediaSession.setActionHandler({ action: "seekto" }, (details) => {
          if (details.seekTime !== undefined && details.seekTime !== null) {
            this.onSeekRequest?.(details.seekTime);
          }
        });
      } catch (e) {
        console.warn("Error init handlers:", e);
      }
    }
  }
  public init(defaulImg: string) {
    if (Capacitor.isNativePlatform()) {
      if (!this.handlersInitialized) {
        this.handlersInitialized = true;
        this.initMediaSessionHandlers();
      }
      // Metadata inicial mínima: la notificación arranca intencional en vez de
      // mostrar datos vacíos/stale mientras el servicio foreground ya corre.
      void this.setMetadata(
        {
          id: "default",
          title: "Vibe",
          artists: "",
          album: "",
          image: "/default-cover.png",
          audioUrl: "",
        },
        defaulImg,
      );
      void defaulImg;
    }
  }
  public async setMetadata(song: Song, artworkSrc: string) {
    if (!song) return;
    if (!this.handlersInitialized) {
      void this.initMediaSessionHandlers();
    }

    if (!Capacitor.isNativePlatform()) return;

    const title = song.title || "Sin título";
    const artist = Array.isArray(song.artists)
      ? song.artists.join(", ")
      : song.artists || "Artista desconocido";
    const album = song.album || "Música";

    const cached = song.image ? artworkCache.get(song.image) : undefined;

    try {
      MediaSession.setMetadata({
        title,
        artist,
        album,
        artwork: cached
          ? [{ src: cached, sizes: "512x512" }]
          : [{ src: "", sizes: "512x512" }],
      });

      if (!cached && song.image) {
        const src = artworkSrc;
        if (src && src !== "") {
          MediaSession.setMetadata({
            title,
            artist,
            album,
            artwork: [{ src, sizes: "512x512" }],
          });
        }
      }
    } catch (e) {
      console.warn("Error metadata nativo:", e);
    }
  }
  syncNativePlaybackState(isPlaying: boolean) {
    if (Capacitor.isNativePlatform()) {
      try {
        MediaSession.setPlaybackState({
          playbackState: isPlaying ? "playing" : "paused",
        });
      } catch (e) {}
    }
  }

  updatePositionState(
    position: number,
    duration: number,
    force: boolean = false,
  ) {
    if (duration <= 0 || position > duration || position < 0) return;

    const now = Date.now();
    if (!force && now - this.lastPositionSync < 2000) return;
    this.lastPositionSync = now;

    if (Capacitor.isNativePlatform()) {
      try {
        MediaSession.setPositionState({
          position,
          duration,
          playbackRate: 1.0,
        });
      } catch (e) {}
    }
  }
}
