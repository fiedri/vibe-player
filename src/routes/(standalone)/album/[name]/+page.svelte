<script lang="ts">
  import { ArrowLeft } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import { PlayFilledAlt, Shuffle } from "carbon-icons-svelte";
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { playerService } from "$lib/services/player/PlayerFacade";
  import { formatearDuracionTotal } from "$lib/utils.js";
  import { Capacitor } from "@capacitor/core";
  import { ContextType } from "$lib/services/player/types";
  import { page } from "$app/state";
  import { albumes } from "$lib/stores/albumes.svelte";

  let albumName = $derived(page.params.name ?? "");
  let albumData = $derived(albumes.getAlbumInfo(albumName));
  let songs = $derived(albumData.songs);
  let totalDuration = $derived(formatearDuracionTotal(songs));
  let albumImage = $derived(albumData.albums[0]?.image);

  let tight = $state(false);
  let scrollTop = $state(0);

  function goBack(e: MouseEvent) {
    if (window.history.length > 1) {
      e.preventDefault();
      window.history.back();
    }
  }

  function handleScroll(e: Event) {
    const target = e.currentTarget as HTMLElement;
    scrollTop = target.scrollTop;
    tight = scrollTop > 120;
  }

  function handlePlay() {
    if (songs.length === 0) return;
    playerService.setContext(ContextType.InPlaylist, songs);
    playerService.setSong(songs[0]);
  }

  function handleShuffled() {
    if (songs.length === 0) return;
    const songShuffled = playerService.shuffle(songs);
    playerService.setContext(ContextType.InPlaylist, songShuffled);
    playerService.setSong(playerService.queue[0]);
  }

  const FADE_RANGE = 200;
  let fade = $derived(Math.min(scrollTop / FADE_RANGE, 1));
</script>

<section
  class="h-dvh w-screen overflow-y-auto overscroll-y-contain {songs.length > 10
    ? ''
    : 'pb-10'}"
  id="album-view"
  onscroll={handleScroll}
>
  <div class="top-0 z-10 py-1 absolute">
    <Button
      variant="ghost"
      class="px-4 transition-all {tight ? 'bg-background' : 'bg-transparent'}"
      onclick={goBack}
    >
      <ArrowLeft class="size-6" />
    </Button>
  </div>

  <div
    class="album-card w-full flex items-end"
    style:background-image="linear-gradient(to bottom, rgba(0,0,0,0.2) 20%,
    rgba(0,0,0,0.5) 85%,rgba(0,0,0,0.9) 100%), url('{albumImage
      ? Capacitor.convertFileSrc(albumImage)
      : '/default-cover.png'}')"
    style:opacity={1 - fade}
  >
    <div class="w-[80%] mb-3 ml-2.5 flex flex-col gap-1">
      <span class="text-primary uppercase font-bold text-sm">Álbumes</span>
      <h2 class="text-4xl font-extrabold truncate">{albumName}</h2>
      <div class="flex items-center gap-3 *:uppercase text-muted-foreground font-bold">
        <span>{songs.length} canciones</span>
        <span>•</span>
        <span>{totalDuration}</span>
      </div>
    </div>
  </div>

  {#if songs.length > 0}
    <div class="flex gap-3 px-4 pt-5 pb-2 sticky top-5 bg-background">
      <Button
        class="h-12 flex-1 bg-white text-background font-semibold active:scale-95 transition-transform"
        onclick={handlePlay}
      >
        <PlayFilledAlt />
        Reproducir
      </Button>
      <Button
        variant="outline"
        class="h-12 flex-1 border-2 border-border font-semibold active:scale-95 transition-transform !bg-background"
        onclick={handleShuffled}
      >
        <Shuffle />
        Aleatorio
      </Button>
    </div>
    <div>
      {#each songs as song, idx (song.id)}
        <SongCard
          {song}
          {idx}
          context={ContextType.InPlaylist}
          contextSongs={songs}
          playlistId={undefined}
        />
      {/each}
    </div>
  {:else}
    <p class="text-center text-muted-foreground text-sm py-10">
      Este álbum no tiene canciones
    </p>
  {/if}
</section>

<style>
  .album-card {
    height: 250px;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }
</style>
