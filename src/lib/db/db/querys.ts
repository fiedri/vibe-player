import { getDb } from ".";
import { eq, count, sql, and, inArray } from "drizzle-orm";
import { playlists, playlistsSongs } from "./schema";
import { expoIn } from "svelte/easing";
import { favorites } from "$lib/stores/favorites.svelte";

export async function getPlaylists() {
  const db = await getDb();
  return await db
    .select({
      id: playlists.id,
      name: playlists.name,
      songsCount: sql<number>`count(${playlistsSongs.songId})`.as("songsCount"),
    })
    .from(playlists)
    .leftJoin(playlistsSongs, eq(playlists.id, playlistsSongs.playlistId))
    .groupBy(playlists.id);
}

export async function createPlaylist(name: string) {
  const db = await getDb();
  const [result] = await db
    .insert(playlists)
    .values({ name })
    .returning({ id: playlists.id });
  return result;
}

export async function deletePlaylist(id: number) {
  const db = await getDb();
  await db.delete(playlists).where(eq(playlists.id, id));
}

export async function getPlaylistSongs(playlistId: number) {
  const db = await getDb();
  const result = await db.query.playlists.findFirst({
    where: {
      id: { eq: playlistId },
    },
    with: { playlistsSongs: true },
  });
  return result;
}

export async function addSongToPlaylist(playlistId: number, songId: string) {
  const db = await getDb();
  await db.insert(playlistsSongs).values({ playlistId, songId });
}

export async function removeSongFromPlaylist(
  playlistId: number,
  songId: string,
) {
  const db = await getDb();
  await db
    .delete(playlistsSongs)
    .where(
      and(
        eq(playlistsSongs.playlistId, playlistId),
        eq(playlistsSongs.songId, songId),
      ),
    );
}

export async function removeSongFromAllPlaylists(songId: string) {
  const db = await getDb();
  await db.delete(playlistsSongs).where(eq(playlistsSongs.songId, songId));
}

export async function removeManySongsFromPlaylist(
  playlistId: number,
  songIds: string[],
) {
  if (songIds.length === 0) return;
  const db = await getDb();
  await db
    .delete(playlistsSongs)
    .where(
      and(
        eq(playlistsSongs.playlistId, playlistId),
        inArray(playlistsSongs.songId, songIds),
      ),
    );
}

export async function removeManySongFromAllPlaylists(songIds: string[]) {
  if (songIds.length === 0) return;
  const db = await getDb();
  await db
    .delete(playlistsSongs)
    .where(inArray(playlistsSongs.songId, songIds));
}

export async function addManySongsToPlaylists(
  playlistId: number,
  songsId: string[],
) {
  if (songsId.length === 0) return;

  const db = await getDb();

  const valuesToInsert = songsId.map((songId) => ({
    playlistId: playlistId,
    songId: songId,
  }));

  await db.insert(playlistsSongs).values(valuesToInsert);
}

export async function getFavoritesInfo() {
  const db = await getDb();
  const favorito = "favoritos";
  return await db.query.playlists.findFirst({
    where: {
      name: favorito,
    },
    with: {
      playlistsSongs: true,
    },
  });
}
