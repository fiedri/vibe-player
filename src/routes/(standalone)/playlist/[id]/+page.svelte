<script lang="ts">
  import { ArrowLeft } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import {OverflowMenuVertical, PlayFilledAlt, Shuffle } from "carbon-icons-svelte";
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { playerService } from "$lib/services/player/PlayerFacade";
  import { formatearDuracionTotal } from "$lib/utils.js";
  import { Capacitor } from "@capacitor/core";
  import { ContextType } from "$lib/services/player/types";
  import { page } from "$app/stores";
  import { playlistStore } from "$lib/stores/playlist.svelte";
  import { onMount } from "svelte";
    import PlaylistsMenu from "$lib/components/ui/menus/playlistsMenu.svelte";



  let playlistId = $derived.by(() => {
    const match = $page.url.pathname.match(/^\/playlist\/(\d+)$/);
    return match ? Number(match[1]) : null;
  });

  onMount(() => {
    if (playlistId !== null) playlistStore.loadPlaylistSongs(playlistId);
  });

  let songs = $derived.by(() =>
    playlistStore.currentPlaylistId === playlistId
      ? playlistStore.currentPlaylistSongs
      : [],
  );
  let playlists = $derived.by(
    () =>
      playlistStore.playlists.find((e) => e.id === playlistId)?.name ?? "",
  );
  let totalDuration = $derived(formatearDuracionTotal(songs));
  let tight = $state(false);

  let scrollTop = $state(0);

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
  function getRandomIndex(): number {
    return Math.floor(Math.random() * (songs.length - 1 - 0 + 1)) + 0;
  }

  // Ejemplo: Generar un entero entre 1 y 10
  const randomImg = $derived(
    songs.length > 0 ? songs[getRandomIndex()]?.image : undefined,
  );
  const FADE_RANGE = 200;
  let fade = $derived(Math.min(scrollTop / FADE_RANGE, 1));
  let isOpenMenu = $state(false)
</script>

<section
  class="h-dvh w-screen overflow-y-auto overscroll-y-contain {songs.length > 10
    ? ''
    : 'pb-10'}"
  id="playlists-view"
  onscroll={handleScroll}
>
  <div class="top-0 z-10 absolute w-full flex justify-between flex-row {tight? 'bg-background' : 'bg-transparent'}">
    <Button
      variant="ghost"
      class="px-4 transition-all"
     href="/playlist" 
    >
      <ArrowLeft class="size-6" />
    </Button>

    <Button
      variant="ghost"
      class="px-4 transition-all"
      onclick={()=> isOpenMenu = !isOpenMenu}
    >
      <OverflowMenuVertical class="size-6" />
    </Button>
    {#if isOpenMenu}
      
    <PlaylistsMenu playlistId={playlistId} onClose={()=>isOpenMenu=false}/>
    {/if}
  </div>

  <div
    class="album-card w-full flex items-end"
    style:background-image="linear-gradient(to bottom, rgba(0,0,0,0.2) 20%,
    rgba(0,0,0,0.5) 85%,rgba(0,0,0,0.9) 100%), url('{randomImg
      ? Capacitor.convertFileSrc(randomImg)
      : '/default-cover.png'}')"
    style:opacity={1 - fade}
  >
    <div class="w-[80%] mb-3 ml-2.5 flex flex-col gap-1">
      <span class="text-primary uppercase font-bold text-sm">Playlists</span>
      <h2 class="text-4xl font-extrabold">{playlists}</h2>
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
        class="h-12 flex-1 border-2 border-border font-semibold active:scale-95 transition-transform"
        onclick={handleShuffled}
      >
        <Shuffle />
        Aleatorio
      </Button>
    </div>
    <div>
      {#each songs as song, idx}
        <SongCard
          {song}
          {idx}
          context={ContextType.InPlaylist}
          contextSongs={songs}
          playlistId={playlistId ?? undefined}
        />
      {/each}
    </div>
  {:else}
    <p class="text-center text-muted-foreground text-sm py-10">
      Esta playlist no tiene canciones
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
