import * as db from "./db/querys";
import { playlistStore } from "$lib/stores/playlist.svelte";
import { favorites } from "$lib/stores/favorites.svelte";

export interface PlaylistBackup {
  formatVersion: 1;
  exportedAt: string;
  playlists: { name: string; id: number; songIds: string[] }[];
}

export async function exportPlaylistsBackup(): Promise<string> {
  const playlists = await db.getPlaylists();

  const entries: PlaylistBackup["playlists"] = [];
  for (const p of playlists) {
    const detail = await db.getPlaylistSongs(p.id);
    const songIds = detail ? detail.playlistsSongs.map((s) => s.songId) : [];
    entries.push({ name: p.name, id: p.id, songIds });
  }

  const payload: PlaylistBackup = {
    formatVersion: 1,
    exportedAt: new Date().toISOString(),
    playlists: entries,
  };

  return JSON.stringify(payload, null, 2);
}

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
    let targetId: number = item.id;
    const isFavorite = item.name === "favoritos" && targetId == 1;
    if (!isFavorite) {

      const existing = await db.getPlaylistByName(item.name);
      if (!existing) {
        await db.createPlaylist(item.name);
      }
    }

    await db.addNoExistingSongs(targetId, item.songIds);
  }

  await playlistStore.loadPlaylist();
  return backup.playlists.length;
}

