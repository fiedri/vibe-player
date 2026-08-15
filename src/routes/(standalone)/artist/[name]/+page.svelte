<script lang="ts">
  import { artists } from "$lib/stores/artist.svelte";
  import { Capacitor } from "@capacitor/core";
  import { page } from "$app/state";
  import { ArrowLeft } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import ThumbnailCard from "$lib/components/ui/Cards/thumbnailCard.svelte";
  import HorizontalContainer from "$lib/components/ui/wrapper/horizontalContainer.svelte";
  import SongCard from "$lib/components/ui/Cards/SongCard.svelte";
  import { ContextType } from "$lib/services/player/types";
  import { DEFAULT_COVER } from "$lib/types/songs";
  import type { MediaFile } from "$lib/types/songs";
  import VirtualList from "$lib/components/ui/virtualList.svelte";
  let artistName = $derived(page.params.name ?? "");
  let artistInfo = $derived(
    artists.artists.find((el) => el.name === artistName),
  );
  let artistImg = $derived(
    artistInfo?.image
      ? Capacitor.convertFileSrc(artistInfo.image)
      : DEFAULT_COVER,
  );
  let allArtistsResources = $derived(artists.getAllArtistInfo(artistName));
  function goBack(e: MouseEvent) {
    if (window.history.length > 1) {
      e.preventDefault();
      window.history.back();
    }
  }
  let scrollTop = $state(0);
  let tight = $derived(scrollTop > 120);
  const FADE_RANGE = 200;
  let fade = $derived(Math.min(scrollTop / FADE_RANGE, 1));
  const HEADER_HEIGHT = 250;
  const ALBUMS_BLOCK_HEIGHT = 216;
  const NO_ALBUMS_BLOCK_HEIGHT = 80;
  const SONG_HEIGHT = 56;
  type ArtistListItem =
    | { kind: "albums" }
    | { kind: "song"; song: MediaFile };
  let listItems: ArtistListItem[] = $derived([
    { kind: "albums" },
    ...allArtistsResources.songs.map(
      (song): ArtistListItem => ({ kind: "song", song }),
    ),
  ]);
  function getItemHeight(index: number): number {
    if (index === 0) {
      const block =
        allArtistsResources.albums.length > 0
          ? ALBUMS_BLOCK_HEIGHT
          : NO_ALBUMS_BLOCK_HEIGHT;
      return HEADER_HEIGHT + block;
    }
    return SONG_HEIGHT;
  }
</script>

<section class="h-dvh w-screen relative overflow-hidden">
  <div class="top-0 z-10 py-1 absolute">
    <Button
      variant="ghost"
      class="px-4 transition-all {tight ? 'bg-background' : 'bg-transparent'}"
      onclick={goBack}
    >
      <ArrowLeft class="size-6" />
    </Button>
  </div>
  <VirtualList bind:scrollTop items={listItems} itemHeight={getItemHeight}>
    {#snippet children(entry, idx)}
      {#if entry.kind === "albums"}
        <div class="w-full" style:height="{getItemHeight(0)}px">
          <div
            class="album-card w-full flex items-end"
            style:background-image="linear-gradient(to bottom, rgba(0,0,0,0.2) 20%,
            rgba(0,0,0,0.5) 85%,rgba(0,0,0,0.9) 100%), url('{artistImg}')"
            style:opacity={1 - fade}
          >
            <h2 class="text-4xl font-bold w-[80%] mb-3 ml-2.5">{artistName}</h2>
          </div>
          <div class="p-4">
            <span class="uppercase font-bold text-xl">Albumes</span>
            <div>
              <HorizontalContainer>
                {#each allArtistsResources.albums as album}
                  <a href="/album/{album.title}">
                    <ThumbnailCard
                      width="w-28"
                      height="h-28"
                      title={album.title}
                      img={Capacitor.convertFileSrc(album.image)}
                    />
                  </a>
                {:else}
                  <p>Sin albumes disponibles.</p>
                {/each}
              </HorizontalContainer>
            </div>
          </div>
        </div>
      {:else}
        <SongCard
          song={entry.song}
          idx={idx - 1}
          context={ContextType.InPlaylist}
          contextSongs={allArtistsResources.songs}
          playlistId={undefined}
        />
      {/if}
    {/snippet}
  </VirtualList>
</section>

<style>
  .album-card {
    height: 250px;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }
</style>