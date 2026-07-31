import { Preferences } from "@capacitor/preferences";

export interface PlayerState {
  trackId: string | number;
  position: number;
  timestamp: number;
  mode: string
}

export async function guardarCache(data: any[]) {
  await Preferences.set({
    key: "biblioteca_cache",
    value: JSON.stringify(data),
  });
}

export async function obtenerCache(): Promise<any[]> {
  const { value } = await Preferences.get({ key: "biblioteca_cache" });
  if (!value) return [];

  try {
    return JSON.parse(value);
  } catch (e) {
    console.error("Error al parsear el caché", e);
    return [];
  }
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
