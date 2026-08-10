<script>
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import VirtualGrid from "$lib/components/ui/VirtualGrid.svelte";
  import { albumes } from "$lib/stores/albumes.svelte";
  import { Capacitor } from "@capacitor/core";
    import { ui } from "$lib/stores/ui.svelte";
    import { onMount } from "svelte";

  onMount(()=>{
ui.query = ""
  })
  let searchQuery = $derived(ui.query);

  let filteredAlbums = $derived(
    searchQuery ? albumes.search(searchQuery) : albumes.albums,
  );
</script>

<div class="h-full w-full p-1">
  <VirtualGrid items={filteredAlbums} columns={3} rowHeight={170} gap={3} overscan={3}>
    {#snippet children(album)}

      <a href="album/{album.name}">
        <ThumbnailCard
          width="w-28"
          height="h-28"
          title={album.name}
          img={Capacitor.convertFileSrc(album.image)}
          subtitle={album.artist}
        />
      </a>
    {/snippet}
  </VirtualGrid>
</div>
