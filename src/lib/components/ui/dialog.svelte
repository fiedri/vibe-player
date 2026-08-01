<script lang="ts">
import { ui } from "$lib/stores/ui.svelte";
import { portal } from "$lib/uiUtils"; 
let open = $derived(ui.isOpenDialog)
</script>
{#if open}
<div use:portal class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/80">
  <div class="bg-zinc-800 p-6 rounded-lg max-w-xs text-center border border-zinc-700">
    <p class="text-white mb-4">Funcionalidad no implementada en esta versión.</p>
    <button onclick={()=> ui.isOpenDialog=false} class="px-4 py-2 bg-blue-600 text-white rounded font-bold">
      ACEPTAR
    </button>
  </div>
</div>
{/if}
