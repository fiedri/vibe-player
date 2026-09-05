<script lang="ts">

import {m} from '$lib/paraglide/messages.js';
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
  import LoadingScreen from "$lib/components/ui/LoadingScreen.svelte";
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
    aria-label={m["playlist.close_menu"]()}
  ></button>
{/if}

<div
  class=" h-full w-full min-w-0 overflow-x-hidden flex flex-col gap-15 min-h-0 overflow-y-auto"
>
  {#if playlistStore.isLoading && playlists.length === 0}
    <LoadingScreen text={m["playlist.loading"]()} />
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
           {m['playlist.favorites']()} 
          </h2>
        </div>
        <div class="mr-5 relative flex flex-row gap-3 items-center">
          <span class="text-xs text-muted-foreground hover:underline truncate"
            >{m.songs({ count: favorito?.songsCount ?? 0 })}</span
          >
        </div>
      </a>
      <h2 class="font-bold text-xl uppercase px-2">{m["playlist.title"]()}</h2>

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
              >{m.songs({ count: playlist.songsCount })}</span
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
                  variant="destructive">{m["dialogs.confirm_delete.delete"]()}</Button
                >
              </div>
            {/if}
          </div>
        </div>
      {:else}
        <div class="h-full w-full flex justify-center items-center">
          <p class="text-muted-foreground text-lg italic">
           {m['playlist.no_playlist']()} 
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
      title={m["playlist.backup.title"]()}
      aria-label={m["playlist.backup.title"]()}><Download /></Button
    >
  </div>
{/snippet}
