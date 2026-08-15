import { Preferences } from "@capacitor/preferences";
import { Filesystem, Directory, Encoding } from "@capacitor/filesystem";
import { Capacitor } from "@capacitor/core";
export interface PlayerState {
  trackId: string | number;
  position: number;
  timestamp: number;
  mode: string;
}

const CACHE_KEY = "biblioteca_cache_v2";
const CACHE_TIMESTAMP_KEY = "biblioteca_cache_timestamp_v2";
const CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000; // 24 h

export async function guardarCache(data: any[]) {
  try {
    await Filesystem.writeFile({
      path: `${CACHE_KEY}.json`,
      data: JSON.stringify(data),
      directory: Directory.Data,
      encoding: Encoding.UTF8,
    });

    await Filesystem.writeFile({
      path: `${CACHE_TIMESTAMP_KEY}.json`,
      data: String(Date.now()),
      directory: Directory.Data,
      encoding: Encoding.UTF8,
    });
  } catch (error) {
    console.error("Error al guardar en caché:", error);
  }
}

export async function obtenerCache(): Promise<any[]> {
  try {
    const fileUri = await Filesystem.getUri({
      path: `${CACHE_KEY}.json`,
      directory: Directory.Data,
    });

    const response = await fetch(Capacitor.convertFileSrc(fileUri.uri));
    if (!response.ok) return [];
    const data = await response.json();
    return data ?? [];
  } catch (error) {
    console.log("No se encontró caché o hubo un error al leerlo.");
    return [];
  }
}
export async function esCacheBibliotecaFresco(): Promise<boolean> {
  try {
    const result = await Filesystem.readFile({
      path: `${CACHE_TIMESTAMP_KEY}.json`,
      directory: Directory.Data,
      encoding: Encoding.UTF8,
    });

    const timestamp = Number(result.data);
    if (!Number.isFinite(timestamp)) return false;
    return Date.now() - timestamp < CACHE_MAX_AGE_MS;
  } catch (error) {
    console.log(error);
    return false;
  }
}
export async function guardarEstadoReproductor(
  songId: string | number,
  currentTime: number,
  mode: string = "off",
) {
  const estado: PlayerState = {
    trackId: songId,
    position: currentTime,
    timestamp: Date.now(),
    mode,
  };

  console.log("🎵 Guardando estado:", estado);
  await Preferences.set({
    key: "player_state",
    value: JSON.stringify(estado),
  });
}

export async function cargarEstadoReproductor(): Promise<
  PlayerState | undefined
> {
  const { value } = await Preferences.get({ key: "player_state" });
  console.log("cargando estado", value);
  if (!value) return;

  try {
    return JSON.parse(value);
  } catch (e) {
    console.error("Error al parsear el estado del reproductor", e);
    return;
  }
}
const OrderByKey = "orderByPreferences";
export enum Item{
  Songs= "songs",
  Albums = "Albums",
  Artists = "Artistas",
  Playlist = "Playlist"
}
export async function guardarParametrosDeOrdenamiento(data, item: Item) {
  await Preferences.set({ key: `OrderByKey-${item}`, value: JSON.stringify(data) });
}
export async function cargarParametrosDeOrdenamiento(item: Item) {
  const { value } = await Preferences.get({ key: `OrderByKey-${item}` });
  console.log("cargando", value);
  if (!value) return;

  try {
    return JSON.parse(value);
  } catch (e) {
    console.error("Error", e);
    return;
  }
}
