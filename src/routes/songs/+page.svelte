<script>
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import VirtualList from "$lib/components/ui/virtualList.svelte";
  //let searchQuery = $state('');
  
 let searchQuery = $state('');

  // Usamos $derived para que reaccione automáticamente 
  // cuando biblioteca.songs cambie de tamaño o el usuario busque algo
  let filteredSongs = $derived(
    searchQuery
      ? biblioteca.search(searchQuery)
      : biblioteca.songs
  );</script>

<div class="biblioteca h-full w-full">
  {#if biblioteca.loading && biblioteca.songs.length === 0}
    <div class="loading p-4 text-center">
      <p>Escaneando MediaStore...</p>
    </div>

  {:else if biblioteca.error && biblioteca.songs.length === 0}
    <div class="error p-4 text-center">
      <p>❌ {biblioteca.error}</p>
      <button onclick={() => biblioteca.refresh()} class="mt-2 text-primary font-medium">
        Reintentar
      </button>
    </div>

  {:else if filteredSongs.length === 0}
    <div class="p-4 text-center text-zinc-400">
      {#if searchQuery}
        <p>No se encontraron resultados para "{searchQuery}"</p>
      {:else}
        <p>No hay canciones en el dispositivo</p>
      {/if}
    </div>

  {:else}
<VirtualList items={filteredSongs} itemHeight={52}>
      {#snippet children(song, idx)}
        <SongCard {song} {idx} />
      {/snippet}
    </VirtualList>
  
  {/if}
</div>
