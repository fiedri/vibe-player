<script>
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import VirtualGrid from "$lib/components/ui/VirtualGrid.svelte";
    import { albumes } from "$lib/stores/albumes.svelte";
  import { artists } from "$lib/stores/artist.svelte";
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
  <VirtualGrid items={filteredArtist} columns={3} rowHeight={170} gap={3} overscan={3}>
    {#snippet children(artist)}
    <a href="artist/{artist.name}">
 <ThumbnailCard
        width="w-28"
        height="h-28"
        title={artist.name}
        img={Capacitor.convertFileSrc(artist.image)}
        subtitle={`${artist.songCount} canciones`}
        onErrorImg={'/default-artist.png'}

      />

    </a> 
    {/snippet}
  </VirtualGrid>
</div>
