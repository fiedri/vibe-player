import type { Song } from "$lib/types/songs";
class PlayerStore {
  currentSong = $state<Song | null>(null);
  isPlaying = $state<boolean>(false);
  volume = $state<number>(0.8);
  currentTime = $state<number>(0);
  duration = $state<number>(0);

  setSong(song: Song) {
    this.pause();
    this.currentSong = song;
    this.isPlaying = true;   
this.currentTime = 0
  }

  togglePlay() {
    if (!this.currentSong) return;
    this.isPlaying = !this.isPlaying;
  }

  play() {
    if (this.currentSong) this.isPlaying = true;
  }

  pause() {
    this.isPlaying = false;
  }
}

export const player = new PlayerStore();
