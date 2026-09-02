// src/routes/+layout.ts
export const ssr = false;
export const prerender = false;

import { Device } from "@capacitor/device";
import { setLocale, type Locale } from "$lib/paraglide/runtime";
import { obtenerCache, cargarIdiomaPreferido } from "$lib/services/stores";
import { biblioteca } from "$lib/stores/biblioteca.svelte";
import type { LayoutLoad } from "./$types";
import { getDb } from "$lib/db/db";
import { playlistStore } from "$lib/stores/playlist.svelte";
export const load: LayoutLoad = async () => {
  await getDb();
  if (playlistStore.playlists.length === 0) {
    await playlistStore.loadPlaylist();
  }
  const cache = await obtenerCache();
  if (cache.length > 0) biblioteca.songs = cache;
  let userLocale: Locale = "en";
  const idiomaPreferido = await cargarIdiomaPreferido();

  if (idiomaPreferido && idiomaPreferido !== "auto") {
    userLocale = idiomaPreferido;
  } else {
    try {
      const { value } = await Device.getLanguageTag();
      if (value.startsWith("es")) {
        userLocale = "es";
      }
    } catch (e) {}
  }
  await setLocale(userLocale, { reload: false });
  return {
    locale: userLocale
  };
};

//recomendacion propia: usar archivos +layout.ts o +page.ts, para la carga de datos antes de renderizar
