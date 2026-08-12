import { AudioEngine } from "./subsystem/AudioEngine.svelte";
import { QueueManager } from "./subsystem/queue.svelte";
import { MediaSessionService } from "./subsystem/mediaSessionService";
import { ArtworkService } from "./subsystem/arworkServices";
import { cargarEstadoReproductor } from "../stores";
import {
  RepeatAllMode,
  RepeatOffmode,
  RepeatOneMode,
} from "./states/modeState";
import { biblioteca } from "$lib/stores/biblioteca.svelte";
import { Capacitor } from "@capacitor/core";
import type { Song } from "$lib/types/songs";
import type { ContextType } from "./types";
export class PlayerFacade {
  protected audioEngine: AudioEngine = new AudioEngine();
  /** Modo de repetición (off|one|all). Estado de configuración, no reactivo
   *  en UI: se persiste y restaura, pero la UI de repeat hoy está comentada. */
  mode: string = "off";
  protected queueManager: QueueManager;
  protected artworkServices: ArtworkService = new ArtworkService();
  protected mediaSessionService: MediaSessionService =
    new MediaSessionService();
  constructor() {
    this.queueManager = new QueueManager(new RepeatOffmode());

    this.mediaSessionService.onPauseRequest = () => this.pause();
    this.mediaSessionService.onPlayRequest = () => this.play();
    this.mediaSessionService.onNextTrackRequest = () => this.next();
    this.mediaSessionService.onPreviousTrackRequest = () => this.previous();
    this.mediaSessionService.onSeekRequest = (time) => this.seekTo(time);
    this.mediaSessionService.onStopTrackRequest = () => this.pause();
    const defaultPath = "/default-cover.png";

    this.artworkServices
      .getArtworkSrc(defaultPath)
      .then((defaultImg) => {
        this.mediaSessionService.init(defaultImg);
      })
      .catch(() => {
        this.mediaSessionService.init(defaultPath);
      });
  }

  private async initSong(song: Song) {
    const adyacentsSongImage = this.queueManager.getAdyacentsSongImage();
    this.artworkServices.prewarmArtworkAdyacente(
      adyacentsSongImage.previous,
      adyacentsSongImage.next,
      this.currentSong?.image,
    );
    // Reset determinista del ancla nativa en CADA cambio de track. No
    // empujamos PLAYING acá todavía: el src nuevo aún no arrancó, y PLAYING
    // con la posición stale previa es exactamente lo que extrapola la barra
    // en el nativo mientras cargamos. PAUSED (posición ~0) congela el bar.
    this.mediaSessionService.resetNativePosition(
      song,
      this.audioEngine.audioElement?.duration ?? 0,
    );
    const img = await this.artworkServices.getArtworkSrc(song.image);
    void this.mediaSessionService.setMetadata(song, img);
  }

  get currentSong() {
    return this.queueManager.currentSong;
  }
  set currentSong(song: Song | null) {
    this.queueManager.currentSong = song;
  }
  get playTrigger() {
    return this.audioEngine.playTrigger;
  }
  get queue() {
    return this.queueManager.queue;
  }
  get currentSongIndex() {
    return this.queueManager.currentSongIndex;
  }
  get isPlaying() {
    return this.audioEngine.isPlaying;
  }
  set isPlaying(isPlaying: boolean) {
    this.audioEngine.isPlaying = isPlaying;
  }
  get duration() {
    return this.audioEngine.duration;
  }
  get currentTime() {
    return this.audioEngine.currentTime;
  }
  get isShuffle() {
    return this.queueManager.isShuffle;
  }
  get isSuppressingNativePause() {
    return this.mediaSessionService.isSuppressingNativePause;
  }
  get volume() {
    return this.audioEngine.volume;
  }
  get currentIndex() {
    return this.queueManager.currentSongIndex;
  }
  get numberOfSongs() {
    return this.queueManager.queue.length;
  }
  set volume(volume: number) {
    this.audioEngine.setVolume(volume);
  }
  public switchMode(mode: string) {
    switch (mode) {
      case "off":
        this.mode = "off";
        this.queueManager.transitionTo(new RepeatOffmode());
        break;
      case "one":
        this.mode = "one";
        this.queueManager.transitionTo(new RepeatOneMode());
        break;
      case "all":
        this.mode = "all";
        this.queueManager.transitionTo(new RepeatAllMode());
        break;
      default:
        break;
    }
  }
  public handleLoadedMetadata() {
    if (!this.currentSong) return;

    this.mediaSessionService.endNativePauseSuppression();
    const el = this.audioEngine.audioElement;
    this.mediaSessionService.updatePositionState(
      el?.currentTime ?? 0,
      el?.duration ?? 0,
      true,
    );
  }
  public syncPlaybackPosition() {
    const el = this.audioEngine.audioElement;
    if (!el) return;
    this.mediaSessionService.updatePositionState(
      el.currentTime,
      el.duration,
      // sin force — el throttle de 2s del service filtra
    );
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
    this.mediaSessionService.beginNativePauseSuppression();
    this.audioEngine.restoreLoadPosition(lastState.position);
    this.queueManager.setCurrentSong(restoredSong);
    const mode = lastState.mode || "off";
    this.switchMode(mode);
    this.queueManager.fillqueue();
    const img = await this.artworkServices.getArtworkSrc(restoredSong.image);

    if (Capacitor.isNativePlatform()) {
      this.mediaSessionService.syncNativePlaybackState(false);
      void this.mediaSessionService.setMetadata(restoredSong, img);

      const duration = restoredSong.duration as number | string | undefined;

      let restoredDuration = 0;
      if (typeof duration === "number" && Number.isFinite(duration)) {
        restoredDuration = duration / 1000;
      } else if (typeof duration === "string" && duration.includes(":")) {
        const parts = duration.split(":").map(Number);
        restoredDuration = parts.reduce((acc, part) => acc * 60 + part, 0);
      }
      if (restoredDuration > 0) {
        this.updatePositionState(lastState.position, restoredDuration, true);
      }
    }
  }

  /** Incrementa el trigger que dispara la reproducción real en el <audio>
   *  (ver $effect en player.svelte) y además manda play() al AudioEngine por si
   *  el elemento ya está bindeado (siguiente/anterior con el miniplayer vivo). */
  private startPlayback() {
    this.audioEngine.incrementPlayRequest();
    this.audioEngine.play();
  }

  public setSong(song: Song) {
    this.queueManager.setCurrentSong(song);
    this.initSong(song);
    this.startPlayback();
  }
  public play() {
    this.audioEngine.play();
  }
  public pause() {
    this.audioEngine.pause();
  }

  public attachElement(audioElement: HTMLAudioElement) {
    this.audioEngine.bindElement(audioElement);
  }

  public previous() {
    this.queueManager.previous();
    const song = this.queueManager.currentSong;
    if (song) {
      this.initSong(song);
      this.startPlayback();
    }
  }
  public setNextSong(song: Song) {
    this.queueManager.setNextSong(song);
  }
  public next() {
    this.queueManager.next();
    const song = this.queueManager.currentSong;
    if (song) {
      this.initSong(song);
      this.startPlayback();
    }
  }
  public togglePlay() {
    if (this.audioEngine.isPlaying) {
      this.audioEngine.pause();
    } else {
      this.audioEngine.play();
    }
  }
  public toggleShuffle() {
    this.queueManager.toggleShuffle();
  }
  public shuffle(songs: Song[]): Song[] {
    return this.queueManager.shuffle(songs);
  }
  public handleTrackEnded() {
    const cancionAnterior = this.currentSong;
    const cancion = this.queueManager.handleTrackEndednext();

    if (cancion) {
      if (cancion !== cancionAnterior) {
        this.initSong(cancion);
      }
      this.startPlayback();
    } else {
      this.pause();
    }
  }
  public seekTo(time: number) {
    this.audioEngine.seek(time);
    this.mediaSessionService.updatePositionState(
      time,
      this.audioEngine.duration,
      this.audioEngine.isPlaying,
    );
  }
  public setContext(context: ContextType, songs: Song[]) {
    this.queueManager.setContext(context, songs);
  }
  // MediaSessionService
  public endNativePauseSuppression() {
    this.mediaSessionService.endNativePauseSuppression();
  }
  public syncNativePlaybackState(isPlaying = this.isPlaying) {
    this.mediaSessionService.syncNativePlaybackState(isPlaying);
  }
  public updatePositionState(
    position: number,
    duration: number,
    force: boolean,
  ) {
    this.mediaSessionService.updatePositionState(position, duration, force);
  }
}

export const playerService = new PlayerFacade();
