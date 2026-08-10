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
  let artistName = $derived(page.params.name ?? "");
  let artistInfo = artists.artists.filter((el) => el.name === artistName)[0];
  let artistImg = Capacitor.convertFileSrc(artistInfo.image);
  let allArtistsResources = $derived(artists.getAllArtistInfo(artistName));
  function goBack(e: MouseEvent) {
    if (window.history.length > 1) {
      e.preventDefault();
      window.history.back();
    }
  }
  $inspect(allArtistsResources);
    let scrollTop = $state(0);
let tight = $state(false);
  function handleScroll(e: Event) {
    const target = e.currentTarget as HTMLElement;
    scrollTop = target.scrollTop;
    tight = scrollTop > 120;
  }
    const FADE_RANGE = 200;
  let fade = $derived(Math.min(scrollTop / FADE_RANGE, 1));
</script>

<section class="overflow-y-auto overscroll-y-contain"
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
    class="album-card w-full flex  items-end"
    style:background-image="linear-gradient(to bottom, rgba(0,0,0,0.2) 20%,
    rgba(0,0,0,0.5) 85%,rgba(0,0,0,0.9) 100%), url('{artistImg}')"
    style:opacity={1 - fade}
  >
    
    <h2 class="text-4xl font-bold w-[80%] mb-3 ml-2.5">{artistName}</h2>
  </div>
  <div>
    <div class="">
      <div class="p-4">
        <span class="uppercase font-bold text-xl">Albumes</span>
        <div>
         <HorizontalContainer>
          {#each allArtistsResources.albums as album}
           
              <ThumbnailCard
                width="w-28"
                height="h-28"
                title={album.name}
                img={Capacitor.convertFileSrc(album.image)}
              />
          {:else}
            <p>Sin albumes disponibles.</p>
          {/each}
         </HorizontalContainer>
        </div>
      </div>

      <div>
        <span class="uppercase font-bold text-xl p-4">Canciones</span>
        {#each allArtistsResources.songs as song, idx}
         <SongCard
           {song}
           {idx}
           context={ContextType.InPlaylist}
           contextSongs={allArtistsResources.songs}
           playlistId={undefined}
         />
        {/each}
      </div>
    </div>
  </div>
</section>

<style>
  .album-card {
    height: 250px;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
  }
</style>
