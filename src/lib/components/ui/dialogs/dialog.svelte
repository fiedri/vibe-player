<script lang="ts">
import { DialogType, ui } from "$lib/stores/ui.svelte";
import { portal } from "$lib/uiUtils"; 
let { children }= $props();

</script>
{#if ui.activeDialog}
  <div 
    use:portal 
    class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/20"
    role="button"
    tabindex="-1"
    onclick={() => ui.closeDialog()}
    onkeydown={(e) => e.key === 'Escape' && ui.closeDialog()}
  >
  <!-- svelte-ignore a11y_no_noninteractive_element_interactions-->
    <div 
      class="bg-popover p-6 w-[90%] border border-border" 
      role="document"
      onclick={(e) => e.stopPropagation()}
      onkeydown={(e) => e.stopPropagation()}
    >
      {@render children(ui.activeDialog)}
    </div>
  </div>
{/if}
