<script lang="ts">
  import { ArrowLeft } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import { PlayFilledAlt, Shuffle } from "carbon-icons-svelte";
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { player } from "$lib/components/ui/player/playerStore.svelte.js";
  import { formatearDuracionTotal } from "$lib/utils.js";
  import { Capacitor } from "@capacitor/core";
  import { ContextType } from "$lib/components/ui/player/playerStore.svelte.js";
  function goBack(e: MouseEvent) {
    if (window.history.length > 1) {
      e.preventDefault();
      window.history.back();
    }
  }

  let { data } = $props();
  let playlists = $derived(data.name);

  let songs = $derived(data.songs);
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
    player.setContext(ContextType.InPlaylist, songs);
    player.setSong(songs[0]);
  }

  function handleShuffled() {
    if (songs.length === 0) return;
    const songShuffled = player.shuffle(songs);
    player.setContext(ContextType.InPlaylist, songShuffled);
    player.setSong(player.queue[0]);
  }
  function getRandomIndex(): number {
    return Math.floor(Math.random() * (songs.length - 1 - 0 + 1)) + 0;
  }

  function onDelete(songId: string) {
    songs = songs.filter((e) => e.id !== songId);
  }

  // Ejemplo: Generar un entero entre 1 y 10
  const randomImg = $derived(
    songs.length > 0 ? songs[getRandomIndex()]?.image : undefined,
  );
  const FADE_RANGE = 200;
  let fade = $derived(Math.min(scrollTop / FADE_RANGE, 1));
</script>

<section
  class="h-dvh w-screen overflow-y-auto overscroll-y-contain {songs.length > 10
    ? ''
    : 'pb-10'}"
  id="playlists-view"
  onscroll={handleScroll}
>
  <div class="sticky top-0 z-10 bg-background px-4 py-1">
    <Button variant="ghost" class="p-2" onclick={goBack}>
      <ArrowLeft class="size-8" />
    </Button>
  </div>

  <div class="">
    <picture class="block px-4" style:opacity={1 - fade}>
      <img
        src={randomImg
          ? Capacitor.convertFileSrc(randomImg)
          : "/default-cover.png"}
        alt="Playlists Img"
        class="h-36 w-full object-cover border-2 border-border"
        loading="lazy"
        onerror={(e) => {
              const target = e.target as HTMLImageElement;
              if (
                target.src !==
                window.location.origin + "/default-cover.png"
              ) {
                target.src = "/default-cover.png";
              }
            }}
      />
    </picture>

    <div class="mt-3 flex flex-col gap-3 px-4" style:opacity={1 - fade}>
      <span class="text-blue-500 uppercase font-bold text-sm">Playlists</span>
      <h2 class="text-4xl font-extrabold">{playlists}</h2>
      <div
        class="flex items-center gap-3 *:uppercase text-muted-foreground font-bold"
      >
        <span>{songs.length} canciones</span>
        <span>•</span>
        <span>{totalDuration}</span>
      </div>
    </div>


    {#if songs.length > 0}
        <div
      class="transition-all duration-500 sticky top-12 py-2 z-10 bg-background px-2 flex flex-row justify-center items-center"
    >
      <Button
        class="bg-white text-background w-[calc(50%-6px)] text-xs"
        onclick={handlePlay}
      >
        <PlayFilledAlt />
        Reproducir
      </Button>
      <Button
        variant="outline"
        class="w-[calc(50%-6px)] border-2 border-border text-xs"
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
            playlistId={data.id}
            {onDelete}
          />
        {/each}
      </div>
    {:else}
      <p class="text-center text-muted-foreground text-sm py-10">
        Esta playlist no tiene canciones
      </p>
    {/if}
  </div>
</section>
