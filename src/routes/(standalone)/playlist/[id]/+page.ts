
import type { PageLoad } from "./$types";
import { getPlaylistSongs } from "$lib/db/db/querys";
import { playlistStore } from "$lib/stores/playlist.svelte";
export const load: PageLoad = async({params})=>{
 const playlistId = params.id;
 const result = await getPlaylistSongs(Number(playlistId));
 const songs = playlistStore.getArraySong(result?.playlistsSongs)
 return{
   id: result?.id,
   name: result?.name,
   songs: songs ?? [],
 }
}
