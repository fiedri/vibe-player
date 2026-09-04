<script>
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import VirtualList from "$lib/components/ui/virtualList.svelte";
  import { ui } from "$lib/stores/ui.svelte";
    import { onMount, onDestroy } from "svelte";
    import { selection } from "$lib/components/multiSelector/selectionStore.svelte";
    import Button from "$lib/components/ui/button/button.svelte";
    import { Shuffle } from "carbon-icons-svelte";
import { playerService } from "$lib/services/player/PlayerFacade";
import { ContextType } from "$lib/services/player/types";
import { m } from "$lib/paraglide/messages.js";
  onMount(()=>{
ui.query = ""
  })
  onDestroy(()=>{
selection.clear()
  })
  $effect(()=>{
selection.avaiblesIds = biblioteca.songs.map(el => el.id)
  })

function handleShuffled() {
    if (biblioteca.songs.length === 0) return;
    const songShuffled = playerService.shuffle(biblioteca.songs);
    playerService.setContext(ContextType.InPlaylist, songShuffled);
    playerService.setSong(playerService.queue[0]);
  }
</script>
 
<div class="biblioteca h-full w-full">
  {#if biblioteca.loading && biblioteca.songs.length === 0}
    <div class="loading p-4 text-center">
      <p>{m["biblioteca.scanning"]()}</p>
    </div>
  {:else if biblioteca.error && biblioteca.songs.length === 0}
    <div class="error p-4 text-center">
      {#if biblioteca.permissionDenied}
      <p>
❌ {m["biblioteca.permission_denied"]()}
      </p>
      {:else}
      <p>❌{biblioteca.error}</p>
      {/if}
      <button
        onclick={() => biblioteca.refresh()}
        class="mt-2 text-primary font-medium"
      >
        {m["biblioteca.retry"]()}
      </button>
    </div>

  {:else}
    <VirtualList items={biblioteca.songs} itemHeight={52}>
      {#snippet children(song, idx)}
        <SongCard {song} {idx} playlistId={undefined}/>
      {/snippet}
    </VirtualList>
  {/if}
</div>
<div class="absolute right-3 bottom-25 z-10">

<Button class="aspect-square size-12 animate_slideUp" onclick={handleShuffled}><Shuffle/></Button>
</div>
