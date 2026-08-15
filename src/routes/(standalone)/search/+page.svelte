<script lang="ts">
  import Button from "$lib/components/ui/button/button.svelte";
  import { Capacitor } from "@capacitor/core";
  import { ArrowLeft } from "carbon-icons-svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { artists } from "$lib/stores/artist.svelte";
  import { albumes } from "$lib/stores/albumes.svelte";
  import { ContextType } from "$lib/services/player/types";
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import HorizontalContainer from "$lib/components/ui/wrapper/horizontalContainer.svelte";
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import type { MediaFile } from "$lib/types/songs";
  function goBack(e: MouseEvent) {
    if (window.history.length > 1) {
      e.preventDefault();
      window.history.back();
    }
  }
  let searchQuery: string = $state("");
  let filteredSongs = $state<MediaFile[]>([]);
  let filteredAlbums = $state<any[]>([]);
  let filteredArtist = $state<any[]>([]);

  $effect(() => {
    const query = searchQuery;

    if (!query) {
      filteredSongs = [];
      filteredArtist = [];
      filteredAlbums = [];
      return;
    }

    const timeout = setTimeout(() => {
      filteredSongs = biblioteca.search(query);
      filteredArtist = artists.search(query);
      filteredAlbums = albumes.search(query);
    }, 500);

    return () => {
      clearTimeout(timeout);
    };
  });
</script>

<section
  class="h-dvh w-screen overflow-y-auto overscroll-y-contain flex flex-col [&_h2]:uppercase [&_h2]:font-black [&_h2]:tracking-wide [&_h2]:text-sm [&_h2]:text-muted-foreground [&_h2]:mb-3"
  id="playlists-view"
>
  <div
    class="w-full flex justify-between px-3 flex-row py-2 shadow-md shadow-primary"
  >
    <Button variant="ghost" class="px-4 transition-all" onclick={goBack}>
      <ArrowLeft class="size-6" />
    </Button>
    <input
      class="appearance-none border-1 bg-input hover:border-border/70 w-[90%] transition-colors py-3 leading-tight focus:outline-none focus:ring-ring focus:border-border focus:shadow-outline border-border"
      type="text"
      placeholder="Search..."
      autofocus
      bind:value={searchQuery}
    />
  </div>
  <div>
    {#if filteredArtist.length > 0}
      <h2 class="px-4 mt-4">Artista</h2>
      <div class="pl-4">
        <HorizontalContainer>
          {#each filteredArtist as artist}
            <a href="/artist/{artist.name}">
              <ThumbnailCard
                img={Capacitor.convertFileSrc(artist.image)}
                title={artist.name}
                height={"h-28"}
                width={"w-28"}
              />
            </a>
          {/each}
        </HorizontalContainer>
      </div>
    {/if}
    {#if filteredAlbums.length > 0}
      <h2 class="px-4 mt-4">Album</h2>
      <div class="pl-4">
        <HorizontalContainer>
          {#each filteredAlbums as album}
            <a href="/album/{album.title}">
              <ThumbnailCard
                img={Capacitor.convertFileSrc(album.image)}
                title={album.title}
                height={"h-28"}
                width={"w-28"}
              />
            </a>
          {/each}
        </HorizontalContainer>
      </div>
    {/if}
    {#if filteredSongs.length > 0}
      <h2 class="px-4 mt-4">Canciones</h2>
      {#each filteredSongs as song, idx (song.id)}
        <SongCard
          {song}
          {idx}
          context={ContextType.InPlaylist}
          contextSongs={filteredSongs}
          playlistId={undefined}
        />
      {/each}
    {/if}
    {#if searchQuery && filteredSongs.length == 0 && filteredArtist.length == 0 && filteredAlbums.length == 0}
      <p class="italic text-center mt-20">No se encontraron resultados</p>
    {/if}
  </div>
</section>
