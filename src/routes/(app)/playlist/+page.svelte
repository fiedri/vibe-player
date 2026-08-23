<script lang="ts">
  import {
    Playlist,
    AddLarge,
    OverflowMenuVertical,
    Download,
    FavoriteFilled,
  } from "carbon-icons-svelte";
  import Button from "$lib/components/ui/button/button.svelte";
  import { ui, DialogType } from "$lib/stores/ui.svelte";
  import { playlistStore } from "$lib/stores/playlist.svelte";
  import { animateTyping } from "$lib/animations";
  import { goto } from "$app/navigation";
  import { selection } from "$lib/components/multiSelector/selectionStore.svelte";
  import { onDestroy, onMount } from "svelte";
  onMount(() => {
    if (selection.isActive) {
      selection.clear();
    }
  });
  onDestroy(() => {
    if (selection.isActive) {
      selection.clear();
    }
  });
  let activeMenuId = $state<number | null>(null);
  let playlists = $derived(
    playlistStore.playlists.filter((el) => el.name !== "favoritos"),
  );
  let favorito = $derived(
    playlistStore.playlists.find((el) => el.name == "favoritos"),
  );
  function toggleMenu(id: number) {
    activeMenuId = activeMenuId === id ? null : id;
  }
  function gotoPlaylist(id: number) {
    goto(`/playlist/${id}`);
  }
</script>

{#if activeMenuId !== null}
  <button
    type="button"
    class="fixed inset-0 z-10 h-full w-full border-none cursor-default"
    onclick={() => (activeMenuId = null)}
    aria-label="Cerrar menu"
  ></button>
{/if}

<div
  class=" h-full w-full min-w-0 overflow-x-hidden flex flex-col gap-15 min-h-0 overflow-y-auto"
>
  {#if playlistStore.isLoading && playlists.length === 0}
    <div
      role="status"
      class="flex flex-row items-center w-full gap-3 justify-center h-full"
    >
      <div>
        <svg
          aria-hidden="true"
          class="w-8 h-8 text-popover animate-spin fill-primary"
          viewBox="0 0 100 101"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"
            fill="currentColor"
          />
          <path
            d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"
            fill="currentFill"
          />
        </svg>
      </div>
      <span use:animateTyping={"Cargando playlists..."}></span>
    </div>
  {:else}
    <div>
      <a
        href="playlist/{favorito?.id}"
        class="mb-5 h-14 px-2 py-10 flex flex-row justify-between border-primary border items-center"
      >
        <div class="flex flex-row gap-3 items-center w-[50%]">
          <FavoriteFilled size={38} class="text-primary" />
          <h2
            class="font-medium hover:underline text-white underline underline-offset-4 uppercase truncate text-primary"
          >
            Favoritos
          </h2>
        </div>
        <div class="mr-5 relative flex flex-row gap-3 items-center">
          <span class="text-xs text-muted-foreground hover:underline truncate"
            >{favorito?.songsCount} songs</span
          >
        </div>
      </a>
      <h2 class="font-bold text-xl uppercase px-2">Mis Playlist</h2>

      {@render buttonToCreate()}
      {#each playlists as playlist (playlist.id)}
        <!-- svelte-ignore a11y_missing_attribute -->
        <!-- svelte-ignore a11y_click_events_have_key_events -->
        <div
          class="h-14 long-press-item p-2 flex flex-row justify-between items-center {selection.seletedIds.has(
            playlist.id,
          )
            ? 'bg-popover'
            : ''}"
          role="button"
          tabindex="0"
          onclick={() => gotoPlaylist(playlist.id)}
        >
          <div class="flex flex-row gap-3 items-center w-[50%]">
            <Playlist size={40} />
            <h2 class="font-medium hover:underline text-white truncate">
              {playlist.name}
            </h2>
          </div>
          <div class="relative flex flex-row gap-3 items-center">
            <span class="text-xs text-muted-foreground hover:underline truncate"
              >{playlist.songsCount} songs</span
            >

            <button
              onclick={(e) => {
                e.stopPropagation();
                toggleMenu(playlist.id);
              }}
            >
              <OverflowMenuVertical size={28} />
            </button>
            {#if activeMenuId === playlist.id}
              <div
                class="absolute right-0 top-full mt-2 w-30 bg-popover border border-border shadow-lg z-50"
              >
                <Button
                  class="p-2 w-full"
                  onclick={(e) => {
                    e.stopPropagation();
                    playlistStore.delete(playlist.id);
                  }}
                  variant="destructive">Eliminar</Button
                >
              </div>
            {/if}
          </div>
        </div>
      {:else}
        <div class="h-full w-full flex justify-center items-center">
          <p class="text-muted-foreground text-lg italic">
            Aun no hay playlists disponibles...
          </p>
        </div>
      {/each}
    </div>
  {/if}
</div>
{#snippet buttonToCreate()}
  <div class="animate_slideUp absolute bottom-25 right-3 z-10">
    <Button
      class="size-12 aspect-square "
      onclick={() => ui.openDialog(DialogType.CreatePlaylist)}
      ><AddLarge /></Button
    >
  </div>
  <div class="animate_slideUp absolute bottom-25 right-18 z-10">
    <Button
      class="size-12 aspect-square"
      onclick={() => ui.openDialog(DialogType.Backup)}
      title="Backup / restaurar playlists"
      aria-label="Backup de playlists"><Download /></Button
    >
  </div>
{/snippet}
