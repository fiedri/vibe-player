import { biblioteca } from "./biblioteca.svelte.ts";
import {
  displayAlbum,
  displayArtist,
  displayImage,
  DEFAULT_COVER,
} from "$lib/types/songs";
import type { SortableStore } from "$lib/types/sortable.ts";
import { SortStrategies } from "./strategy/sortBy/strategy.ts";
import { Item } from "$lib/services/stores.js";
export interface AlbumInfo {
  title: string;
  artist: string;
  image: string;
  songCount: number;
}

class AlbumesStore implements SortableStore {
  itemType: Item = Item.Albums;
  availableSortOptions: { value: string; label: string }[] = [
    { value: "title", label: "Título" },
    { value: "artist", label: "Artista" },
  ];
  currentSort: string = $state("");
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
          title: albumName,
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
    const strategy = SortStrategies[this.currentSort] ?? SortStrategies.titleAsc;
    return [...map.values()].sort(strategy);
  }

  search(query: string): AlbumInfo[] {
    const q = query.toLowerCase();
    return this.albums.filter(
      (a) =>
        a.title.toLowerCase().includes(q) || a.artist.toLowerCase().includes(q),
    );
  }

  getAlbumInfo(albumName: string) {
    const albums = this.albums.filter((el) => el.title === albumName);
    const songs = biblioteca.songs.filter(
      (el) => displayAlbum(el) === albumName,
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

export const albumes = new AlbumesStore();
