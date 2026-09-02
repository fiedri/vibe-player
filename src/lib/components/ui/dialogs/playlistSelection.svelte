<script lang="ts">
  import { playlistStore } from "$lib/stores/playlist.svelte";
  import { DialogType, ui } from "$lib/stores/ui.svelte";
  import { SvelteSet } from "svelte/reactivity";
import { m } from "$lib/paraglide/messages.js";
  import { Add, Checkmark } from "carbon-icons-svelte";
  import Button from "../button/button.svelte";
    import { selection } from "$lib/components/multiSelector/selectionStore.svelte";
  let selectedIds = new SvelteSet<number>();
  function togglePlaylist(id: number) {
    if (selectedIds.has(id)) {
      selectedIds.delete(id);
    } else {
      selectedIds.add(id);
    }
  }
  async function handleSubmit() {
    const playlistIdsArray = Array.from(selectedIds);
    const payload = ui.dialogPayload;

    let addAction: (playlistId: number) => {};

    if (payload instanceof Set || Array.isArray(payload)) {
      const songsIdToAdd = Array.from(payload);
      addAction = (playlistId) =>
        playlistStore.addManySongs(playlistId, songsIdToAdd);
    } else {
      // Es una sola canción (string)
      const songId = payload as string;
      addAction = (playlistId) => playlistStore.addSong(playlistId, songId);
    }

    // 2. Un solo bucle 'for' que ejecuta la acción configurada
    for (const playlistId of playlistIdsArray) {
      await addAction(playlistId);
    }
selection.clear()
    ui.closeDialog();
  }
</script>

<div class="flex flex-col gap-4">
  <h2 class="text-base font-bold uppercase tracking-wider text-foreground">
    {m["songs_options.add_to_playlists"]()}
  </h2>

  <div class="flex flex-col max-h-64 overflow-y-auto pr-1">
    <button
      type="button"
      onclick={() => ui.openDialog(DialogType.CreatePlaylist, ui.dialogPayload)}
      class="flex w-full items-center gap-3 px-3 py-3 border border-dashed border-border bg-secondary/40 hover:bg-secondary text-sm font-medium transition-colors cursor-pointer text-foreground active:bg-primary active:text-primary-foreground"
    >
      <Add class="size-5 shrink-0" />
      <span class="uppercase">{m["playlist.add.new_playlist"]()}</span>
    </button>

    {#each playlistStore.playlists as playlist (playlist.id)}
      <button
        type="button"
        onclick={() => togglePlaylist(playlist.id)}
        class="flex w-full items-center gap-3 border px-3 py-3.5 transition-colors cursor-pointer text-sm select-none text-left {selectedIds.has(
          playlist.id,
        )
          ? 'border-primary bg-primary/15 text-foreground'
          : 'border-border bg-card text-foreground hover:bg-accent/40'}"
      >
        <span
          class="flex size-5 shrink-0 items-center justify-center border-2 transition-colors {selectedIds.has(
            playlist.id,
          )
            ? 'border-primary bg-primary text-primary-foreground'
            : 'border-muted-foreground/40 text-transparent'}"
        >
          <Checkmark class="size-4" />
        </span>
        <span
          class="truncate font-medium uppercase transition-colors {selectedIds.has(
            playlist.id,
          )
            ? 'text-primary'
            : 'text-foreground'}">{playlist.name == 'favoritos' ? m["playlist.favorites"](): playlist.name}</span
        >
      </button>
    {/each}
  </div>
  <div class="flex flex-row gap-3 mt-3 justify-end items-center">
    <Button variant="ghost" class="p-0" onclick={() => ui.closeDialog()}
      >{m.cancel()}</Button
    >
    <Button class="p-2 h-10 w-20" onclick={handleSubmit}>{m.add()}</Button>
  </div>
</div>
