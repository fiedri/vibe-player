<script>
import {m} from '$lib/paraglide/messages.js';
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import VirtualGrid from "$lib/components/ui/VirtualGrid.svelte";
  import LoadingScreen from "$lib/components/ui/LoadingScreen.svelte";
    import { albumes } from "$lib/stores/albumes.svelte";
  import { artists } from "$lib/stores/artist.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { Capacitor } from "@capacitor/core";
    import { ui } from "$lib/stores/ui.svelte";
    import { onMount } from "svelte";

  onMount(()=>{
ui.query = ""
  })
  let searchQuery = $derived(ui.query);

  let filteredArtist = $derived(
    searchQuery ? artists.search(searchQuery) : artists.artists,
  );
</script>

<div class="h-full w-full p-1">
  {#if !artists.loaded && artists.artists.length === 0}
    <LoadingScreen text={m["biblioteca.loading_artists"]()} />
  {:else if biblioteca.error && artists.artists.length === 0}
    <div class="error p-4 text-center">
      <p>❌{biblioteca.error}</p>
      <button
        onclick={() => biblioteca.refresh()}
        class="mt-2 text-primary font-medium"
      >
        {m["biblioteca.retry"]()}
      </button>
    </div>
  {:else}
  <VirtualGrid items={filteredArtist} columns={3} rowHeight={170} gap={3} overscan={3}>
    {#snippet children(artist)}
    <a href="artist/{artist.name}">
 <ThumbnailCard
        width="w-28"
        height="h-28"
        title={artist.name}
        img={Capacitor.convertFileSrc(artist.image)}
        subtitle={m.songs({ count: artist.songCount })}
        onErrorImg={'/default-artist.png'}

      />
    </a> 
    {/snippet}
  </VirtualGrid>
  {/if}
</div>
