import { biblioteca } from "./biblioteca.svelte.ts";
import { displayAlbum, displayArtist, displayImage, DEFAULT_COVER } from "$lib/types/songs";

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
      const albumName = displayAlbum(song);
      const artistName = displayArtist(song);
      const key = `${albumName}||${artistName}`;

      let entry = map.get(key);
      if (!entry) {
        entry = {
          name: albumName,
          artist: artistName,
          image: displayImage(song),
          songCount: 0,
        };
        map.set(key, entry);
      }

      // Primera imagen no vacía que encontremos
      if (entry.image === DEFAULT_COVER) {
        const cover = displayImage(song);
        if (cover !== DEFAULT_COVER) {
          entry.image = cover;
        }
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
    const songs = biblioteca.songs.filter((el) => displayAlbum(el) === albumName);
    return {
      albums,
      songs,
    };
  }
}

export const albumes = new AlbumesStore();
