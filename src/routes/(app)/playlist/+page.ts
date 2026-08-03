import { playlistStore } from "$lib/stores/playlist.svelte";
import type { PageLoad } from "./$types";
export const load: PageLoad = ()=>{
  if(playlistStore.playlists.length === 0){
    playlistStore.loadPlaylist()
  }
 return {}
}


