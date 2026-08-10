import { biblioteca } from "$lib/stores/biblioteca.svelte";
import type { Song } from "$lib/types/songs";
import { ModeState } from "../states/modeState";
export enum ContextType {
  InPlaylist = "playlists",
  InBiblioteca = "biblioteca",
}

export class QueueManager {
  queue = $state([...biblioteca.songs]);
  private rawSource = $derived([...biblioteca.songs]);
  private state!: ModeState;
  isShuffle = $state<boolean>(false);
  context = $state<ContextType | null>(null);
  playlistSongs = $state<Song[]>([]);
  currentSong = $state<Song | null>(null);
  currentSongIndex = $derived<number | null>(
    Array.isArray(this.queue) && this.currentSong
      ? this.queue.findIndex(
          (el) =>
            el.id === this.currentSong?.id

        )
      : null,
  );
  constructor(state: ModeState){
    this.transitionTo(state)
  }
  public transitionTo(state: ModeState): void {
        console.log(`Context: Transition to ${(<any>state).constructor.name}.`);
        this.state = state;
        this.state.setContext(this);
    }
  public setContext(context: ContextType, songs?: Song[]) {
    if (context === ContextType.InPlaylist && songs) {
      this.playlistSongs = [...songs];
      this.queue = [...songs];
    } else {
      this.playlistSongs = [];
      this.queue = [...this.rawSource];
    }
    this.context = context;
    if (this.isShuffle) this.aplicarShuffle();
  }
  public setCurrentSong(song: Song) {
    this.currentSong = song;
  }

  private aplicarShuffle() {
    const source =
      this.context === ContextType.InPlaylist
        ? this.playlistSongs
        : this.rawSource;
    const currentSong = this.currentSong;
    this.queue = this.shuffle(source);
    if (currentSong) {
      this.queue = this.queue.filter((song) => song.id !== currentSong.id);
      this.queue.unshift(currentSong);
    }
  }

  public shuffle(array: Song[]): Song[] {
    const shuffled = [...array];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }

    return shuffled;
  }
  public toggleShuffle() {
    this.isShuffle = !this.isShuffle;
    if (this.isShuffle) {
      this.aplicarShuffle();
    } else {
      this.queue =
        this.context === ContextType.InPlaylist
          ? [...this.playlistSongs]
          : [...this.rawSource];
    }
  }
  public getAdyacentsSongImage() {
    let previous = null;
    let next = null;

    if (this.currentSongIndex !== null && this.currentSongIndex !== -1) {
      previous = this.queue[this.currentSongIndex - 1]?.image ?? null;
      next = this.queue[this.currentSongIndex + 1]?.image ?? null;
    }

    return { previous, next };
  }

  public previous() {
   this.state.previous()

  }
  public next() {
   this.state.next()

  }
  public handleTrackEndednext(): Song | null {
    return this.state.handleTrackEndednext();
  }
  public fillqueue(){
    this.queue = this.rawSource
  }
}
