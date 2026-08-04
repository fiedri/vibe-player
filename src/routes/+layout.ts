// src/routes/+layout.ts
export const ssr = false;
export const prerender = false;

import { obtenerCache } from "$lib/services/stores";
import { biblioteca } from "$lib/stores/biblioteca.svelte";
import type { LayoutLoad } from "./$types";
import { getDb } from "$lib/db/db";
import { playlistStore } from "$lib/stores/playlist.svelte";
export const load: LayoutLoad = async () => {
  
getDb()
  if(playlistStore.playlists.length === 0){
    playlistStore.loadPlaylist()
  }
  const cache = await obtenerCache();
  if (cache.length > 0) biblioteca.songs = cache;
  return {};
};

//recomendacion propia: usar archivos +layout.ts o +page.ts, para la carga de datos antes de renderizar
