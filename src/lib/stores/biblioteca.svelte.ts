import { cargarBiblioteca, eliminarCanciones } from "$lib/services/files";
import { eliminarCancion } from "$lib/services/files";
import { ensureThumbnail } from "$lib/services/artworks";

import {m} from '$lib/paraglide/messages.js';
import {
  esCacheBibliotecaFresco,
  guardarCache,
  Item,
  obtenerCache,
} from "$lib/services/stores";
import { Capacitor } from "@capacitor/core";
import {
  displayTitle,
  displayArtist,
  displayAlbum,
  displayImage,
  type MediaFile,
} from "$lib/types/songs";
import { DialogType, ui } from "./ui.svelte";
import {
  removeManySongFromAllPlaylists,
  removeSongFromAllPlaylists,
} from "$lib/db/db/querys";
import { SortStrategies } from "./strategy/sortBy/strategy";
import type { SortableStore } from "$lib/types/sortable";

const LOTE_INICIAL = 1500;

class BibliotecaStore implements SortableStore {
  itemType: Item = Item.Songs;
  availableSortOptions: { value: string; label: string }[] = [
    { value: "title", label: m["menus.home_overflow_menu.order_by_options.title"]() },
    { value: "date", label: m["menus.home_overflow_menu.order_by_options.date"]() },
    { value: "duration", label: m["menus.home_overflow_menu.order_by_options.duration"]() },
  ];
  songs = $state<MediaFile[]>([]);
  loading = $state(false);
  error = $state<string | null>(null);
  loaded = $state(false);
  permissionDenied = $state<boolean>(false);
  songCount = $derived(this.songs.length);
  currentSort = $state<string>("");
  async load(forceScan = false): Promise<boolean> {
    if (this.loaded || this.loading) return false;

    console.log("cargar biblioteca");
    this.loading = true;
    this.error = null;

    try {
      if (!forceScan && (await esCacheBibliotecaFresco())) {
        // Caché fresca: usar lo que ya haya en memoria (poblado por el
        // layout) o volver a leerla por si acaso, sin tocar el device.
        if (this.songs.length === 0) {
          const cached = await obtenerCache();
          this.songs = this.#dedupePorId(cached);
        }
        this.loaded = true;
        this.loading = false;
        return false;
      }

      const rawInitial = await cargarBiblioteca(LOTE_INICIAL, 0);

      // Mergear con lo que ya haya (caché) sin duplicar por id
      this.#mergeSongs(rawInitial);

      this.loaded = true;
      this.loading = false;

      // Thumbnails del lote inicial en segundo plano, una sola pasada:
      // reprocesarlos al terminar el resto satura el hilo nativo sin
      // ganancia (el guard thumbnailsRunning ya los saltea mientras corren).
      void this.procesarThumbnails();

      // 2. Escanear el resto sin bloquear
      await this.loadRestInBackground();

      return true;
    } catch (e) {
      this.error = e instanceof Error ? e.message : "Error cargando biblioteca";
      console.error("BibliotecaStore:", e);
      this.loading = false;
      return false;
    }
  }

  #mergeSongs(nuevas: MediaFile[]): number {
    const prevLen = this.songs.length;
    const deduped = this.#dedupePorId([...this.songs, ...nuevas]);
    const strategy =
      SortStrategies[this.currentSort] ?? SortStrategies.titleAsc;
    this.songs = deduped.sort(strategy);
    return this.songs.length - prevLen;
  }

  #dedupePorId(lista: MediaFile[]): MediaFile[] {
    const vistos = new Set<string>();
    return lista.filter((s) => {
      if (vistos.has(s.id)) return false;
      vistos.add(s.id);
      return true;
    });
  }

  private async loadRestInBackground() {
    let currentOffset = LOTE_INICIAL;
    let hasMore = true;

    while (hasMore) {
      try {
        const batch = await cargarBiblioteca(LOTE_INICIAL, currentOffset);

        if (batch.length === 0) {
          hasMore = false;
          break;
        }

        const agregadas = this.#mergeSongs(batch);
        currentOffset += batch.length;

        // Batch sin NINGÚN id nuevo: ya vimos todo el resto en una pasada
        // previa (la paginación "fake" del plugin devuelve siempre el mismo
        // conjunto). Cortar acá deja de saturar el hilo nativo sin ganancia.
        if (agregadas === 0) {
          hasMore = false;
          break;
        }

        // Pausa generosa: deja ventanas en el hilo único de Capacitor para
        // que el MediaSession (setMetadata/setPositionState) avance entre
        // batchs.
        await new Promise((resolve) => setTimeout(resolve, 1200));
      } catch (e) {
        console.error("Error en carga en segundo plano:", e);
        hasMore = false;
      }
    }
  }

  async refresh() {
    // Re-escaneo forzado: ignora la frescura de la caché.
    this.loaded = false;
    this.songs = [];
    const oldSongCount = this.songCount;
    await this.load(true);
    if (oldSongCount < this.songCount) {
      guardarCache(this.songs);
    }
  }

  private thumbnailsRunning = false;
  private async procesarThumbnails() {
    if (!Capacitor.isNativePlatform()) return;
    if (this.thumbnailsRunning) return;
    this.thumbnailsRunning = true;
    try {
      const unique = [
        ...new Set(this.songs.map((s) => s.albumArtUri).filter(Boolean)),
      ];
      for (let i = 0; i < unique.length; i++) {
        await ensureThumbnail(unique[i]!);
        if (i % 5 === 4) {
          await new Promise((r) => setTimeout(r, 150));
        }
      }
    } finally {
      this.thumbnailsRunning = false;
    }
  }

  search(query: string) {
    const q = query.toLowerCase();
    return this.songs.filter(
      (s) =>
        displayTitle(s).toLowerCase().includes(q) ||
        displayArtist(s).toLowerCase().includes(q) ||
        displayAlbum(s).toLowerCase().includes(q),
    );
  }
  async deleteSong(songId: string, songUri: string) {
    try {
      const result = await eliminarCancion(songUri);
      if (!result) return;

      this.songs = this.songs.filter((el) => el.uri !== songUri);
      guardarCache(this.songs);
      await removeSongFromAllPlaylists(songId);
    } catch (error) {
      console.error(error);
      ui.openDialog(DialogType.Error, error);
    }
  }
  async deleteManySongs(songsIds: Set<string>) {
    try {
      const ids = Array.from(songsIds);
      const songsTodeleleUri = this.songs
        .filter((el) => songsIds.has(el.id))
        .map((el) => {
          return el.uri;
        });
      const result = await eliminarCanciones(songsTodeleleUri);
      if (result.error) {
        console.log(result.error);
        return;
      }
      this.songs = this.songs.filter((el) => !songsIds.has(el.id));
      guardarCache(this.songs);
      await removeManySongFromAllPlaylists(ids);
      console.log("cancion borrada con exito");
    } catch (error) {
      console.error(error);
      ui.openDialog(DialogType.Error, error);
    }
  }
  async sort(sortBy: string) {
    if (sortBy == this.currentSort) return;
    this.currentSort = sortBy;
    const strategy = SortStrategies[sortBy] ?? SortStrategies.titleAsc;
    this.songs = [...this.songs].sort(strategy);
  }
}

export const biblioteca = new BibliotecaStore();
