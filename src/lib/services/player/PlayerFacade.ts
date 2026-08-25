import type { AudioEngine } from "./subsystem/AudioEngine.svelte";
import { WebAudioEngine } from "./subsystem/AudioEngine.svelte";
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
import type { MediaFile } from "$lib/types/songs";
import { displayImage } from "$lib/types/songs";
import type { ContextType } from "./types";
export class PlayerFacade {
  protected audioEngine: AudioEngine;
  protected queueManager: QueueManager;
  protected artworkServices: ArtworkService = new ArtworkService();
  protected mediaSessionService: MediaSessionService =
    new MediaSessionService();
  constructor(audioEngine: AudioEngine) {
    this.audioEngine = audioEngine;
    this.audioEngine.onEndedRequest = () => this.handleTrackEnded();
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

  private async initSong(song: MediaFile) {
    const adyacentsSongImage = this.queueManager.getAdyacentsSongImage();
    this.artworkServices.prewarmArtworkAdyacente(
      adyacentsSongImage.previous,
      adyacentsSongImage.next,
      this.currentSong ? displayImage(this.currentSong) : undefined,
    );
    this.mediaSessionService.resetNativePosition(song, this.duration ?? 0);
    const img = await this.artworkServices.getArtworkSrc(displayImage(song));
    void this.mediaSessionService.setMetadata(song, img);
  }

  get currentSong() {
    return this.queueManager.currentSong;
  }
  set currentSong(song: MediaFile | null) {
    this.queueManager.currentSong = song;
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
  get mode() {
    return this.queueManager.mode;
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
        this.queueManager.transitionTo(new RepeatOffmode());
        break;
      case "one":
        this.queueManager.transitionTo(new RepeatOneMode());
        break;
      case "all":
        this.queueManager.transitionTo(new RepeatAllMode());
        break;
      default:
        break;
    }
  }
  public cycleRepeatMode(): string {
    const order = ["off", "one", "all"];
    const next = order[(order.indexOf(this.mode) + 1) % order.length]!;
    this.switchMode(next);
    return next;
  }
  public handleLoadedMetadata() {
    if (!this.currentSong) return;

    this.mediaSessionService.endNativePauseSuppression();
    this.mediaSessionService.updatePositionState(
      this.currentTime ?? 0,
      this.duration ?? 0,
      true,
    );
  }
  public syncPlaybackPosition() {
    if (!this.duration || !Number.isFinite(this.duration)) return;

    this.mediaSessionService.updatePositionState(
      this.currentTime,
      this.duration,
      false,     );
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
    console.log(lastState.mode);
    const mode = lastState.mode || "off";

    this.switchMode(mode);
    this.audioEngine.setUrl(restoredSong.uri);
    this.queueManager.fillqueue();
    const img = await this.artworkServices.getArtworkSrc(
      displayImage(restoredSong),
    );

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

  private startPlayback() {
    this.isPlaying = true;
    this.play();
    this.mediaSessionService.syncNativePlaybackState(
      this.isPlaying,
      0,
      this.duration,
    );
  }

  public setSong(song: MediaFile) {
    this.queueManager.setCurrentSong(song);
    this.audioEngine.setUrl(this.queueManager.currentSong?.uri);
    this.initSong(song);
    this.startPlayback();
  }
  public play() {
    this.audioEngine.play();
    this.isPlaying = true;
    this.endNativePauseSuppression();
    this.mediaSessionService.syncNativePlaybackState(
      true,
      this.currentTime,
      this.duration,
    );
  }
  public pause() {
    this.audioEngine.pause();

    if (this.isSuppressingNativePause) {
      return;
    }
    this.isPlaying = false;
    this.mediaSessionService.syncNativePlaybackState(
      false,
      this.currentTime,
      this.duration,
    );
  }

  public previous() {
    let song;
    if (this.audioEngine.currentTime >= 3) {
      this.seekTo(0);
      return;
    }
    this.queueManager.previous();
    song = this.queueManager.currentSong;
    if (song) {
      this.setSong(song);
    }
  }
  public setNextSong(song: MediaFile) {
    this.queueManager.setNextSong(song);
  }
  public next() {
    this.queueManager.next();
    const song = this.queueManager.currentSong;
    this.syncPlaybackPosition();
    if (song) {
      this.setSong(song);
    }
  }
  public togglePlay() {
    if (this.audioEngine.isPlaying) {
      this.pause();
    } else {
      this.play();
    }
  }
  public toggleShuffle() {
    this.queueManager.toggleShuffle();
  }
  public shuffle(songs: MediaFile[]): MediaFile[] {
    return this.queueManager.shuffle(songs);
  }
  public handleTrackEnded() {
    const cancionAnterior = this.currentSong;
    const cancion = this.queueManager.handleTrackEndednext();

    if (cancion) {
      if (cancion !== cancionAnterior) {
        this.setSong(cancion);
      }
      this.startPlayback();
    } else {
      this.pause();
    }
  }
  public seekTo(time: number) {
    const validDuration =
      this.duration && Number.isFinite(this.duration) ? this.duration : 0;

    this.audioEngine.seek(time);

    if (validDuration > 0) {
      this.mediaSessionService.updatePositionState(time, validDuration, true);
    }
  }
  public setContext(context: ContextType, songs: MediaFile[]) {
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

export const playerService = new PlayerFacade(new WebAudioEngine());

