import { biblioteca } from "./biblioteca.svelte";
import { albumes } from "./albumes.svelte";
import { displayArtist, displayImage, DEFAULT_COVER } from "$lib/types/songs";
import type { SortableStore } from "$lib/types/sortable";
import { Item } from "$lib/services/stores";
import { SortStrategies } from "./strategy/sortBy/strategy";

export interface artist {
  image: string;
  name: string;
  songCount: number;
}

class ArtistStore implements SortableStore {
  itemType: Item = Item.Artists;
  availableSortOptions: { value: string; label: string }[] = [
    { value: "name", label: "Nombre" },
    { value: "songCount", label: "N. Canciones" },
  ];

  currentSort = $state<string>("");
  artists = $derived(this.#buildArtists());

  get loaded() {
    return biblioteca.loaded;
  }

#buildArtists(): artist[] {
  const map = new Map<string, artist>();

  for (const song of biblioteca.songs) {
    const fullArtist = displayArtist(song);
    
    const rawArtists = fullArtist.split(/ feat\.?| ft\.?| & |,|\//i);
    
    const uniqueArtistsInSong = new Set(
      rawArtists.map(a => a.trim()).filter(Boolean)
    );

    for (const artistName of uniqueArtistsInSong) {
      const key = artistName.toLowerCase();

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
  }

  const strategy = SortStrategies[this.currentSort] ?? SortStrategies.nameAsc;
  return [...map.values()].sort(strategy);
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
  async sort(sortBy: string) {
    if (sortBy == this.currentSort) return;
    this.currentSort = sortBy;
  }
}

export const artists = new ArtistStore();
