<script>
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import VirtualGrid from "$lib/components/ui/VirtualGrid.svelte";
  import { artists } from "$lib/stores/artist.svelte";
  import { Capacitor } from "@capacitor/core";
</script>

<div class="h-full w-full p-1">
  <VirtualGrid items={artists.artists} columns={3} rowHeight={170} gap={3} overscan={3}>
    {#snippet children(artist)}
      <ThumbnailCard
        width="w-28"
        height="h-28"
        title={artist.name}
        img={Capacitor.convertFileSrc(artist.image)}
        subtitle={`${artist.songCount} canciones`}
      />
    {/snippet}
  </VirtualGrid>
</div>
