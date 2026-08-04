import * as db from "$lib/db/db/querys";
import { biblioteca } from "./biblioteca.svelte";
interface Playlists {
  name: string;
  id: number;
  songsCount: number;
}

class playlist {
  isLoading = $state<boolean>(false);
  isLoaded = $state<boolean>(false);
  playlists = $state<Playlists[]>([]);
  error = $state<string | null>(null);
 
  public async loadPlaylist() {
    try {
      console.log("cargando playlist");
      this.isLoading = true;
      const result = await db.getPlaylists();
      this.playlists = result;

      this.isLoading = false;
      if (this.playlists.length === 0) {
        this.error = "No hay playlist Disponibles";
      }
    } catch (e) {
      this.error = e;
    }
  }
  public async add(playlistsName: string, firstSong?: string) {
    try {
      const resultId = await db.createPlaylist(playlistsName.trim());
      this.playlists.unshift({
        name: playlistsName,
        id: resultId.id,
        songsCount: 0,
      });
    } catch (e) {
      console.error(e);
    }
  }
  public async delete(playlistsId: number) {
    try {
      this.playlists = this.playlists.filter((el) => el.id !== playlistsId);
      await db.deletePlaylist(playlistsId);
    } catch (e) {
      console.error(e);
    }
  }
  public async addSong(playlistId: number, songId: string) {
    try {
      const playlistIdx = this.playlists.findIndex((e) => e.id == playlistId);
      if (playlistIdx === -1) return;
      this.playlists[playlistIdx].songsCount++;
      await db.addSongToPlaylist(playlistId, songId);
    } catch (e) {
      console.error(e);
    }
  }
  public getArraySong(playlistsSongArr: { songId: string }[] = []) {
    const idsSongs = new Set(playlistsSongArr.map((item) => item.songId));
    const result = biblioteca.songs.filter((el) => idsSongs.has(el.id));
    return result;
  }
  public async removeSong(playlistId: number, songId: string) {
    try {
      const playlistIdx = this.playlists.findIndex((e) => e.id == playlistId);
      if (playlistIdx === -1) return;
      this.playlists[playlistIdx].songsCount--;

      await db.removeSongFromPlaylist(playlistId, songId);
    } catch (e) {
      console.error(e);
    }
  }
}

export const playlistStore = new playlist();
