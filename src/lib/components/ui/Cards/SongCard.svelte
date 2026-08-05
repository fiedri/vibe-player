<script lang="ts">
  import { portal } from "$lib/uiUtils";
  import { ContextType, player } from "../player/playerStore.svelte";
  import {
    OverflowMenuVertical as EllipsisVertical,
    PlayFilledAlt as Play,
    PauseFilled as Pause,
  } from "carbon-icons-svelte";
  import type { Song } from "$lib/types/songs";
  import { ui, DialogType } from "$lib/stores/ui.svelte";
  import { slide } from "svelte/transition";
    import Button from "../button/button.svelte";
import { formatearMS } from "$lib/utils";
    import { playlistStore } from "$lib/stores/playlist.svelte";
  interface Props {
    song: Song;
    idx: number;
    context?: ContextType;
    contextSongs?: Song[];
    playlistId: number | undefined;
    onDelete?: (songId: string) => void;
  }

  let {
    song,
    idx,
    context = ContextType.InBiblioteca,
    contextSongs = [],
    playlistId = undefined,
    onDelete = ()=>{}
  }: Props = $props();

  let isCurrent = $derived(
    player.currentSong?.id === song.id &&
      player.currentSong?.album === song.album,
  );
  let isPlayingThis = $derived(isCurrent && player.isPlaying);

  function handlePlay() {
    player.setContext(context, contextSongs);
    if (isCurrent) {
      player.togglePlay();
    } else {
    
      player.setSong(song);
    }
  }
  function handleRemove(playlistId: number, songid: string){
  playlistStore.removeSong(playlistId, songid)
  }

  let openMenu = $state(false);
</script>

<div
  onclick={handlePlay}
  onkeydown={(e) => (e.key === "Enter" || e.key === " ") && handlePlay()}
  role="button"
  tabindex="0"
  class="flex items-center justify-between p-2 hover:bg-zinc-800/50 group text-sm cursor-pointer select-none {isPlayingThis
    ? 'bg-card'
    : ''} transition-all duration-200 ease-in-out gap-2"
>
  <div class="flex items-center gap-4 flex-1 min-w-0 pointer-events-none">
    <button
      onclick={(e) => {
        e.stopPropagation();
        handlePlay();
      }}
      aria-label={isPlayingThis ? "Pausar" : "Reproducir"}
      class="w-6 text-center text-muted-foreground cursor-pointer flex justify-center items-center shrink-0 pointer-events-auto"
    >
      {#if isPlayingThis}
        <Pause class="size-4 text-primary fill-current" />
      {:else}
        <Play class="size-4 text-white fill-current" />
      {/if}
    </button>

    <div class="min-w-0 flex-1">
      <p class="font-medium hover:underline text-white truncate">
        {song.title}
      </p>
      <p class="text-xs text-muted-foreground hover:underline truncate">
        {song.artists}
      </p>
    </div>
  </div>

  <div class="relative flex gap-2 items-center justify-center shrink-0">
    <span class="text-muted-foreground">{typeof song.duration == "number"? formatearMS(song.duration) : "00:00"}</span>
    <button
      onclick={(e) => {
        e.stopPropagation();
        openMenu = !openMenu;
      }}
      aria-label="Opciones de canción"
      class="p-1 hover:text-foreground"
    >
      <EllipsisVertical class="size-6" />
    </button>
  </div>
</div>

{#if openMenu}
  <button
    use:portal
    type="button"
    class="fixed inset-0 z-10 h-full w-full border-none cursor-default bg-black/20"
    onclick={() => (openMenu = false)}
    aria-label="Cerrar menu"
  ></button>
  <div
    use:portal
    transition:slide
    class="fixed bottom-0 right-0 left-0 z-50 min-h-[30%] border-t border-border bg-popover text-popover-foreground pb-[env(safe-area-inset-bottom)] shadow-xl"
  >
  <Button
      class="w-full justify-start border-b border-border px-4 py-4 text-sm active:bg-primary active:text-primary-foreground"
      onclick={(e) => {
        e.stopPropagation();
       ui.openDialog(DialogType.Playlist, song.id) 
        openMenu = false;
      }}
      variant="ghost"
    >
      Agregar a playlists
    </Button>
   {#if context === ContextType.InPlaylist && playlistId}
    
<Button
      class="w-full justify-start border-b border-border px-4 py-4 text-sm active:bg-primary active:text-primary-foreground"
      onclick={(e) => {
        e.stopPropagation();
        openMenu = false;
        handleRemove(playlistId, song.id);
        onDelete(song.id)
      }}
      variant="ghost"
    >
      Quitar de playlists</Button>

  <!-- {:else} se muestran el submenu en canciones-->

   {/if}
  </div>
{/if}
