import type { InferSelectModel, InferInsertModel } from "drizzle-orm";
import * as schema from "./schema";

export type Playlist = InferSelectModel<typeof schema.playlists>;
export type NewPlaylist = InferInsertModel<typeof schema.playlists>;
