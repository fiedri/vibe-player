import type { MediaFile } from "@odion-cloud/capacitor-mediastore";

export type { MediaFile };

export const UNKNOWN_TITLE = "Sin título";
export const UNKNOWN_ARTIST = "Artista desconocido";
export const UNKNOWN_ALBUM = "Álbum desconocido";
export const DEFAULT_COVER = "/default-cover.png";

/**
 * Retorna el título a mostrar.
 * Filtra la cadena "Unknown" generada por parches del plugin nativo para tratarla como ausente.
 */
export function displayTitle(m: MediaFile | null | undefined): string {
  if (!m) return UNKNOWN_TITLE;
  const rawTitle = m.title?.trim();
  if (!rawTitle || rawTitle.toLowerCase() === "unknown") {
    return m.displayName?.trim() || UNKNOWN_TITLE;
  }
  return rawTitle;
}

/**
 * Retorna el artista a mostrar.
 */
export function displayArtist(m: MediaFile | null | undefined): string {
  if (!m) return UNKNOWN_ARTIST;
  const artist = m.artist?.trim();
  return artist && artist.toLowerCase() !== "unknown" ? artist : UNKNOWN_ARTIST;
}

/**
 * Retorna el álbum a mostrar.
 */
export function displayAlbum(m: MediaFile | null | undefined): string {
  if (!m) return UNKNOWN_ALBUM;
  const album = m.album?.trim();
  return album && album.toLowerCase() !== "unknown" ? album : UNKNOWN_ALBUM;
}

/**
 * Retorna la URI de carátula o el fallback por defecto.
 */
export function displayImage(m: MediaFile | null | undefined): string {
  return m?.albumArtUri || DEFAULT_COVER;
}

