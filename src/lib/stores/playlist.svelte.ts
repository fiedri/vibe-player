import * as db from "$lib/db/db/querys";
import { biblioteca } from "./biblioteca.svelte";
import type { MediaFile } from "$lib/types/songs";
import { DialogType, ui } from "./ui.svelte";
interface Playlists {
  name: string;
  id: number;
  songsCount: number;
}

class playlist {
  isLoading = $state<boolean>(false);
  isLoaded = $state<boolean>(false);
  playlists = $state<Playlists[]>([]);
  error = $state<string | null | unknown>(null);

  currentPlaylistId = $state<number | null>(null);
  currentPlaylistSongs = $state<MediaFile[]>([]);

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
      if (!resultId) throw Error("Error al crear la playlist");
      this.playlists.unshift({
        name: playlistsName,
        id: resultId.id,
        songsCount: 0,
      });
    } catch (e) {
      console.error(e);
      ui.openDialog(DialogType.Error, e);
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
    const songsMap = new Map(biblioteca.songs.map((song) => [song.id, song]));

    return playlistsSongArr
      .map((pSong) => songsMap.get(pSong.songId))
      .filter(
        (song): song is (typeof biblioteca.songs)[number] => song !== undefined,
      );
  }
  public async removeSong(playlistId: number, songId: string) {
    try {
      const playlistIdx = this.playlists.findIndex((e) => e.id == playlistId);
      if (playlistIdx === -1) return;
      this.playlists[playlistIdx].songsCount--;
      this.currentPlaylistSongs = this.currentPlaylistSongs.filter(
        (s) => s.id !== songId,
      );

      await db.removeSongFromPlaylist(playlistId, songId);
    } catch (e) {
      console.error(e);
    }
  }
  public async loadPlaylistSongs(playlistId: number) {
    const result = await db.getPlaylistSongs(playlistId);
    this.currentPlaylistId = playlistId;
    this.currentPlaylistSongs = this.getArraySong(result?.playlistsSongs);
  }
  public async removeManySongs(playlistId: number, songIds: string[]) {
    try {
      const playlistIdx = this.playlists.findIndex((e) => e.id == playlistId);
      const removed = new Set(songIds);
      this.currentPlaylistSongs = this.currentPlaylistSongs.filter(
        (s) => !removed.has(s.id),
      );
      if (playlistIdx !== -1) {
        this.playlists[playlistIdx].songsCount = Math.max(
          0,
          this.playlists[playlistIdx].songsCount - songIds.length,
        );
      }
      await db.removeManySongsFromPlaylist(playlistId, songIds);
    } catch (e) {
      console.error(e);
    }
  }
  public async addManySongs(playlistId: number, songsIds: string[]) {
    try {
      const playlistIdx = this.playlists.findIndex((e) => e.id == playlistId);
      if (playlistIdx === -1) return;
      this.playlists[playlistIdx].songsCount =
        this.playlists[playlistIdx].songsCount + songsIds.length;

      await db.addManySongsToPlaylists(playlistId, songsIds);
    } catch (error) {
      console.warn("error al agregar muchas canciones", error);
    }
  }
  public async deleteDuplicates(playlistId: number) {
    try {
      if (playlistId !== this.currentPlaylistId){
        throw Error("Ocurrio un error al eliminar duplicados");
      }
      await db.deleteDuplicates(playlistId);
      const uniquesSongs = new Set(this.currentPlaylistSongs);
      this.currentPlaylistSongs = [...uniquesSongs];
      const playlistIdx = this.playlists.findIndex((el) => el.id == playlistId);
      this.playlists[playlistIdx].songsCount = uniquesSongs.size;
    } catch (e) {
      console.error(e);
    }
  }
}

export const playlistStore = new playlist();
