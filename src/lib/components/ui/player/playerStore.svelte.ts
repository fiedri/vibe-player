import type { Song } from "$lib/types/songs";
import { biblioteca } from "$lib/stores/biblioteca.svelte";
import { MediaSession } from "@capgo/capacitor-media-session";
import { Capacitor } from "@capacitor/core";
import defaultCoverAsset from "./default-cover.png";

class PlayerStore {
  currentSong = $state<Song | null>(null);
  isPlaying = $state<boolean>(false);
  volume = $state<number>(0.8);
  currentTime = $state<number>(0);
  duration = $state<number>(0);
  currentSongIndex = $derived<number | null>(
    biblioteca.songs.findIndex(
      (el) =>
        el.id === this.currentSong?.id || el.title === this.currentSong?.title,
    ),
  );
  isOpened = $state<boolean>(false);

  private handlersInitialized = false;
  // Añadimos esto para evitar saturar el bridge de Capacitor
  private lastPositionSync = 0;

  public async initMediaSessionHandlers() {
    // ... (Tu código actual de initMediaSessionHandlers se mantiene igual)
    // Solo asegúrate de que el handler de 'seekto' pase force=true
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
            // Forzamos la actualización al hacer seek manual
            this.updatePositionState(details.seekTime, this.duration, true);
          }
        });
      } catch (e) {
        console.warn("Error init handlers:", e);
      }
    }
    // ... (Mantén tu código web igual)
  }

  public async setMetadata(song: Song) {
    if (!song) return;
    if (!this.handlersInitialized) {
      void this.initMediaSessionHandlers();
    }

    //let coverUrl = song.image
    //? Capacitor.convertFileSrc(song.image)
    //: window.location.origin + defaultCoverAsset;
    // Simplifica el artwork list para no enviar tanta data redundante al plugin
    //const artworkList = [
    //{ src: coverUrl, sizes: '512x512' }
    //];

    if (Capacitor.isNativePlatform()) {
      try {
        await MediaSession.setMetadata({
          title: song.title || "Sin título",
          artist: Array.isArray(song.artists)
            ? song.artists.join(", ")
            : song.artists || "Artista desconocido",
          album: song.album || "Música",
        });
      } catch (e) {
        console.warn("Error metadata nativo:", e);
      }
    }
  }

  async syncNativePlaybackState(isPlaying: boolean = this.isPlaying) {
    if (Capacitor.isNativePlatform()) {
      try {
        await MediaSession.setPlaybackState({
          playbackState: isPlaying ? "playing" : "paused",
        });
      } catch (e) {}
    }
  }

  // Modificado: Agregamos "force" y un limitador (throttle)
  async updatePositionState(
    position: number = this.currentTime,
    duration: number = this.duration,
    force: boolean = false,
  ) {
    if (duration <= 0 || position > duration || position < 0) return;

    const now = Date.now();
    // Si no es forzado, limitamos la actualización a 1 vez cada 2 segundos.
    // Android interpola el tiempo solo, no hace falta mandarle el segundo a segundo.
    if (!force && now - this.lastPositionSync < 2000) return;
    this.lastPositionSync = now;

    if (Capacitor.isNativePlatform()) {
      try {
        await MediaSession.setPositionState({
          position,
          duration,
          playbackRate: 1.0,
        });
      } catch (e) {}
    }
  }

  // Modificado para esperar los metadatos antes de lanzar el play state
  setSong(song: Song) {
    this.currentSong = song;
    this.isPlaying = true;
    this.currentTime = 0;

    // Primero enviamos metadata, luego forzamos estado y posición inicial
    //    this.setMetadata(song).then(() => {
    //     this.syncNativePlaybackState(true);
    //    this.updatePositionState(0, this.duration, true);
    // });
  }

  async togglePlay() {
    if (!this.currentSong) return;
    this.isPlaying = !this.isPlaying;
    await this.syncNativePlaybackState();
    // Forzar actualización de posición para que Android sepa exactamente dónde se pausó/reanudó
    this.updatePositionState(this.currentTime, this.duration, true);
  }

  async play() {
    if (this.currentSong) {
      this.isPlaying = true;
      await this.syncNativePlaybackState();
      this.updatePositionState(this.currentTime, this.duration, true);
    }
  }

  async pause() {
    this.isPlaying = false;
    await this.syncNativePlaybackState();
    this.updatePositionState(this.currentTime, this.duration, true);
  }

  next() {
    if (this.currentSongIndex === null || this.currentSongIndex === -1) return;
    const nextSong = biblioteca.songs[this.currentSongIndex + 1];
    if (nextSong) this.setSong(nextSong);
  }

  previous() {
    if (this.currentSongIndex === null || this.currentSongIndex <= 0) return;
    const previousSong = biblioteca.songs[this.currentSongIndex - 1];
    if (previousSong) this.setSong(previousSong);
  }
}

export const player = new PlayerStore();
