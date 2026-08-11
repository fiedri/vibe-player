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
  public onStopTrackRequest?: () => void;
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
        MediaSession.setActionHandler({ action: "stop" }, () =>
          this.onStopTrackRequest?.(),
        );
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

  /**
   * Invalida el ancla nativa en CADA cambio de track: congela la barra en ~0
   * (PAUSED + posición epsilon) en vez de dejar que el PlaybackState viejo
   * (PLAYING + posición stale) siga extrapolando mientras el nuevo src carga.
   * La barra solo se reactiva cuando onplay/handleLoadedMetadata re-anclan.
   */
  public resetNativePosition(song: Song | null, elementDuration: number = 0) {
    if (!Capacitor.isNativePlatform()) return;

    this.syncNativePlaybackState(false);

    let finiteDuration = this.#songDurationToSeconds(song?.duration);
    if (
      !(finiteDuration > 0) &&
      Number.isFinite(elementDuration) &&
      elementDuration > 0
    ) {
      finiteDuration = elementDuration;
    }
    if (!(finiteDuration > 0)) return;

    // 0.001 → 1ms nativo: vence el equality-guard del servicio (position !=
    // newPosition) aunque el ancla vieja esté exactamente en 0.
    this.updatePositionState(0.001, finiteDuration, true);
  }

  /** Song.duration es ms (número) o "mm:ss" (string). Devuelve segundos. */
  #songDurationToSeconds(duration: number | string | undefined): number {
    if (
      typeof duration === "number" &&
      Number.isFinite(duration) &&
      duration > 0
    ) {
      return duration / 1000;
    }
    if (typeof duration === "string" && duration.includes(":")) {
      const parts = duration.split(":").map(Number);
      return parts.reduce((acc, part) => acc * 60 + part, 0);
    }
    return 0;
  }

  updatePositionState(
    position: number,
    duration: number,
    force: boolean = false,
  ) {
    if (
      duration <= 0 ||
      position < 0 ||
      position > duration ||
      !Number.isFinite(duration) ||
      !Number.isFinite(position)
    )
      return;

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
