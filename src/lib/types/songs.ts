import type { MediaFile } from "@odion-cloud/capacitor-mediastore";
import { m } from "$lib/paraglide/messages.js";

export type { MediaFile };

export const DEFAULT_COVER = "/default-cover.png";

/**
 * Retorna el título a mostrar.
 * Filtra la cadena "Unknown" generada por parches del plugin nativo para tratarla como ausente.
 */
export function displayTitle(file: MediaFile | null | undefined): string {
  if (!file) return m["unknown.title"]();
  const rawTitle = file.title?.trim();
  if (!rawTitle || rawTitle.toLowerCase() === "unknown") {
    return file.displayName?.trim() || m["unknown.title"]();
  }
  return rawTitle;
}

/**
 * Retorna el artista a mostrar.
 */
export function displayArtist(file: MediaFile | null | undefined): string {
  if (!file) return m["unknown.artist"]();
  const artist = file.artist?.trim();
  return artist && artist.toLowerCase() !== "unknown" ? artist : m["unknown.artist"]();
}

/**
 * Retorna el álbum a mostrar.
 */
export function displayAlbum(file: MediaFile | null | undefined): string {
  if (!file) return m["unknown.album"]();
  const album = file.album?.trim();
  return album && album.toLowerCase() !== "unknown" ? album : m["unknown.album"]();
}

/**
 * Retorna la URI de carátula o el fallback por defecto.
 */
export function displayImage(file: MediaFile | null | undefined): string {
  return file?.albumArtUri || DEFAULT_COVER;
}

