<script>
    import { ArrowLeft, OverflowMenuVertical } from "carbon-icons-svelte";
import Button from "../ui/button/button.svelte";
import { selection } from "./selectionStore.svelte";
</script>
{#if selection.isActive}
  
<div class="fixed top-0 h-16 bg-popover border-b-2 border-0 border-border right-0 left-0 flex flex-row items-center justify-between px-2">
  <Button variant="ghost" class="p-2" onclick={()=> selection.isActive = false}>
      <ArrowLeft class="size-8" />
    </Button>
    <Button variant="ghost" class="p-2"><OverflowMenuVertical class="size-8"/></Button>
    <div class="absolute top-full right-2 bg-popover flex flex-col border-2 border-border">
    <Button variant="ghost">Agregar a playlist</Button>
    <Button variant="destructive" class="text-left">Eliminar</Button>
    </div>
</div>
{/if}
