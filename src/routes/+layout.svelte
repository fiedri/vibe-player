<script lang="ts">
  import "./layout.css";
  import favicon from "$lib/assets/favicon.svg";
  import Hero from "$lib/components/ui/hero/hero.svelte";
  import Player from "$lib/components/ui/player/player.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { solicitarPermisosAudio } from "$lib/services/files";
  import { obtenerCache, guardarCache } from "$lib/services/stores";
  import { onMount, onDestroy } from "svelte";
  import { App } from "@capacitor/app";
  import { player } from "$lib/components/ui/player/playerStore.svelte";
  import { Capacitor } from "@capacitor/core";
  import { LocalNotifications } from "@capacitor/local-notifications"; // 👈 Importa esto

  let { children } = $props();
  let backListener: any = null;

  onMount(async () => {
   
    requestAnimationFrame(async () => {
      const cache = await obtenerCache();
      if (cache && cache.length > 0) {
        biblioteca.songs = cache;
      }
    });

    setTimeout(async () => {
      if (!biblioteca.loaded && !biblioteca.loading) {
        console.log("Iniciando verificación en segundo plano...");
        await solicitarPermisosAudio();
        await biblioteca.load();

        if (biblioteca.songs && biblioteca.songs.length > 0) {
          await guardarCache(biblioteca.songs);
        }
      }
    }, 2000);

    if (Capacitor.isNativePlatform()) {
      try {
        const check = await LocalNotifications.checkPermissions();
        if (check.display !== "granted") {
          const req = await LocalNotifications.requestPermissions();
          console.log("Resultado del permiso nativo:", req.display);
        }
      } catch (err) {
        console.error("Error al solicitar permisos:", err);
      }

      backListener = await App.addListener("backButton", (event) => {
        if (player.isOpened) {
          player.isOpened = false;
          return;
        }

        if (window.history.length > 1) {
          window.history.back();
        } else {
          App.exitApp();
        }
      });
    }
  });

  onDestroy(async () => {
    if (backListener) {
      const handler = await backListener;
      handler.remove();
    }
  });
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>
<div class="h-dvh flex flex-col justify-between border-r-0">
  <Hero />

  <div class="flex-1 flex flex-col min-h-0 overflow-hidden">
    {@render children()}
  </div>
  <footer>
    <Player />
  </footer>
</div>
