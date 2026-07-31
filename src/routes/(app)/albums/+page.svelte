<script>
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import VirtualGrid from "$lib/components/ui/VirtualGrid.svelte";
  import { albumes } from "$lib/stores/albumes.svelte";
  import { Capacitor } from "@capacitor/core";
</script>

<div class="h-full w-full p-1">
  <VirtualGrid items={albumes.albums} columns={3} rowHeight={170} gap={3} overscan={3}>
    {#snippet children(album)}
    <a href="/albums/[{album.name}]/">

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
