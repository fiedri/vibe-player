<script>
    import Button from "../button/button.svelte";
    import { playlistStore } from "$lib/stores/playlist.svelte";
let {playlistId, isOpen} = $props()
let options = [
{
text: "Eliminar Duplicados",
onclick: ()=> {playlistStore.deleteDuplicates(playlistId); isOpen= false}
}
]
</script>
<div class="absolute top-full bg-popover right-5 border-border">
{#each options as option}
 <Button variant="secondary" onclick={option.onclick}>{option.text}</Button> 
{/each}
</div>
