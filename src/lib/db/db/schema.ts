import { sqliteTable, text, integer } from "drizzle-orm/sqlite-core";

export const playlists = sqliteTable("playlists", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  name: text("name").notNull(),
  dateCreated: text("date_created").default("CURRENT_TIMESTAMP"),
});

export const playlistsSongs = sqliteTable("playlists_songs", {
  id: integer("id").primaryKey({ autoIncrement: true }),
  playlistId: integer("playlist_id").references(() => playlists.id, {
    onDelete: "cascade",
  }),
  songId: text("song_id").notNull(),
});
