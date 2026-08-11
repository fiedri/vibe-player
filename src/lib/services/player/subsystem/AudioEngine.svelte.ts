export class AudioEngine {
  currentTime = $state<number>(0);
  duration = $state<number>(0);
  volume = $state<number>(1);
  isPlaying = $state<boolean>(false);

  playTrigger = $state<number>(0);
  audioElement: HTMLAudioElement | null = null;

  private pendingLoadPosition: number | null = null;

  public incrementPlayRequest() {
    this.playTrigger++;
  }

  public restoreLoadPosition(position: number) {
    this.pendingLoadPosition = position;
    this.currentTime = position;
  }

  public bindElement(audioElement: HTMLAudioElement) {
    this.audioElement = audioElement;
    this.audioElement.ontimeupdate = () => {
      this.currentTime = this.audioElement?.currentTime ?? 0;
    };

    this.audioElement.onloadedmetadata = () => {
      const element = this.audioElement;
      if (!element) return;
      this.duration = element.duration ?? 0;

      if (this.pendingLoadPosition !== null) {
        element.currentTime = this.pendingLoadPosition;
        this.currentTime = this.pendingLoadPosition;
        this.pendingLoadPosition = null;
      }
    };

    this.audioElement.onplay = () => {
      this.isPlaying = true;
    };

    this.audioElement.onpause = () => {
      this.isPlaying = false;
    };
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
    this.audioElement?.pause();
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
