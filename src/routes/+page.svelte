<script lang="ts">
      import VirtualList from "$lib/components/ui/virtualList.svelte";
      import SongCard from "$lib/components/ui/Cards/SongCard.svelte";

      // Generamos 2.000 canciones de prueba
      const mockSongs = Array.from({ length: 2000 }, (_, i) => ({
        id: `${i}`,
        title: `Canción Virtual #${i + 1}`,
        artists: `Artista #${(i % 10) + 1}`,
        album: "Álbum de Prueba",
        duration: 180,
        path: `/test/${i}.mp3`,
        audioUrl: ""
      }));
    </script>

    <div class="biblioteca h-full w-full flex flex-col min-h-0">
      <VirtualList items={mockSongs} estimateSize={52}>
        {#snippet children(song, idx)}
          <SongCard {song} {idx} />
        {/snippet}
      </VirtualList>
    </div>
