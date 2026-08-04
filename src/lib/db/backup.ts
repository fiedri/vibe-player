import * as db from "./db/querys";
import { playlistStore } from "$lib/stores/playlist.svelte";

export interface PlaylistBackup {
  formatVersion: 1;
  exportedAt: string;
  playlists: { name: string; songIds: string[] }[];
}

/**
 * Genera un backup en JSON de las playlists (guarda los id de las canciones,
 * no su contenido binario).
 *
 * Razón de existir: Android Auto-Backup quedó desactivado (fix del
 * "CapacitorSQLitePlugin: null"), así las playlists no se restauran solas al
 * reinstalar. Este backup el usuario lo copia/guarda y lo importa después.
 */
export async function exportPlaylistsBackup(): Promise<string> {
  const playlists = await db.getPlaylists();

  const entries: PlaylistBackup["playlists"] = [];
  for (const p of playlists) {
    const detail = await db.getPlaylistSongs(p.id);
    const songIds = detail ? detail.playlistsSongs.map((s) => s.songId) : [];
    entries.push({ name: p.name, songIds });
  }

  const payload: PlaylistBackup = {
    formatVersion: 1,
    exportedAt: new Date().toISOString(),
    playlists: entries,
  };

  return JSON.stringify(payload, null, 2);
}

/**
 * Restaura las playlists desde un JSON de backup. Crea cada playlist y le
 * asigna sus canciones. Al terminar refresca el store para actualizar la UI.
 *
 * @returns cantidad de playlists importadas
 */
export async function importPlaylistsBackup(jsonStr: string): Promise<number> {
  let raw: unknown;
  try {
    raw = JSON.parse(jsonStr);
  } catch {
    throw new Error("El texto no es un JSON válido.");
  }

  if (
    !raw ||
    typeof raw !== "object" ||
    (raw as PlaylistBackup).formatVersion !== 1 ||
    !Array.isArray((raw as PlaylistBackup).playlists)
  ) {
    throw new Error("Formato de backup inválido.");
  }

  const backup = raw as PlaylistBackup;
  for (const item of backup.playlists) {
    const { id } = await db.createPlaylist(item.name);
    for (const songId of item.songIds) {
      await db.addSongToPlaylist(id, songId);
    }
  }

  await playlistStore.loadPlaylist();
  return backup.playlists.length;
}