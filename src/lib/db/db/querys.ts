import { getDb } from ".";
import { eq, count, sql, and } from "drizzle-orm";
import { playlists, playlistsSongs } from "./schema";

const db = await getDb();

export async function getPlaylists() {
return await db
    .select({
      id: playlists.id,
      name: playlists.name,
      songsCount: sql<number>`count(${playlistsSongs.songId})`.as('songsCount'),
    })
    .from(playlists)
    .leftJoin(playlistsSongs, eq(playlists.id, playlistsSongs.playlistId))
    .groupBy(playlists.id);
}

export async function createPlaylist(name: string) {
  const [result] = await db
    .insert(playlists)
    .values({ name })
    .returning({ id: playlists.id });
  return result;
}

export async function deletePlaylist(id: number) {
  await db.delete(playlists).where(eq(playlists.id, id));
}

export async function getPlaylistSongs(playlistId: number) {
const result = await db.query.playlists.findFirst({
  where: {
    id: { eq: playlistId}, 
  },
  with: { playlistsSongs: true }
});
return result
}

export async function addSongToPlaylist(playlistId: number, songId: string) {
  await db.insert(playlistsSongs).values({ playlistId, songId });
}

export async function removeSongFromPlaylist(playlistId: number, songId: string) {
  await db
    .delete(playlistsSongs)
    .where(and(eq(playlistsSongs.playlistId, playlistId), eq(playlistsSongs.songId, songId)));
}
