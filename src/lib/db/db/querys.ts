import { getDb } from ".";
import { eq, like, min, notInArray, sql, and, inArray } from "drizzle-orm";
import { playlists, playlistsSongs } from "./schema";

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
  if (name.toLowerCase() === "favoritos") {
    throw new Error("No puedes crear otra playlist llamada 'Favoritos'");
  }
  const db = await getDb();
  const existing = await db
    .select({ name: playlists.name })
    .from(playlists)
    .where(like(playlists.name, `${name}%`));

  const existingNames = new Set(existing.map((p) => p.name.toLowerCase()));

  let playlistName = name;
  let counter = 1;

  while (existingNames.has(playlistName.toLowerCase())) {
    playlistName = `${name} (${counter})`;
    counter++;
  }

  const [result] = await db
    .insert(playlists)
    .values({ name: playlistName })
    .returning();

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
export async function deleteDuplicates(playlistId: number) {
  const db = await getDb();
  try {
    const validIds = db
      .select({
        id: min(playlistsSongs.id),
      })
      .from(playlists)
      .innerJoin(playlistsSongs, eq(playlists.id, playlistsSongs.playlistId))
      .where(eq(playlists.id, playlistId))
      .groupBy(playlistsSongs.songId);

    await db
      .delete(playlistsSongs)
      .where(
        and(
          eq(playlistsSongs.playlistId, playlistId),
          notInArray(playlistsSongs.id, validIds),
        ),
      );
  } catch (e) {
    console.error(e);
  }
}

export async function addNoExistingSongs(
  playlistId: number,
  songsId: string[],
) {
  if (songsId.length === 0) return;

  const db = await getDb();
  const existingSongs = await db
    .select({ id: playlistsSongs.songId })
    .from(playlistsSongs)
    .where(
      and(
        eq(playlistsSongs.playlistId, playlistId),
        inArray(playlistsSongs.songId, songsId),
      ),
    );
  const existingIds = new Set(existingSongs.map((e) => e.id));
  const idToInsert = songsId.filter((id) => !existingIds.has(id));
  if (idToInsert.length > 0) {
    const valuesToInsert = idToInsert.map((id) => ({
      playlistId,
      songId: id,
    }));
    await db.insert(playlistsSongs).values(valuesToInsert);
  }
}

export async function getPlaylistByName(name: string) {
  const db = await getDb();
  return db.select().from(playlists).where(eq(playlists.name, name)).get()
}
