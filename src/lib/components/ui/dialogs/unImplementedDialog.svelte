<script>
    import Button from "../button/button.svelte";
import { ui } from "$lib/stores/ui.svelte";
import { m } from "$lib/paraglide/messages.js";
</script>
<p>{m["dialogs.unimplemented"]()}</p>
<Button onclick={()=> ui.closeDialog()}>{m["dialogs.accept"]()}</Button>
