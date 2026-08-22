import { Capacitor } from "@capacitor/core";
export abstract class AudioEngine {
  public currentTime = $state<number>(0);
  public duration = $state<number>(0);
  public volume = $state<number>(1);
  public isPlaying = $state<boolean>(false);
  public onEndedRequest?: () => void;
  abstract setUrl(songUrl: string): void;
  abstract restoreLoadPosition(position: number): void;
  abstract setVolume(val: number): void;
  abstract seek(time: number): void;
  abstract play(): void;
  abstract pause(): void;
}
export class WebAudioEngine extends AudioEngine {
  private audioElement: HTMLAudioElement = new Audio();
  constructor() {
    super();
    const updateDuration = () => {
      if (!isNaN(this.audioElement.duration)) {
        this.duration = this.audioElement.duration;
      }
    };

    this.audioElement.addEventListener("loadedmetadata", updateDuration);
    this.audioElement.addEventListener("durationchange", updateDuration);
    this.audioElement.ontimeupdate = () => {
      this.currentTime = this.audioElement?.currentTime ?? 0;
    };
    this.audioElement.onloadedmetadata = () => {
      this.duration = this.audioElement.duration ?? 0;
      this.audioElement.currentTime = this.currentTime;
    };
    this.audioElement.onended = () => {
      this.onEndedRequest?.();
    };
  }

  public restoreLoadPosition(position: number) {
    this.currentTime = position;
  }
  public setUrl(songUrl: string) {
    this.audioElement.src = Capacitor.convertFileSrc(songUrl);
  }
  public play() {
    if (!this.audioElement) return;

    const playPromise = this.audioElement.play();

    if (playPromise !== undefined) {
      playPromise
        .then(() => {
          this.isPlaying = true;
        })
        .catch((error) => {
          if (error.name !== "AbortError") {
            console.error("Error al reproducir audio:", error);
            this.isPlaying = false;
          }
        });
    } else {
      this.isPlaying = true;
    }
  }

  public pause() {
    this.audioElement.pause();
    this.isPlaying = false;
  }

  public seek(time: number) {
    if (this.audioElement) {
      this.audioElement.currentTime = time;
      this.currentTime = time;
    }
  }

  public setVolume(val: number) {
    this.volume = val;
    if (this.audioElement) {
      this.audioElement.volume = val;
    }
  }
}
