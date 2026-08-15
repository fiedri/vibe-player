<script lang="ts">
  import Button from "../button/button.svelte";
  import { ui, DialogType } from "$lib/stores/ui.svelte";
  import { biblioteca } from "$lib/stores/biblioteca.svelte";
  import { Information } from "carbon-icons-svelte";
  import { onDestroy } from "svelte";
  import type { MediaFile } from "$lib/types/songs";

  let { onClose, song }: { onClose: () => void; song: MediaFile } = $props();
  let options = [
    {
      text: "Información",
      variant: "ghost",
      action: () => {
        ui.openDialog(DialogType.InfoSong, song);
        onClose()
      },
      icon: Information
    },
    {
      text: "Agregar a playlist",
      variant: "ghost",
      action: () => {
        ui.openDialog(DialogType.Playlist, song.id);
        onClose();
      },
    },
    {
      text: "Borrar",
      variant: "destructive",
      action: () => {
        biblioteca.deleteSong(song.id, song.uri);
        onClose();
      },
    },
  ];
  onDestroy(() => {
    onClose();
  });
</script>

<div class="bg-popover absolute right-2 top-full w-[60%] z-20">
  {#each options as option}
    <Button
      class="w-full justify-start text-left border-b border-border text-sm active:bg-primary active:text-primary-foreground"
      variant={option.variant}
      onclick={option.action}
    >
      <span class="w-full text-left flex flex-row items-center gap-3">{#if option.icon}<option.icon/>{/if}{option.text}</span>
    </Button>
  {/each}
</div>

<button class="fixed inset-0" onclick={onClose} aria-label="Cerrar Menu"
></button>
