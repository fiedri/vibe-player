import { Capacitor } from "@capacitor/core";
import type { MediaFile } from "@odion-cloud/capacitor-mediastore";
export abstract class AudioEngine {
  public duration = $state<number>(0);
  public volume = $state<number>(1);
  public isPlaying = $state<boolean>(false);
  public onEndedRequest?: () => void;
  public onLoadedMetadata?: () => void;
  public onSeeked?: () => void;

  public anchorPosition = $state<number>(0);
  public anchorTimestamp: number = typeof performance !== "undefined" ? performance.now() : 0;

  public get currentTime(): number {
    if (this.isPlaying) {
      const now = typeof performance !== "undefined" ? performance.now() : 0;
      const elapsed = (now - this.anchorTimestamp) / 1000;
      return Math.min(
        this.duration || Infinity,
        Math.max(0, this.anchorPosition + elapsed),
      );
    }
    return this.anchorPosition;
  }

  public set currentTime(time: number) {
    this.anchorPosition = time;
    this.anchorTimestamp = typeof performance !== "undefined" ? performance.now() : 0;
  }

  abstract setSong(song: MediaFile): void;
  abstract restoreLoadPosition(position: number): void;
  abstract setVolume(val: number): void;
  abstract seek(time: number): void;
  abstract play(): void;
  abstract pause(): void;
}
export class WebAudioEngine extends AudioEngine {
  private audioElement: HTMLAudioElement = new Audio();
  private pendingPosition: number | null = null;
  constructor() {
    super();
    const updateDuration = () => {
      if (!isNaN(this.audioElement.duration)) {
        this.duration = this.audioElement.duration;
      }
    };

    this.audioElement.addEventListener("durationchange", updateDuration);
    this.audioElement.ontimeupdate = () => {
      this.anchorPosition = this.audioElement?.currentTime ?? 0;
      this.anchorTimestamp = performance.now();
    };
    this.audioElement.onloadedmetadata = () => {
      this.duration = this.audioElement.duration ?? 0;
      if (this.pendingPosition !== null) {
        this.audioElement.currentTime = this.pendingPosition;
        this.anchorPosition = this.pendingPosition;
        this.pendingPosition = null;
      } else {
        this.anchorPosition = 0;
      }
      this.anchorTimestamp = performance.now();
      this.onLoadedMetadata?.();
    };
    this.audioElement.onseeked = () => {
      this.anchorPosition = this.audioElement.currentTime;
      this.anchorTimestamp = performance.now();
      this.onSeeked?.();
    };
    this.audioElement.onended = () => {
      this.isPlaying = false;
      this.anchorPosition = 0;
      this.anchorTimestamp = performance.now();
      this.onEndedRequest?.();
    };
  }

  public restoreLoadPosition(position: number) {
    this.pendingPosition = position;
    this.anchorPosition = position;
    this.anchorTimestamp = performance.now();
  }
  public setSong(song: MediaFile) {
    this.anchorPosition = 0;
    this.anchorTimestamp = performance.now();
    this.duration = 0;
    this.isPlaying = false;
    this.audioElement.src = Capacitor.convertFileSrc(song.uri);
  }
  public play() {
    if (!this.audioElement) return;

    this.anchorTimestamp = performance.now();
    const playPromise = this.audioElement.play();

    if (playPromise !== undefined) {
      playPromise
        .then(() => {
          this.isPlaying = true;
          this.anchorTimestamp = performance.now();
        })
        .catch((error) => {
          if (error.name !== "AbortError") {
            console.error("Error al reproducir audio:", error);
            this.isPlaying = false;
          }
        });
    } else {
      this.isPlaying = true;
      this.anchorTimestamp = performance.now();
    }
  }

  public pause() {
    this.anchorPosition = this.audioElement.currentTime;
    this.anchorTimestamp = performance.now();
    this.audioElement.pause();
    this.isPlaying = false;
  }

  public seek(time: number) {
    if (this.audioElement) {
      this.audioElement.currentTime = time;
      this.anchorPosition = time;
      this.anchorTimestamp = performance.now();
    }
  }

  public setVolume(val: number) {
    this.volume = val;
    if (this.audioElement) {
      this.audioElement.volume = val;
    }
  }
}
