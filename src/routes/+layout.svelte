<script lang="ts">
  import "./layout.css";
  import favicon from "$lib/assets/favicon.svg";
  import Player from "$lib/components/ui/player/player.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { solicitarPermisosAudio } from "$lib/services/files";
  import {
    guardarCache,
    guardarEstadoReproductor,
    obtenerCache,
  } from "$lib/services/stores";
  import { onMount, onDestroy } from "svelte";
  import { App } from "@capacitor/app";
  import { player } from "$lib/components/ui/player/playerStore.svelte";
  import { Capacitor } from "@capacitor/core";
  import { LocalNotifications } from "@capacitor/local-notifications";
    import Dialog from "$lib/components/ui/dialog.svelte";
  let { children } = $props();
  let backListener: any = null;
  let pauseListener: any = null;

  onMount(async () => {
    player.init();
    await player.loadLastSavedState();
    await solicitarPermisosAudio();
    setTimeout(async () => {
      if (!biblioteca.loaded && !biblioteca.loading) {
        console.log("📀 Iniciando carga de biblioteca...");
        const escaneoCompleto = await biblioteca.load();
        console.log(`📀 Biblioteca cargada: ${biblioteca.songs.length} canciones`);

        // Renovar la caché (y su timestamp) SOLO cuando hubo un escaneo
        // real. Si load() usó la caché fresca no se toca el timestamp:
        // así la frescura vence a las 24h y aparecen canciones nuevas.
        if (escaneoCompleto) {
          await guardarCache(biblioteca.songs);
          console.log("✅ Caché local guardado");
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
      pauseListener = await App.addListener("pause", () => {
        // guardarEstado reproductor
        if (player.currentSong) {
          guardarEstadoReproductor(player.currentSong?.id, player.currentTime, player.mode);
        }
      });
    }
  });

  onDestroy(async () => {
    if (backListener) {
      const handler = backListener;
      handler.remove();
    }
    if (pauseListener) {
      const handler = pauseListener;
      handler.remove();
    }
  });
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>
<div class="h-dvh flex flex-col justify-between border-r-0 overflow-hidden">
  {@render children()}
  <footer>
    <Player />
  </footer>
</div>
<Dialog/>
