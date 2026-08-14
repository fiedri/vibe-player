import { biblioteca } from "./biblioteca.svelte";
import { albumes } from "./albumes.svelte";
import { displayArtist, displayImage, DEFAULT_COVER } from "$lib/types/songs";

export interface artist {
  image: string;
  name: string;
  songCount: number;
}

class ArtistStore {
  artists = $derived(this.#buildArtists());

  get loaded() {
    return biblioteca.loaded;
  }

  #buildArtists(): artist[] {
    const map = new Map<string, artist>();

    for (const song of biblioteca.songs) {
      const artistName = displayArtist(song);
      const key = artistName;

      let entry = map.get(key);
      if (!entry) {
        entry = {
          name: artistName,
          image: displayImage(song),
          songCount: 0,
        };
        map.set(key, entry);
      }
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

  search(query: string): artist[] {
    const q = query.toLowerCase();
    return this.artists.filter((a) => a.name.toLowerCase().includes(q));
  }
  getAllArtistInfo(artisName: string) {
    const albums = albumes.albums.filter((el) => el.artist.includes(artisName));
    const songs = biblioteca.songs.filter((el) =>
      displayArtist(el).includes(artisName),
    );
    return {
      albums,
      songs,
    };
  }
}

export const artists = new ArtistStore();
