import * as schema from "./schema";
import { defineRelations } from "drizzle-orm";

export const schemaRelations = defineRelations(schema, (r) => ({
  playlists: {
    playlistsSongs: r.many.playlistsSongs(),
  },
  playlistsSongs: {
    playlist: r.one.playlists({
      from: r.playlistsSongs.playlistId,
      to: r.playlists.id,
    }),
  },
}));
