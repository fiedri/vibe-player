import { biblioteca } from "$lib/stores/biblioteca.svelte";
import type { MediaFile } from "$lib/types/songs";
import { displayImage } from "$lib/types/songs";
import { ModeState } from "../states/modeState";
export enum ContextType {
  InPlaylist = "playlists",
  InBiblioteca = "biblioteca",
}

export class QueueManager {
  queue = $state([...biblioteca.songs]);
  private rawSource = $derived([...biblioteca.songs]);
  private state!: ModeState;
  mode = $state<string>("off");
  isShuffle = $state<boolean>(false);
  context = $state<ContextType | null>(null);
  playlistSongs = $state<MediaFile[]>([]);
  currentSong = $state<MediaFile | null>(null);
  //findLastIndex no es una buena solucion, ya que si la cancion esta despues de la que se esta reproduciendo actualmente
  //al agregarla a la siguiente, siempre buscara la ultima, no la que acabamos de agregar que aparecera amtes
  currentSongIndex = $state<number | null>(null);
  constructor(state: ModeState) {
    this.transitionTo(state);
  }
  public transitionTo(state: ModeState): void {
    console.log(`Context: Transition to ${(<any>state).constructor.name}.`);
    this.state = state;
    this.state.setContext(this);
    this.mode = state.modeName;
  }
  public setContext(context: ContextType, songs?: MediaFile[]) {
    if (context === ContextType.InPlaylist && songs) {
      this.playlistSongs = [...songs];
      this.queue = [...songs];
    } else {
      this.playlistSongs = [];
      this.queue = [...this.rawSource];
    }
    this.context = context;
    if (this.isShuffle) this.aplicarShuffle();
    this.calculateIndex();
  }
  public setCurrentSong(song: MediaFile) {
    this.currentSong = song;
    this.calculateIndex(song);
  }
  private calculateIndex(song?: MediaFile) {
    const index = this.queue.findIndex((item) => {
      return this.currentSong?.id == item.id || song?.id == item.id;
    });

    if (index !== -1) {
      this.currentSongIndex = index;
    }
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
      this.currentSongIndex = 0;
    }
  }

  public shuffle(array: MediaFile[]): MediaFile[] {
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
    this.calculateIndex();
  }
  public getAdyacentsSongImage() {
    let previous = null;
    let next = null;

    if (this.currentSongIndex !== null && this.currentSongIndex !== -1) {
      const prevSong = this.queue[this.currentSongIndex - 1];
      const nextSong = this.queue[this.currentSongIndex + 1];
      previous = prevSong ? displayImage(prevSong) : null;
      next = nextSong ? displayImage(nextSong) : null;
    }

    return { previous, next };
  }

  public previous() {
    this.state.previous();
  }
  public next() {
    this.state.next();
  }
  public handleTrackEndednext(): MediaFile | null {
    return this.state.handleTrackEndednext();
  }
  public fillqueue() {
    this.queue = this.rawSource;
    this.calculateIndex();
  }
  public setNextSong(song: MediaFile) {
    if (this.currentSongIndex !== null) {
      this.queue.splice(this.currentSongIndex + 1, 0, song);
    }
  }
  public moveInQueue(from: number, to: number): void {
    if (
      from < 0 ||
      from > this.queue.length - 1 ||
      to > this.queue.length - 1 ||
      to < 0
    ) {
      throw new Error("Parametros fuera del rango permitido");
    }
    if (from === to || this.currentSongIndex == null) return;
    if (from === this.currentSongIndex) {
      this.currentSongIndex = to;
    }
    else if (from < this.currentSongIndex && to >= this.currentSongIndex) {
      this.currentSongIndex--;
    } else if(from > this.currentSongIndex && this.currentSongIndex >= to) {
      this.currentSongIndex++;
    }

    const [item] = this.queue.splice(from, 1);
    this.queue.splice(to, 0, item);
  }
}
