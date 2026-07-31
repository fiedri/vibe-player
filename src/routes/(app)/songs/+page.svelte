<script>
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import VirtualList from "$lib/components/ui/virtualList.svelte";
  import { ui } from "$lib/stores/ui.svelte";
    import { onMount } from "svelte";

  onMount(()=>{
ui.query = ""
  })
  let searchQuery = $derived(ui.query);

  let filteredSongs = $derived(
    searchQuery ? biblioteca.search(searchQuery) : biblioteca.songs,
  );
</script>

<div class="biblioteca h-full w-full">
  {#if biblioteca.loading && biblioteca.songs.length === 0}
    <div class="loading p-4 text-center">
      <p>Escaneando Biblioteca...</p>
    </div>
  {:else if biblioteca.error && biblioteca.songs.length === 0}
    <div class="error p-4 text-center">
      <p>❌ {biblioteca.error}</p>
      <button
        onclick={() => biblioteca.refresh()}
        class="mt-2 text-primary font-medium"
      >
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
