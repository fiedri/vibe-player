<script>
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  // Si quieres un estado local de búsqueda (también con runes)
  //let searchQuery = $state('');
  
  let filteredSongs = biblioteca.songs

</script>

<div class="biblioteca">
  {#if biblioteca.loading}
    <div class="loading">
      <p>Escaneando MediaStore...</p>
      <!-- Podrías poner un spinner aquí -->
    </div>

  {:else if biblioteca.error}
    <div class="error">
      <p>❌ {biblioteca.error}</p>
      <button onclick={() => biblioteca.refresh()}>
        Reintentar
      </button>
    </div>

  {:else}
    <div class="song-list" id="song-list">
      {#each filteredSongs as song, idx (song.id)}
        <SongCard {song} {idx} />
      {:else}
        {#if searchQuery}
          <p>No se encontraron resultados para "{searchQuery}"</p>
        {:else}
          <p>No hay canciones en el dispositivo</p>
        {/if}
      {/each}
    </div>
  {/if}
</div>
