<script lang="ts">
  import { player, type Song } from "../player/player.svelte.ts";
  import { Play, Pause, EllipsisVertical } from "@lucide/svelte";

  interface Props {
    song: Song;
    idx: number;
  }

  let { song, idx }: Props = $props();

  let isCurrent = $derived(
    player.currentSong?.id === song.id &&
      player.currentSong?.albumId == song.albumId,
  );
  let isPlayingThis = $derived(isCurrent && player.isPlaying);

  function handlePlay() {
    if (isCurrent) {
      player.togglePlay();
    } else {
      player.setSong(song);
    }
  }

  function handleContainerClick() {
    if (window.innerWidth < 768) {
      handlePlay();
    }
  }
</script>

<div
  onclick={handleContainerClick}
  class="flex items-center justify-between p-2 hover:bg-zinc-800/50 group text-sm cursor-pointer md:cursor-default select-none {isPlayingThis ? 'bg-card' : ''} transition-all duration-300 ease-in-out gap-2"
>
  <!-- Agregamos flex-1 y min-w-0 aquí -->
  <div
    class="flex items-center gap-4 pointer-events-none md:pointer-events-auto flex-1 min-w-0"
  >
    <button
      onclick={(e) => {
        e.stopPropagation();
        handlePlay();
      }}
      class="w-6 text-center text-muted-foreground cursor-pointer flex justify-center items-center shrink-0"
    >
      {#if isPlayingThis}
        <Pause class="size-4 text-primary fill-current" />
      {:else}
        <Play class="size-4 text-white fill-current" />
      {/if}
    </button>

    <!-- Agregamos min-w-0 al contenedor de los textos -->
    <div class="min-w-0 flex-1">
      <p
        class="font-medium md:hover:underline cursor-pointer text-white truncate"
      >
        {song.title}
      </p>
      <p class="text-xs text-muted-foreground md:hover:underline cursor-pointer truncate">
        {song.artists}
      </p>
    </div>
  </div>

  <!-- Agregamos shrink-0 para que la duración y opciones no se aplasten -->
  <div class="flex gap-2 items-center justify-center shrink-0">
    <span class="text-muted-foreground">{song.duration}</span>
    <button aria-label="Song menu">
      <EllipsisVertical />
    </button>
  </div>
</div>
