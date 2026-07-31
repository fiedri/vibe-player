import { cargarBiblioteca, formatbiblioteca } from "$lib/services/files";
import type { Song } from "$lib/types/songs";

class BibliotecaStore {
  songs = $state<Song[]>([]);
  loading = $state(false);
  error = $state<string | null>(null);
  loaded = $state(false);

  songCount = $derived(this.songs.length);

  async load() {
    if (this.loaded || this.loading) return;

    this.loading = true;
    this.error = null;

    try {
      // 1. Cargar el primer lote inicial desde el dispositivo
      const rawInitial = await cargarBiblioteca(500, 0);
      const iniciales = formatbiblioteca(rawInitial);

      // Mergear con lo que ya haya (caché) sin duplicar por audioUrl
      this.#mergeSongs(iniciales);

      this.loaded = true;
      this.loading = false;

      // 2. Escanear el resto sin bloquear
      await this.loadRestInBackground();

    } catch (e) {
      this.error = e instanceof Error ? e.message : "Error cargando biblioteca";
      console.error("BibliotecaStore:", e);
      this.loading = false;
    }
  }

  /** Mergea canciones nuevas evitando duplicados por audioUrl */
  #mergeSongs(nuevas: Song[]) {
    const existentes = new Set(this.songs.map((s) => s.audioUrl));
    const aAgregar = nuevas.filter((s) => !existentes.has(s.audioUrl));
    if (aAgregar.length > 0) {
      this.songs = [...this.songs, ...aAgregar];
    }
  }

  private async loadRestInBackground() {
    let currentOffset = 500;
    let hasMore = true;

    while (hasMore) {
      try {
        const batch = await cargarBiblioteca(500, currentOffset);

        if (batch.length === 0) {
          hasMore = false;
          break;
        }

        const formateadas = formatbiblioteca(batch);
        this.#mergeSongs(formateadas);
        currentOffset += batch.length;

        await new Promise((resolve) => setTimeout(resolve, 500));

      } catch (e) {
        console.error("Error en carga en segundo plano:", e);
        hasMore = false;
      }
    }
  }

  async refresh() {
    this.loaded = false;
    this.songs = [];
    await this.load();
  }

  search(query: string) {
    const q = query.toLowerCase();
    return this.songs.filter(
      (s) =>
        s.title.toLowerCase().includes(q) ||
        s.artists.toLowerCase().includes(q) ||
        s.album.toLowerCase().includes(q)
    );
  }
}

export const biblioteca = new BibliotecaStore();
