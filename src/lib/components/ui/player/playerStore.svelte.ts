import type { Song } from "$lib/types/songs";
import { biblioteca } from "$lib/stores/biblioteca.svelte";
import { MediaSession } from "@capgo/capacitor-media-session";
import { Capacitor } from "@capacitor/core";
import {
  artworkCache,
  DEFAULT_COVER_DATA_URL,
  ensureThumbnail,
} from "$lib/services/artworks";
import { cargarEstadoReproductor } from "$lib/services/stores";

class PlayerStore {
  queue = $derived([...biblioteca.songs]);
  currentSong = $state<Song | null>(null);
  isPlaying = $state<boolean>(false);
  isShuffle = $state<boolean>(false);
  volume = $state<number>(1);
  currentTime = $state<number>(0);
  duration = $state<number>(0);

  currentSongIndex = $derived<number | null>(
    Array.isArray(this.queue) && this.currentSong
      ? this.queue.findIndex(
          (el) =>
            el.id === this.currentSong?.id ||
            el.title === this.currentSong?.title,
        )
      : null,
  );
  isOpened = $state<boolean>(false);
  mode = $state<string>("off");
  playTrigger = $state<number>(0);
  public onSeekRequest?: (time: number) => void;
  public onPauseRequest?: () => void;
  private handlersInitialized = false;
  private lastPositionSync = 0;
  private pendingArtwork = new Map<string, Promise<string>>();
  /**
   * Supresión del push 'paused' al MediaSession nativo durante un cambio de
   * src. Cuando el <audio> cambia de src el browser dispara un `pause`
   * transitorio; si ese pause se pushea al nativo, la patch nativa ABANDONA el
   * audio focus y el play() inmediato re-pide focus con el churn abandon→request
   * que en Android devuelve FAILED → la patch pausa la canción recién cambiada.
   * El flag se activa ANTES de tocar currentSong (setSong/loadLastSavedState) y
   * solo bloquea el push de 'paused' proveniente del evento `pause` del <audio>
   * (handlePause). Las pausas intencionales (togglePlay/pause/actionCallback)
   * no pasan por ese camino para el push y quedan intactas.
   */
  private suppressNativePausePush = false;

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

  public init() {
    if (Capacitor.isNativePlatform()) {
      if (!this.handlersInitialized) {
        this.handlersInitialized = true;
        this.initMediaSessionHandlers();
      }
      // Metadata inicial mínima: la notificación arranca intencional en vez de
      // mostrar datos vacíos/stale mientras el servicio foreground ya corre.
      void this.setMetadata({
        id: "default",
        title: "Vibe",
        artists: "",
        album: "",
        image: "/default-cover.png",
        audioUrl: "",
      });
      void this.getArtworkSrc("/default-cover.png");
    }
  }

  public initMediaSessionHandlers() {
    if (Capacitor.isNativePlatform()) {
      try {
        MediaSession.setActionHandler({ action: "play" }, () => this.play());
        MediaSession.setActionHandler({ action: "pause" }, () => this.pause());
        MediaSession.setActionHandler({ action: "previoustrack" }, () =>
          this.previous(),
        );
        MediaSession.setActionHandler({ action: "nexttrack" }, () =>
          this.next(),
        );
        MediaSession.setActionHandler({ action: "seekto" }, (details) => {
          if (details.seekTime !== undefined && details.seekTime !== null) {
            this.currentTime = details.seekTime;

            if (this.onSeekRequest) {
              this.onSeekRequest(details.seekTime);
            }

            this.updatePositionState(details.seekTime, this.duration, true);
          }
        });
      } catch (e) {
        console.warn("Error init handlers:", e);
      }
    }
  }
  public async loadLastSavedState() {
    const lastState = await cargarEstadoReproductor();
    console.log("last-State", lastState);
    if (!lastState) return;

    const lastSong = biblioteca.songs.findIndex(
      (el) => el.id == lastState.trackId,
    );
    if (lastSong === -1) return;
    const restoredSong = biblioteca.songs[lastSong];
    this.beginNativePauseSuppression();
    this.currentTime = lastState.position;
    this.currentSong = restoredSong;
    this.mode = lastState.mode ? lastState.mode : 'off';
    void this.getArtworkSrc(restoredSong.image);


    if (Capacitor.isNativePlatform()) {
      // PlayerState no persiste isPlaying y el <audio> arranca pausado tras el
      // restore, así que la notificación queda en "paused", igual que el estado
      // real del reproductor (syncNativePlaybackState(this.isPlaying) daría el
      // mismo resultado pero explícito queda documentado).
      this.syncNativePlaybackState(false);
      void this.setMetadata(restoredSong);

      const durationParts = restoredSong.duration?.split(":").map(Number);
      const restoredDuration = durationParts?.length
        ? durationParts.reduce((acc, part) => acc * 60 + part, 0)
        : 0;
      if (restoredDuration > 0) {
        // force=true: restauración al arrancar, no debe respetar el throttle
        // de 2s. updatePositionState descarta internamente posiciones inválidas.
        this.updatePositionState(lastState.position, restoredDuration, true);
      }
    }
  }
  public async setMetadata(song: Song) {
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
        const src = await this.getArtworkSrc(song.image);
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

  private async getArtworkSrc(image?: string): Promise<string> {
    if (!image) return DEFAULT_COVER_DATA_URL;

    const cached = artworkCache.get(image);
    if (cached) return cached;

    const pending = this.pendingArtwork.get(image);
    if (pending) return pending;

    const task = ensureThumbnail(image)
      .then((base64) => {
        // ensureThumbnail ya pobló artworkCache (caché compartida). Si
        // devolvió null, NUNCA devolver una URL http(s):// aquí: el plugin
        // la fetchea con HttpURLConnection síncrono sin timeout en el hilo
        // serializado de Capacitor, congelando todos los
        // setMetadata/setPositionState.
        return base64 || DEFAULT_COVER_DATA_URL;
      })
      .finally(() => this.pendingArtwork.delete(image));

    this.pendingArtwork.set(image, task);
    return task;
  }

  syncNativePlaybackState(isPlaying: boolean = this.isPlaying) {
    if (Capacitor.isNativePlatform()) {
      try {
        MediaSession.setPlaybackState({
          playbackState: isPlaying ? "playing" : "paused",
        });
      } catch (e) {}
    }
  }

  updatePositionState(
    position: number = this.currentTime,
    duration: number = this.duration,
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

  setSong(song: Song) {
    // Antes de cambiar el src: suprime el `pause` transitorio del <audio>
    // para no pushear 'paused' al nativo (evita el churn de audio focus).
    this.beginNativePauseSuppression();
    this.currentSong = song;
    this.isPlaying = true;
    this.currentTime = 0;
    this.duration = 0;
    this.playTrigger++;
    console.log(song.image);
    if (Capacitor.isNativePlatform()) {
      if (!this.handlersInitialized) {
        this.handlersInitialized = true;
        this.initMediaSessionHandlers();
      }
      this.syncNativePlaybackState(true);
      this.updatePositionState(0, this.duration, true);
      void this.setMetadata(song);
    }

    // Precalentamiento no bloqueante: mientras el usuario escucha, el
    // artwork de la canción siguiente (y anterior si difiere) se procesa en
    // idle. Al avanzar, el thumbnail ya está en disco + caché en memoria y
    // setMetadata llega directo a la notificación sin lecturas nativas.
    this.prewarmArtworkAdyacente();
  }

  /**
   * Precalienta el artwork adyacente (siguiente/anterior) durante el idle.
   * Fire-and-forget: nunca se espera en el hot path de setSong.
   */
  private prewarmArtworkAdyacente() {
    if (this.currentSongIndex === null || this.currentSongIndex === -1) return;

    const siguiente = this.queue[this.currentSongIndex + 1]?.image;
    const anterior = this.queue[this.currentSongIndex - 1]?.image;
    const actual = this.currentSong?.image;

    const aPrecalentar = [siguiente, anterior].filter(
      (img): img is string => !!img && img !== actual && !artworkCache.has(img),
    );
    if (aPrecalentar.length === 0) return;

    const precalentar = () => {
      for (const img of aPrecalentar) {
        void ensureThumbnail(img).catch(() => {});
      }
    };

    // requestIdleCallback con fallback a setTimeout para WebViews viejas.
    if (typeof requestIdleCallback === "function") {
      requestIdleCallback(precalentar, { timeout: 2000 });
    } else {
      setTimeout(precalentar, 250);
    }
  }
  shuffle(array) {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }

    return shuffled;
  }

  togglePlay() {
    if (!this.currentSong) return;
    this.isPlaying = !this.isPlaying;
    // Si la nueva intención es pausar, el pause real va por el callback al
    // <audio>: el $effect de player.svelte ya no pausa, solo reproduce.
    if (!this.isPlaying) {
      this.onPauseRequest?.();
    }
    this.syncNativePlaybackState();
    this.updatePositionState(this.currentTime, this.duration, true);
  }
  toggleShuffle() {
    this.isShuffle = !this.isShuffle;
    if (this.isShuffle) {
      const currentSong = this.currentSong;
      this.queue = this.shuffle(biblioteca?.songs);
      if (currentSong) {
        this.queue = this.queue.filter((song) => song.id !== currentSong.id);
        this.queue.unshift(currentSong);
      }
    } else {
      const currentSong = this.currentSong;
      this.queue = [...biblioteca.songs];
    }
  }

  play() {
    if (this.currentSong) {
      this.isPlaying = true;
      this.syncNativePlaybackState();
      this.updatePositionState(this.currentTime, this.duration, true);
    }
  }

  pause() {
    this.isPlaying = false;
    // Pausa intencional → callback directo al <audio> (el $effect no pausa).
    this.onPauseRequest?.();
    this.syncNativePlaybackState();
    this.updatePositionState(this.currentTime, this.duration, true);
  }

  next() {
    if (this.mode == "one" && this.currentSong) {
      this.currentTime = 0;
      this.playTrigger++;
      this.onSeekRequest?.(0);
      this.syncNativePlaybackState(true);
      this.updatePositionState(0, this.duration, true);
      return;
    } else if (this.mode == "all" && this.currentSong) {
      if ((this.queue.length-1) <= this.currentSongIndex) {
        this.setSong(this.queue[0]);
        return
      }


    }
    if (this.currentSongIndex === null || this.currentSongIndex === -1) return;
    const nextSong = this.queue[this.currentSongIndex + 1];
    if (nextSong) this.setSong(nextSong);
  }

  previous() {
    if (this.mode == "one" && this.currentSong) {
      this.currentTime = 0;
      this.playTrigger++;
      this.onSeekRequest?.(0);
      this.syncNativePlaybackState(true);
      this.updatePositionState(0, this.duration, true);
      return;
    }
    if (this.currentSongIndex === null || this.currentSongIndex <= 0) return;
    const previousSong = this.queue[this.currentSongIndex - 1];
    if (previousSong) this.setSong(previousSong);
  }
}

export const player = new PlayerStore();
