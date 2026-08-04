<script lang="ts">
import { DialogType, ui } from "$lib/stores/ui.svelte";
import { portal } from "$lib/uiUtils"; 
let { children }= $props();

</script>
{#if ui.activeDialog}
<div use:portal class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/20"
onclick={() => ui.closeDialog()}>

<div class="bg-popover p-6 w-[90%] border border-border" onclick={(e) => e.stopPropagation()}>
{@render children(ui.activeDialog)}
  </div>
</div>
{/if}
