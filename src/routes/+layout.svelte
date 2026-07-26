<script lang="ts">
  import "./layout.css";
  import favicon from "$lib/assets/favicon.svg";
  import Hero from "$lib/components/ui/hero/hero.svelte";
  import Player from "$lib/components/ui/player/player.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { obtenerCache, guardarCache } from "$lib/services/stores";
  import { onMount } from "svelte";

  let { children } = $props();

  onMount(async () => {
    const cache = await obtenerCache();
    if (cache && cache.length > 0) {
      biblioteca.songs = cache;
      console.log(`Caché cargada instantáneamente: ${cache.length} canciones.`);
    }

    setTimeout(async () => {
      if (!biblioteca.loaded && !biblioteca.loading) {
        console.log("Iniciando verificación en segundo plano...");
        await biblioteca.load();

        if (biblioteca.songs && biblioteca.songs.length > 0) {
          await guardarCache(biblioteca.songs);
        }
      }
    }, 500);
  });
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>
<div class="h-dvh flex flex-col justify-between border-r-0">
  <Hero />
  <!-- Aseguramos flex flex-col min-h-0 para que los hijos sepan exactamente cuánto espacio vertical tienen -->
  <div class="flex-1 flex flex-col min-h-0 overflow-hidden">
    {@render children()}
  </div>
  <footer>
    <Player />
  </footer>
</div>
