import { SvelteSet } from "svelte/reactivity";
import * as db from "$lib/db/db/querys";
import { playlistStore } from "./playlist.svelte";
class FavoritesStore {
  //actualizar cuando se agregue
  favoritesId: number = 0;
  songsIds = new SvelteSet<string>();
  counts = $derived(this.songsIds.size);
  async loadFavoritesInfo() {
    const info = await db.getFavoritesInfo();
    if(!info) return
    this.favoritesId = info.id;
    //console.log(this.favorites)
    info?.playlistsSongs.forEach((el) => {
      this.songsIds.add(el.songId);
    });
  }
  async addSong(songId: string) {
    try {
      this.songsIds.add(songId);
      await playlistStore.addSong(this.favoritesId, songId);
    } catch (e) {
      console.error(e);
    }
  }
  async removeSong(songId:string){
    try{
      this.songsIds.delete(songId)
      await playlistStore.removeSong(this.favoritesId, songId)
    }catch(error){
console.error(error)
    }
  }
  toggleFavorite(songId: string) {
    if (this.songsIds.has(songId)) {
      this.removeSong(songId)
    } else {
      this.addSong(songId);
    }
  }
}

export const favorites = new FavoritesStore();
