<script lang="ts">
  import "./layout.css";
  import Player from "$lib/components/ui/player/player.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { solicitarPermisosAudio } from "$lib/services/files";
  import { guardarCache, guardarEstadoReproductor } from "$lib/services/stores";
  import { DialogType, ui } from "$lib/stores/ui.svelte";
  import { onMount, onDestroy } from "svelte";
  import { App } from "@capacitor/app";
  import { playerService } from "$lib/services/player/PlayerFacade";
  import { Capacitor } from "@capacitor/core";
  import Dialog from "$lib/components/ui/dialogs/dialog.svelte";
  import UnImplementedDialog from "$lib/components/ui/dialogs/unImplementedDialog.svelte";
  import CreatePlaylistsDialog from "$lib/components/ui/dialogs/createPlaylistsDialog.svelte";
  import PlaylistSelection from "$lib/components/ui/dialogs/playlistSelection.svelte";
  import BackupPlaylistDialog from "$lib/components/ui/dialogs/backupPlaylistDialog.svelte";
  import ErrorDialog from "$lib/components/ui/dialogs/errorDialog.svelte";
  import ConfirmDeleteDialog from "$lib/components/ui/dialogs/confirmDeleteDialog.svelte";
  import SelectionMode from "$lib/components/multiSelector/selectionMode.svelte";
  import { selection } from "$lib/components/multiSelector/selectionStore.svelte";
  import { favorites } from "$lib/stores/favorites.svelte";
    import InfoSong from "$lib/components/ui/dialogs/infoSong.svelte";
  let { children } = $props();
  let backListener: any = null;
  let pauseListener: any = null;

  onMount(async () => {
    if (Capacitor.isNativePlatform()) {
      try {
        backListener = await App.addListener("backButton", (event) => {
          console.log("[back] pressed", event);
          if (ui.playerIsOpen) {
            ui.playerIsOpen = false;
            return;
          }
          if (selection.isActive) {
            selection.clear();
            return;
          }

          if (event.canGoBack) {
            window.history.back();
          } else {
            App.exitApp();
          }
        });
      } catch (err) {
        console.error("[back] fallo al registrar listener:", err);
      }

      try {
        pauseListener = await App.addListener("pause", () => {
          // guardarEstado reproductor
          if (playerService.currentSong) {
            guardarEstadoReproductor(
              playerService.currentSong?.id,
              playerService.currentTime,
              playerService.mode,
            );
          }
        });
      } catch (err) {
        console.error("[pause] fallo al registrar listener:", err);
      }
    }

    try {
      await playerService.loadLastSavedState();
    } catch (err) {
      console.error("Error al cargar el último estado guardado:", err);
    }
    try {
      await favorites.loadFavoritesInfo();
    } catch (err) {
      console.error("error al cargar favoritos", err);
    }
    try {
      const permissions = (await solicitarPermisosAudio()) as
        | { audio?: string }
        | undefined;
      if (permissions?.audio !== "granted") {
        biblioteca.permissionDenied = true;
      }
    } catch (err) {
      console.error("Error al solicitar permisos de audio:", err);
    }
    setTimeout(async () => {
      if (!biblioteca.loaded && !biblioteca.loading) {
        console.log("📀 Iniciando carga de biblioteca...");
        const escaneoCompleto = await biblioteca.load();
        console.log(
          `📀 Biblioteca cargada: ${biblioteca.songs.length} canciones`,
        );

        // Renovar la caché (y su timestamp) SOLO cuando hubo un escaneo
        // real. Si load() usó la caché fresca no se toca el timestamp:
        // así la frescura vence a las 24h y aparecen canciones nuevas.
        if (escaneoCompleto) {
          await guardarCache(biblioteca.songs);
          console.log("✅ Caché local guardado");
        }
      }
    }, 2000);


    window.addEventListener("unhandledrejection", (event) => {
      console.error("Promesa rechazada no manejada:", event.reason);

      const mensaje = event.reason?.message || "Ocurrió un error inesperado";
      ui.openDialog(DialogType.Error, mensaje as string);
    });
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
  window.addEventListener("popstate", () => {
    if (ui.activeDialog) ui.closeDialog();
  });
</script>

<div class="h-dvh flex flex-col justify-between border-r-0 overflow-hidden">
  {@render children()}
  <footer>
    <Player />
  </footer>
</div>

<Dialog>
  {#snippet children(dialogType: DialogType)}
    {#if dialogType === DialogType.Playlist}
      <PlaylistSelection />
    {:else if dialogType === DialogType.Unimplemented}
      <UnImplementedDialog />
    {:else if dialogType === DialogType.CreatePlaylist}
      <CreatePlaylistsDialog />
    {:else if dialogType === DialogType.Backup}
      <BackupPlaylistDialog />
    {:else if dialogType === DialogType.Error}
      <ErrorDialog />
    {:else if dialogType === DialogType.ConfirmDelete}
      <ConfirmDeleteDialog />
      {:else if dialogType == DialogType.InfoSong}
      <InfoSong/>
    {/if}
  {/snippet}
</Dialog>

<SelectionMode />
