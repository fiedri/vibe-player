<script>
import { ui } from "$lib/stores/ui.svelte";
    import Button from "../button/button.svelte";
</script>
<div>
<p>{ui.dialogPayload}</p>
<Button onclick={()=>ui.closeDialog()}>Aceptar</Button>
</div>
