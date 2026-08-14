<script lang="ts">
  import Button from "../button/button.svelte";
  import { ui } from "$lib/stores/ui.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { selection } from "$lib/components/multiSelector/selectionStore.svelte";
    import { playerService as player } from "$lib/services/player/PlayerFacade";

  let selectedIds = $derived(ui.dialogPayload as Set<string>);
  let count = $derived(selectedIds?.size ?? 0);

  function handleCancel() {
    ui.closeDialog();
    selection.clear();
  }

  async function handleConfirm() {
    const idsToDelete = new Set(selectedIds);
    const currentSongId = player.currentSong?.id;
    if (currentSongId && idsToDelete.has(currentSongId)) {
      player.currentSong = null;
    }
    ui.closeDialog();
    selection.clear();
    if (idsToDelete.size > 0) {
      const idString = new Set(Array.from(idsToDelete, String));
      await biblioteca.deleteManySongs(idString);
    }
  }
</script>

<div class="flex flex-col gap-4">
  <h3 class="font-medium text-foreground uppercase text-xl">
    Eliminar Canciones
  </h3>
  <p class="text-muted-foreground text-sm">
    ¿Estás seguro que deseas eliminar {count} canciones?
  </p>
  <div class="flex justify-end gap-3 pt-2">
    <Button variant="ghost" onclick={handleCancel} type="button">
      Cancelar
    </Button>
    <Button variant="destructive" onclick={handleConfirm} type="button">
      Eliminar
    </Button>
  </div>
</div>
