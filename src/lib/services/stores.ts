import { Preferences } from "@capacitor/preferences";

// Es asíncrono, no bloquea la interfaz
export async function guardarCache(data) {
  // Es asíncrono, no bloquea la interfaz
  await Preferences.set({
    key: "biblioteca_cache",
    value: JSON.stringify(data),
  });
}

export async function obtenerCache() {
  const { value } = await Preferences.get({ key: "biblioteca_cache" });
  if (!value) return [];

  try {
    return JSON.parse(value);
  } catch (e) {
    console.error("Error al parsear el caché", e);
    return [];
  }
}
