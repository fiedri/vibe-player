import { biblioteca } from "./biblioteca.svelte";

export interface artist{
image: string;
name: string;
songCount: number;
}


class ArtistStore{

  artists = $derived(this.#buildArtists());

  get loaded() {
    return biblioteca.loaded;
  }
  // agregar separacion de artistas: una cacion tiene varios artistas, en este caso es un string y lo 
  // artistas se separan por coma
  #buildArtists(): artist[] {
    const map = new Map<string, artist>();

    for (const song of biblioteca.songs) {
      const key = `${song.artists ?? "Unknown"}||${song.artists ?? "Unknown"}`;

      let entry = map.get(key);
      if (!entry) {
        entry = {
          name: song.artists ?? "Unknown",
          image: song.image ?? "/default-cover.png",
          songCount: 0,
        };
        map.set(key, entry);
      }
      if (entry.image === "/default-cover.png" && song.image) {
        entry.image = song.image;
      }
      entry.songCount++;
    }

    return [...map.values()].sort((a, b) => a.name.localeCompare(b.name));
  }

  search(query: string): artist[] {
    const q = query.toLowerCase();
    return this.artists
    .filter(
      (a) =>
        a.name.toLowerCase().includes(q) ||
        a.artist.toLowerCase().includes(q),
    );
  }
}

export const artists = new ArtistStore()
