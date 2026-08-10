import { biblioteca } from "./biblioteca.svelte.ts";

export interface AlbumInfo {
  name: string;
  artist: string;
  image: string;
  songCount: number;
}

class AlbumesStore {
  albums = $derived(this.#buildAlbums());

  get loaded() {
    return biblioteca.loaded;
  }

  #buildAlbums(): AlbumInfo[] {
    const map = new Map<string, AlbumInfo>();

    for (const song of biblioteca.songs) {
      const key = `${song.album ?? "Unknown"}||${song.artists ?? "Unknown"}`;

      let entry = map.get(key);
      if (!entry) {
        entry = {
          name: song.album ?? "Unknown",
          artist: song.artists ?? "Unknown",
          image: song.image ?? "/default-cover.png",
          songCount: 0,
        };
        map.set(key, entry);
      }

      // Primera imagen no vacía que encontremos
      if (entry.image === "/default-cover.png" && song.image) {
        entry.image = song.image;
      }

      entry.songCount++;
    }

    return [...map.values()].sort((a, b) => a.name.localeCompare(b.name));
  }

  search(query: string): AlbumInfo[] {
    const q = query.toLowerCase();
    return this.albums.filter(
      (a) =>
        a.name.toLowerCase().includes(q) ||
        a.artist.toLowerCase().includes(q),
    );
  }

  getAlbumInfo(albumName: string) {
    const albums = this.albums.filter((el) => el.name === albumName);
    const songs = biblioteca.songs.filter((el) => el.album === albumName);
    return {
      albums,
      songs,
    };
  }
}

export const albumes = new AlbumesStore();
