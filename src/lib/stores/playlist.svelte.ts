import * as db from "$lib/db/db/querys";
import { playlists } from "$lib/db/db/schema";

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
  public async add(playlistsName: string) {
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
}

export const playlistStore = new playlist();
