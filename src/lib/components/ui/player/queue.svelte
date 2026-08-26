<script lang="ts">
  import { expand } from "$lib/uiUtils";
  import VirtualList from "../virtualList.svelte";
  import SongCard from "../Cards/SongCard.svelte";
  import { playerService } from "$lib/services/player/PlayerFacade";
  import { fade } from "svelte/transition";

  let heigthContainer = $state(0);
  let isAnimating = $state(false);
  let tight = $derived(heigthContainer > (window.innerHeight * 40) / 100);

  function handleAnimationStart() {
    isAnimating = true;
  }

  function handleAnimationEnd() {
    // Da un pequeño margen (100ms) tras la animación para digerir eventos fantasma
    setTimeout(() => {
      isAnimating = false;
    }, 100);
  }
</script>

<div
  use:expand={{ onStart: handleAnimationStart, onEnd: handleAnimationEnd }}
  class="flex flex-col bg-popover py-2 fixed bottom-0 w-full right-0 left-0 no-copy touch-none"
  bind:offsetHeight={heigthContainer}
>
  <div data-drag-handle class="cursor-pointer select-none py-1">
    <div class="w-20 h-1.5 bg-primary mx-auto mb-2 rounded-full"></div>
    <h2 class="text-center uppercase font-bold leading-5 p-2 text-muted-foreground text-xs tracking-wider">
      Cola de Reproducción
    </h2>
  </div>

  {#if tight}
    <div 
      class="flex-1 min-h-0 w-full border-border border-t {isAnimating ? 'pointer-events-none' : ''}" 
      transition:fade={{ duration: 150 }}
    >
      <VirtualList items={playerService.queue} itemHeight={54}>
        {#snippet children(song, idx)}
          <SongCard {song} {idx} playlistId={undefined} />
        {/snippet}
      </VirtualList>
    </div>
  {/if}
</div>
