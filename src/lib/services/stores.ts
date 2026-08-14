import { Preferences } from "@capacitor/preferences";

export interface PlayerState {
  trackId: string | number;
  position: number;
  timestamp: number;
  mode: string
}

const CACHE_KEY = "biblioteca_cache_v2";
const CACHE_TIMESTAMP_KEY = "biblioteca_cache_timestamp_v2";
const CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000; // 24 h

export async function guardarCache(data: any[]) {
  await Preferences.set({
    key: CACHE_KEY,
    value: JSON.stringify(data),
  });
  await Preferences.set({
    key: CACHE_TIMESTAMP_KEY,
    value: String(Date.now()),
  });
}

export async function obtenerCache(): Promise<any[]> {
  const { value } = await Preferences.get({ key: CACHE_KEY });
  if (!value) return [];

  try {
    return JSON.parse(value);
  } catch (e) {
    console.error("Error al parsear el caché", e);
    return [];
  }
}

/**
 * ¿La caché de la biblioteca es fresca (< 24h)? El timestamp lo escribe
 * guardarCache() cuando ocurre un escaneo real del device. Si la app se
 * cerró hace menos de un día, la caché se considera fresca y se evita
 * re-escanear el MediaStore (que satura el hilo único de Capacitor).
 */
export async function esCacheBibliotecaFresco(): Promise<boolean> {
  const { value } = await Preferences.get({ key: CACHE_TIMESTAMP_KEY });
  if (!value) return false;
  const timestamp = Number(value);
  if (!Number.isFinite(timestamp)) return false;
  return Date.now() - timestamp < CACHE_MAX_AGE_MS;
}
export async function guardarEstadoReproductor(songId: string | number, currentTime: number, mode: string = 'off') {
  const estado: PlayerState = {
    trackId: songId,
    position: currentTime,
    timestamp: Date.now(),
    mode
  };
  
  console.log("🎵 Guardando estado:", estado);
  await Preferences.set({ 
    key: "player_state", 
    value: JSON.stringify(estado)
  });
}

export async function cargarEstadoReproductor(): Promise<PlayerState | undefined> {
  const { value } = await Preferences.get({ key: "player_state" });
  console.log('cargando estado', value);
  if (!value) return;

  try {
    return JSON.parse(value);
  } catch (e) {
    console.error("Error al parsear el estado del reproductor", e);
    return;
  }
}
