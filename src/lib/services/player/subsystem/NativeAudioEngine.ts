import { registerPlugin } from "@capacitor/core";
import type { MediaFile } from "@odion-cloud/capacitor-mediastore";
import { AudioEngine } from "./AudioEngine.svelte";

export interface NativeAudioEnginePlugin {
  setSong(options: { uri: string }): Promise<void>;
  restoreLoadPosition(options: { position: number }): Promise<void>;
  play(): Promise<void>;
  pause(): Promise<void>;
  seek(options: { time: number }): Promise<void>;
  setVolume(options: { volume: number }): Promise<void>;
  addListener(
    eventName: "loadedMetadata",
    listenerFunc: (data: { duration: number; currentTime: number }) => void,
  ): Promise<{ remove: () => Promise<void> }>;
  addListener(
    eventName: "isPlayingChange",
    listenerFunc: (data: { isPlaying: boolean; currentTime: number }) => void,
  ): Promise<{ remove: () => Promise<void> }>;
  addListener(
    eventName: "seeked",
    listenerFunc: (data: { currentTime: number }) => void,
  ): Promise<{ remove: () => Promise<void> }>;
  addListener(
    eventName: "ended",
    listenerFunc: () => void,
  ): Promise<{ remove: () => Promise<void> }>;
  addListener(
    eventName: "error",
    listenerFunc: (data: { message: string }) => void,
  ): Promise<{ remove: () => Promise<void> }>;
}

const nativeAudioEnginePlugin = registerPlugin<NativeAudioEnginePlugin>(
  "NativeAudioEngine",
);

/**
 * ExoPlayer-backed AudioEngine. Playback happens natively; this class only
 * mirrors state back into the reactive fields the rest of the app already
 * reads from AudioEngine (PlayerFacade, MediaSessionService, UI).
 */
export class NativeAudioEngine extends AudioEngine {
  private plugin: NativeAudioEnginePlugin = nativeAudioEnginePlugin;

  constructor() {
    super();

    void this.plugin.addListener("loadedMetadata", ({ duration, currentTime }) => {
      this.duration = duration;
      this.currentTime = currentTime;
      this.onLoadedMetadata?.();
    });
    void this.plugin.addListener("isPlayingChange", ({ isPlaying, currentTime }) => {
      //this.isPlaying = isPlaying;
      this.currentTime = currentTime;
    });
    void this.plugin.addListener("seeked", ({ currentTime }) => {
      this.currentTime = currentTime;
    });
    void this.plugin.addListener("ended", () => {

      this.currentTime = 0;
      this.isPlaying = false;
      this.onEndedRequest?.();
    });
    void this.plugin.addListener("error", ({ message }) => {
      console.error("NativeAudioEngine playback error:", message);
      this.anchorPosition = this.currentTime;
      this.anchorTimestamp = performance.now();
      this.isPlaying = false;
    });
  }

  public restoreLoadPosition(position: number) {
    this.anchorPosition = position;
    this.anchorTimestamp = performance.now();
    void this.plugin.restoreLoadPosition({ position });
  }

  public setSong(song: MediaFile) {
    this.anchorPosition = 0;
    this.anchorTimestamp = performance.now();
    this.duration = 0;
    void this.plugin.setSong({ uri: song.uri }).catch((error: unknown) => {
      console.error("Error al setear la canción:", error);
      this.isPlaying = false
    });
  }

  public play() {
    this.anchorTimestamp = performance.now();
    this.isPlaying = true
    this.plugin
      .play()
      .catch((error: unknown) => {
        console.error("Error al reproducir audio:", error);
        this.anchorPosition = this.currentTime;
        this.anchorTimestamp = performance.now();
        this.isPlaying = false;
      });
  }

  public pause() {
    this.isPlaying = false
    this.anchorPosition = this.currentTime;
    this.anchorTimestamp = performance.now();
    void this.plugin.pause();
  }

  public seek(time: number) {
    this.currentTime = time;
    void this.plugin.seek({ time });
  }

  public setVolume(val: number) {
    this.volume = val;
    void this.plugin.setVolume({ volume: val });
  }
}
