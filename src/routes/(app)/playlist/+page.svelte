<script>
  import {
    Playlist,
    AddLarge,
    OverflowMenuVertical,
  } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import { ui } from "$lib/stores/ui.svelte";
  let mockAlbums = Array.from({ length: 100 }, (_, i) => ({
    title: "Ejemplo",
    portada: "/default-cover.png",
    numbersSongs: 20,
  }));
</script>

<div
  class=" h-full w-full min-w-0 overflow-x-hidden flex flex-col gap-15 min-h-0 overflow-y-auto"
>
  <div>
   <div class="animate_slideUp absolute bottom-25 right-3  z-0">
<Button class="rounded-full size-15 " onclick={() => ui.handleDialog()}
      ><AddLarge /></Button
    >
   </div> 
    {#each mockAlbums as playlists}
      <div class="h-14 p-2 flex flex-row justify-between items-center">
        <div class="flex flex-row gap-3 items-center w-[50%]">
          <Playlist size={40} />
          <h2 class="font-medium hover:underline text-white truncate">
            {playlists.title}
          </h2>
        </div>
        <div class="flex flex-row gap-3 items-center">
          <span class="text-xs text-muted-foreground hover:underline truncate"
            >{playlists.numbersSongs} songs</span
          >
          <OverflowMenuVertical size={28} />
        </div>
      </div>
    {/each}
  </div>
</div>

<style>

</style>
