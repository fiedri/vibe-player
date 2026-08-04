<script lang="ts">
  import Button from "../button/button.svelte";
  import { playlistStore } from "$lib/stores/playlist.svelte";
  import { DialogType, ui } from "$lib/stores/ui.svelte";
  let playlistsName = $state("");
  function handleCreatePlaylist(e: Event) {
    e.preventDefault();
    if (!playlistsName.trim()) return;
    if(ui.dialogPayload){
    playlistStore.add(playlistsName);
    ui.openDialog(DialogType.Playlist, ui.dialogPayload as string);
    }else{
    playlistStore.add(playlistsName)

    ui.closeDialog();
    }
    playlistsName = "";
  }
</script>

<form onsubmit={handleCreatePlaylist} class="flex flex-col gap-4">
  <label
    for="playlist-name"
    class="font-medium text-foreground uppercase text-xl"
  >
    Nueva Playlist
  </label>
  <input
    id="playlist-name"
    type="text"
    placeholder="Nombre de la playlist..."
    class="w-full bg-background px-1 py-3 text-foreground border-0 border-b-2 border-primary outline-none focus:outline-none focus:ring-0 focus:border-primary"
    bind:value={playlistsName}
  />
  <div class="flex justify-end gap-3">
    <Button variant="ghost" onclick={() => ui.closeDialog()} type="button"
      >cancelar</Button
    >
    <Button type="submit" disabled={playlistsName.trim() === ""}>Crear</Button>
  </div>
</form>
