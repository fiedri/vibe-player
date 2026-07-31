import type { Song } from "$lib/types/songs";
import { biblioteca } from "$lib/stores/biblioteca.svelte";
import { MediaSession } from "@capgo/capacitor-media-session";
import { Capacitor } from "@capacitor/core";
import { cargarEstadoReproductor } from "$lib/services/stores";
import { truncate } from "fs";
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
      ? biblioteca.songs.findIndex(
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
  private handlersInitialized = false;
  private lastPositionSync = 0;

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
    //al ejecutarse este codigo, es posible que biblioteca aun no haya cargado la cancion por lo
    // que se espera un error
    const lastSong = biblioteca.songs.findIndex(
      (el) => el.id == lastState.trackId,
    );
    if (lastSong === -1) return;
    this.currentTime = lastState.position;
    this.currentSong = biblioteca.songs[lastSong];
    this.mode = lastState.mode ? lastState.mode : 'off';
  }
  public async setMetadata(song: Song) {
    if (!song) return;
    if (!this.handlersInitialized) {
      void this.initMediaSessionHandlers();
    }

    if (Capacitor.isNativePlatform()) {
      try {
        MediaSession.setMetadata({
          title: song.title || "Sin título",
          artist: Array.isArray(song.artists)
            ? song.artists.join(", ")
            : song.artists || "Artista desconocido",
          album: song.album || "Música",
          artwork: [
            {
              src: `${window.location.origin}/default-cover.png`,
              sizes: "512x512",
            },
          ],
        });
      } catch (e) {
        console.warn("Error metadata nativo:", e);
      }
    }
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
    this.currentSong = song;
    this.isPlaying = true;
    this.currentTime = 0;
    console.log(song.image);
    if (Capacitor.isNativePlatform() && !this.handlersInitialized) {
      this.handlersInitialized = true;
      this.initMediaSessionHandlers();
    }

    this.setMetadata(song).then(() => {
      this.syncNativePlaybackState(true);
      this.updatePositionState(0, this.duration, true);
    });
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
