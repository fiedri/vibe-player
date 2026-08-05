import { CapacitorMediaStore } from "@odion-cloud/capacitor-mediastore";
import type { Song } from "$lib/types/songs";

interface MediaFile {
  id: string | number;
  uri: string;
  displayName: string;
  size: number;
  mimeType: string;
  dateAdded: number;
  mediaType: string;
  title?: string;
  artist?: string;
  album?: string;
  albumArtist?: string;
  composer?: string;
  duration?: number;
  genre?: string;
  year?: number;
  track?: number;
  albumArtUri?: string;
  width?: number;
  height?: number;
  isExternal?: boolean;
}

export async function solicitarPermisosAudio() {
  try {
    return await CapacitorMediaStore.requestPermissions({ types: ["audio"] });
  } catch (e) {
    console.warn("Error al pedir permisos:", e);
  }
}

// files.ts
export async function cargarBiblioteca(limit = 500, offset = 0): Promise<MediaFile[]> {


  const opciones: any = {
    mediaType: "audio",
    sortBy: "DATE",
    includeExternal: true,
  };

  // Añadimos la paginación a la consulta nativa
  if (limit > 0) opciones.limit = limit;
  if (offset > 0) opciones.offset = offset;

  const resultado = await CapacitorMediaStore.getMediasByType(opciones);
  return resultado.media || [];
}



export function formatbiblioteca(biblioteca: MediaFile[]): Song[] {
  return biblioteca.map((song): Song => {
    const {
      id,
      title,
      duration,
      album,
      artist,
      uri,
      albumArtUri,
      displayName,
    } = song;

    const safeTitle = title || displayName || "Sin título";
    const safeArtist = artist || "Artista desconocido";
    const safeAlbum = album || "Álbum desconocido";
    const safeDuration = duration;
    const safeImage = albumArtUri || "/default-cover.png";

    return {
      id: String(id),
      title: safeTitle,
      duration: safeDuration,
      album: safeAlbum,
      artists: safeArtist,
      audioUrl: uri,
      image: safeImage,
    };
  });
}
