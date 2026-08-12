import { createClient } from "@libsql/client";
import { drizzle } from "drizzle-orm/libsql";
import { sqliteTable, text, integer } from "drizzle-orm/sqlite-core";
import { and, eq, min, notInArray, sql } from "drizzle-orm";
import { playlists, playlistsSongs } from "../db/schema";

async function runTest() {
  const client = createClient({ url: "file::memory:" });
  const db = drizzle({ client });

  await client.execute(`
    CREATE TABLE playlists (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      date_created TEXT DEFAULT CURRENT_TIMESTAMP
    );
  `);
  await client.execute(`
    CREATE TABLE playlists_songs (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      playlist_id INTEGER REFERENCES playlists(id) ON DELETE CASCADE,
      song_id TEXT NOT NULL
    );
  `);

  console.log("--- Creando registros de prueba ---");
  await db.insert(playlists).values({ name: "favoritos" });
  await db.insert(playlistsSongs).values([
    { playlistId: 1, songId: "song_a" },
    { playlistId: 1, songId: "song_a" },
    { playlistId: 1, songId: "song_a" },
    { playlistId: 1, songId: "song_b" },
    { playlistId: 1, songId: "song_b" },
    { playlistId: 1, songId: "song_b" },

  ]);
  /// select * from playlists where playlist.id = ? left join playlistsSongs ON playlist.id = playlistsSongs.playlistId
  console.log("Resultado devuelto por Drizzle:");
  const data = await db.select().from(playlistsSongs);
  console.log(data)
  
  /*.select({
      //minId: min(playlistsSongs.playlistId),
      playlist: playlistsSongs.playlistId,
      song: playlistsSongs.songId,
      rowId: sql<number>`min(rowid)`,
    })
    .from(playlistsSongs)
    .groupBy(playlistsSongs.songId);*/

  const data2 = await db.select().from(playlistsSongs);
  console.log(data2)
}

runTest();
