import { drizzle } from "drizzle-orm/sqlite-proxy";
import { schemaRelations } from "./relations";
import { CapacitorSQLite, SQLiteConnection } from "@capacitor-community/sqlite";

const sqlite = new SQLiteConnection(CapacitorSQLite);
export type Database = Awaited<ReturnType<typeof initDatabase>>;
let dbInitPromise: Promise<Database> | null = null;

async function initDatabase() {
  const dbName = "vibe_music_db";
  await sqlite.checkConnectionsConsistency();
  const isConn = await sqlite.isConnection(dbName, false);
  let dbConnection;
  if (isConn.result) {
    dbConnection = await sqlite.retrieveConnection(dbName, false);
  } else {
    dbConnection = await sqlite.createConnection(
      dbName,
      false,
      "no-encryption",
      1,
      false,
    );
  }

  await dbConnection.open();

  // Solo tablas de datos de usuario (playlists)
  await crearTablas(dbConnection);

  return drizzle(
    async (sql, params, method) => {
      try {
        if (sql === "BEGIN") {
          await dbConnection.beginTransaction();
          return { rows: [] };
        }
        if (sql === "COMMIT") {
          await dbConnection.commitTransaction();
          return { rows: [] };
        }
        if (sql === "ROLLBACK") {
          await dbConnection.rollbackTransaction();
          return { rows: [] };
        }

        const isSelect =
          method === "all" || method === "get" || method === "values";

        if (isSelect) {
          const res = await dbConnection.query(sql, params);
          return {
            rows: res.values ? res.values.map((r) => Object.values(r)) : [],
          };
        } else {
          const res = await dbConnection.run(sql, params);
          return { rows: [] };
        }
      } catch (e) {
        console.error("Error ejecutando query:", e);
        throw e;
      }
    },
    { relations: schemaRelations },
  );
}

async function crearTablas(conn: any) {
  const tablas = [
    `CREATE TABLE IF NOT EXISTS playlists (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      date_created TEXT DEFAULT CURRENT_TIMESTAMP
    );`,
    `CREATE TABLE IF NOT EXISTS playlists_songs (
      playlist_id INTEGER REFERENCES playlists(id) ON DELETE CASCADE,
      song_id TEXT NOT NULL
    );`,
  ];
  for (const sql of tablas) {
    await conn.run(sql, []);
  }
}

export async function getDb() {
  if (!dbInitPromise) {
    dbInitPromise = initDatabase();
  }
  return dbInitPromise;
}
