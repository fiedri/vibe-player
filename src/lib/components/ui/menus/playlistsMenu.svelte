<script>
    import Button from "../button/button.svelte";
    import { playlistStore } from "$lib/stores/playlist.svelte";
let {playlistId, onClose} = $props()
let options = [
{
text: "Eliminar Duplicados",
onclick: ()=> {playlistStore.deleteDuplicates(playlistId); onClose()}
}
]
</script>
<div class="absolute top-full bg-popover right-5 border-border z-10">
{#each options as option}
 <Button variant="secondary" onclick={option.onclick}>{option.text}</Button> 
{/each}
</div>
<button class="fixed inset-0 " onclick={onClose} aria-label="Cerrar Menu">
</button>
