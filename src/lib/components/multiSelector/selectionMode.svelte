<script lang="ts">
  import { ArrowLeft, OverflowMenuVertical } from "carbon-icons-svelte";
  import Button from "../ui/button/button.svelte";
  import { selection } from "./selectionStore.svelte";
  import { DialogType, ui } from "$lib/stores/ui.svelte";
  import { page } from "$app/stores";
  import { playlistStore } from "$lib/stores/playlist.svelte";

  let openMenu = $state(false);
  let currentPlaylistId = $derived.by(() => {
    const match = $page.url.pathname.match(/^\/playlist\/(\d+)$/);
    return match ? Number(match[1]) : null;
  });

  async function removeFromPlaylist() {
    openMenu = false;
    if (currentPlaylistId === null) return;
    const ids = Array.from(selection.seletedIds) as string[];
    await playlistStore.removeManySongs(currentPlaylistId, ids);
    selection.clear();
  }
</script>

{#if selection.isActive}
  <div
    class="fixed top-0 h-16 bg-popover border-b-2 border-0 border-border right-0 left-0 flex flex-row items-center justify-between px-2 z-100"
  >
    <div class="flex flex-row items-center">
      <Button variant="ghost" class="p-2" onclick={() => selection.clear()}>
        <ArrowLeft class="size-8" />
      </Button>
      <span>{selection.count} Seleccionados</span>
    </div>
    <Button
      variant="ghost"
      class="p-2"
      onclick={() => {
        openMenu = !openMenu;
      }}
    >
      <OverflowMenuVertical class="size-8" />
    </Button>
    {#if openMenu}
      <div
        class="absolute top-full right-2 bg-popover flex flex-col border-2 border-border"
      >
        <Button
          variant="ghost"
          class="w-full justify-start text-sm active:bg-primary active:text-primary-foreground"
          onclick={() => {
            openMenu = false;
            ui.openDialog(DialogType.Playlist, selection.seletedIds);
          }}
        >
          Agregar a playlist
        </Button>
        {#if currentPlaylistId !== null}
          <Button
            variant="ghost"
            class="w-full justify-start text-sm active:bg-primary active:text-primary-foreground"
            onclick={removeFromPlaylist}
          >
            Quitar de la playlist
          </Button>
        {/if}
        <Button
          variant="destructive"
          class="w-full justify-start text-sm active:bg-primary active:text-primary-foreground"
          onclick={() => {
            openMenu = false;
            ui.openDialog(DialogType.ConfirmDelete, selection.seletedIds);
          }}
        >
          Eliminar
        </Button>
      </div>
    {/if}
  </div>
{/if}
