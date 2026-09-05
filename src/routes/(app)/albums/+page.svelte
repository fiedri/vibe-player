<script>
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import VirtualGrid from "$lib/components/ui/VirtualGrid.svelte";
  import LoadingScreen from "$lib/components/ui/LoadingScreen.svelte";
  import { albumes } from "$lib/stores/albumes.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { Capacitor } from "@capacitor/core";
    import { ui } from "$lib/stores/ui.svelte";
    import { onMount } from "svelte";
  import { m } from "$lib/paraglide/messages.js";

  onMount(()=>{
ui.query = ""
  })
  let searchQuery = $derived(ui.query);

  let filteredAlbums = $derived(
    searchQuery ? albumes.search(searchQuery) : albumes.albums,
  );
</script>

<div class="h-full w-full p-1">
  {#if !albumes.loaded && albumes.albums.length === 0}
    <LoadingScreen text={m["biblioteca.loading_albums"]()} />
  {:else if biblioteca.error && albumes.albums.length === 0}
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
  <VirtualGrid items={filteredAlbums} columns={3} rowHeight={170} gap={3} overscan={3}>
    {#snippet children(album)}

      <a href="album/{album.title}">
        <ThumbnailCard
          width="w-28"
          height="h-28"
          title={album.title}
          img={Capacitor.convertFileSrc(album.image)}
          subtitle={album.artist}
                  />
      </a>
    {/snippet}
  </VirtualGrid>
  {/if}
</div>
