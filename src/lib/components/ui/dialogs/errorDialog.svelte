<script>
import { ui } from "$lib/stores/ui.svelte";
import { m } from "$lib/paraglide/messages.js";
    import Button from "../button/button.svelte";
</script>
<div>
<p>{ui.dialogPayload}</p>
<Button onclick={()=>ui.closeDialog()}>{m["dialogs.accept"]()}</Button>
</div>
