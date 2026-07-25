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
      const rawInitial = await cargarBiblioteca(100, 0);
      const iniciales = formatbiblioteca(rawInitial);

      // Solo si la caché estaba completamente vacía mostramos estas 100 de golpe
      if (this.songs.length === 0) {
        this.songs = iniciales;
      }

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

  private async loadRestInBackground() {
    let currentOffset = 100;
    let hasMore = true;
    let todasLasNuevas: Song[] = [];

    while (hasMore) {
      try {
        const batch = await cargarBiblioteca(500, currentOffset);

        if (batch.length === 0) {
          hasMore = false;
          break;
        }

        const formateadas = formatbiblioteca(batch);
        todasLasNuevas.push(...formateadas);
        currentOffset += batch.length;

        // PAUSA TÁCTICA: Dejamos respirar al Event Loop 50ms entre lotes 
        // para que la interfaz mantenga 60fps
        await new Promise((resolve) => setTimeout(resolve, 50));

      } catch (e) {
        console.error("Error en carga en segundo plano:", e);
        hasMore = false;
      }
    }

    // 3. Si el escaneo encontró más canciones de las que teníamos en caché, actualizamos
    if (todasLasNuevas.length > 0) {
      const totalEscaneado = [...this.songs.slice(0, 100), ...todasLasNuevas];
      
      // Solo actualizamos si la cantidad es distinta para evitar re-renders innecesarios
      if (totalEscaneado.length !== this.songs.length) {
        this.songs = totalEscaneado;
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
